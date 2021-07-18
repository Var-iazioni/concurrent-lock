package com.variazioni.concurrent.lock.impl;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import com.variazioni.concurrent.lock.Lock;

/**
 * Implementation class. Based on MySQL
 * 
 * @author Variazioni
 * @date 2021/07/18
 */
@Component
public class LockByDatabase implements Lock {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(LockByDatabase.class);

  protected volatile boolean isOpenExpirationRenewal = true;

  /**
   * Locks statistics implementation
   * 
   * @author Variazioni
   * @param lockName
   * @return int
   */
  @Override
  public int locksCount(String lockName) {
    int result = 0;
    try {
      // Timeout mechanism, before each insert, clear the timeout lock
      clearTimedoutLocks(lockName);

      // Query sql
      String sql = "SELECT COUNT(`LOCK_KEY`) FROM `RESOURCE_LOCK` WHERE `LOCK_NAME` = ?";
      SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, lockName);
      if (rs.next()) {
        result = rs.getInt(1);
      }

    } catch (Exception e) {
      logger.error("Query lock quantity database exception, return failure!", e);
      return -1;
    }
    return result;
  }

  /**
   * Shared lock implementation
   * 
   * @author Variazioni
   * @param lockName
   * @param lockKey
   * @param limit
   * @param timeout
   * @param wait
   * @return boolean
   */
  @Override
  public boolean lock(String lockName, String lockKey, int limit, int timeout, Boolean wait) {
    try {
      if (wait) {
        // Block, wait 10 minutes
        for (int i = 0; i < 120; i++) {
          if (lockDatabase(lockName, lockKey, limit, timeout)) {
            return true;
          }
          TimeUnit.SECONDS.sleep(5);
        }
      } else {
        return lockDatabase(lockName, lockKey, limit, timeout);
      }
    } catch (Exception e) {
      logger.error("Database exception, lock failure!");
      return false;
    }
    return false;
  }

  /**
   * Exclusive lock implementation
   * 
   * @author Variazioni
   * @param lockName
   * @param lockKey
   * @param lockinTime
   * @param wait
   * @return boolean
   */
  @Override
  public boolean lock(String lockName, String lockKey, int timeout, Boolean wait) {
    try {
      if (wait) {
        // Block, wait 10 minutes
        for (int i = 0; i < 200; i++) {
          if (lockDatabaseSingle(lockName, lockKey, timeout)) {
            return true;
          }
          TimeUnit.SECONDS.sleep(3);
        }
      } else {
        return lockDatabaseSingle(lockName, lockKey, timeout);
      }
    } catch (Exception e) {
      logger.error("Database exception, lock failure!");
      return false;
    }
    return false;
  }

  /**
   * Exclusive lock process
   * 
   * @author Variazioni
   * @param lockName
   * @param lockKey
   * @param timeout
   * @return boolean
   */
  public boolean lockDatabaseSingle(String lockName, String lockKey, int timeout) {

    try {
      // Timeout mechanism, before each insert, clear the timeout lock
      clearTimedoutLocks(lockName);

      // Insert SQL
      String insertSql =
          "INSERT INTO RESOURCE_LOCK (`LOCK_KEY`,`LOCK_NAME`,`LOCK_HOLDER`,`EXPIRED_TIME`) VALUES ('SINGLE_LOCK', ?, ?, DATE_ADD(NOW(), INTERVAL ? SECOND))";
      if (jdbcTemplate.update(insertSql, lockName, lockKey, timeout) == 0) {
        return false;
      }
    } catch (Exception e) {
      logger.error("Database exception, lock failure!");
      return false;
    }
    return true;
  }

  /**
   * Shared lock process
   * 
   * @author Variazioni
   * @param lockName
   * @param lockKey
   * @param limit
   * @param timeout
   * @return boolean
   */
  public boolean lockDatabase(String lockName, String lockKey, int limit, int timeout) {

    try {

      // Timeout mechanism, before each insert, clear the timeout lock
      clearTimedoutLocks(lockName);

      // Insert SQL
      String insertSql =
          "INSERT INTO RESOURCE_LOCK ( `LOCK_KEY`, `LOCK_NAME`,`LOCK_HOLDER`,`EXPIRED_TIME` ) SELECT ?, ?, ?,(SELECT DATE_ADD(NOW(),INTERVAL ? SECOND)) EXPIRED_TIME FROM DUAL WHERE TRUE = ( SELECT IF( ( SELECT COUNT(`LOCK_KEY`) FROM RESOURCE_LOCK WHERE `LOCK_NAME` = ?) < ?,TRUE,FALSE))";
      if (jdbcTemplate.update(insertSql, lockKey, lockName, lockKey, timeout, lockName,
          limit) == 0) {
        return false;
      }
    } catch (Exception e) {
      logger.error("Database exception, lock failure!");
      return false;
    }
    return true;
  }

  /**
   * Unlock implementation
   * 
   * @author Variazioni
   * @param lockName
   * @param lockKey
   * @return boolean
   */
  @Override
  public boolean unlock(String lockName, String lockKey) {

    // Timeout mechanism, before each insert, clear the timeout lock
    clearTimedoutLocks(lockName);

    try {
      // Delete SQL
      String sql = "DELETE FROM `RESOURCE_LOCK` WHERE `LOCK_NAME` = ? AND `LOCK_HOLDER` = ?";
      if (jdbcTemplate.update(sql, lockName, lockKey) == 0) {
        return false;
      }
    } catch (Exception e) {
      logger.error("Database exception, unlock failure!", e);
      return false;
    }
    return true;
  }

  /**
   * Waiting to unlock
   * 
   * @author Variazioni
   * @param lockName
   * @return boolean
   */
  @Override
  public boolean waitLock(String lockName) {
    while (true) {
      if (0 == locksCount(lockName)) {
        return true;
      } else if (-1 == locksCount(lockName)) {
        return false;
      }
      try {
        TimeUnit.SECONDS.sleep(3);
      } catch (InterruptedException e) {
        e.printStackTrace();
        return false;
      }
    }
  }

  /**
   * Lock timeout extended
   * 
   * @author Variazioni
   * @param lockName
   * @param lockKey
   * @param timeout
   * @return boolean
   */
  @Override
  public boolean extendLock(String lockName, String lockKey, int second) {

    try {
      String sql =
          "UPDATE RESOURCE_LOCK SET `EXPIRED_TIME` = (SELECT DATE_ADD(NOW(),INTERVAL ? SECOND)) WHERE `LOCK_KEY` = ? AND LOCK_NAME = ?";
      if (jdbcTemplate.update(sql, second, lockKey, lockName) == 0) {
        return false;
      }
    } catch (Exception e) {
      logger.error("Database exception, lock timeout extended failure!", e);
      return false;
    }
    return true;
  }

  /**
   * Clear timedout locks
   * 
   * @author Variazioni
   * @param lockName
   */
  private void clearTimedoutLocks(String lockName) {
    try {
      String deleteSql =
          "DELETE FROM RESOURCE_LOCK WHERE `LOCK_NAME` = ? AND `EXPIRED_TIME` < NOW()";
      jdbcTemplate.update(deleteSql, lockName);
    } catch (Exception e) {
      logger.error("Clearing timeout lock database exception, clearing failed!");
    }
  }

}
