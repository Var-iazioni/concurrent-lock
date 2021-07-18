package com.variazioni.concurrent.lock;

/**
 * Resource lock interface
 * 
 * @author Variazioni
 * @date 2021/07/18
 */
public interface Lock {

  /**
   * locks statistics
   * 
   * @author Variazioni
   * @param lockName
   * @return int
   */
  public int locksCount(String lockName);

  /**
   * Shared lock
   * 
   * @author Variazioni
   * @param lockName
   * @param lockKey
   * @param limit
   * @param timeout
   * @param wait
   * @return boolean
   */
  public boolean lock(String lockName, String lockKey, int limit, int timeout, Boolean wait);


  /**
   * Exclusive lock
   * 
   * @author Variazioni
   * @param lockName
   * @param lockKey
   * @param lockinTime
   * @param wait
   * @return boolean
   */
  public boolean lock(String lockName, String lockKey, int lockinTime, Boolean wait);

  /**
   * Unlock
   * 
   * @author Variazioni
   * @param lockName
   * @param lockKey
   * @return boolean
   */
  public boolean unlock(String lockName, String lockKey);

  /**
   * Waiting to unlock
   * 
   * @author Variazioni
   * @param lockName
   * @return boolean
   */
  public boolean waitLock(String lockName);

  /**
   * Lock timeout extended
   * 
   * @author Variazioni
   * @param lockName
   * @param lockKey
   * @param timeout
   * @return boolean
   */
  public boolean extendLock(String lockName, String lockKey, int timeout);
}
