package com.variazioni.concurrent.lock.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.variazioni.concurrent.lock.Lock;
import com.variazioni.concurrent.lock.exception.LockException;

/**
 * Lock service class
 * 
 * @author Variazioni
 * @date 2021/07/18
 */
@Service
public class LockService {

  @Resource(name = "lockByDatabase")
  private Lock lock;

  // For saving locked thread key
  private ConcurrentHashMap<Long, HashMap<String, String>> threadSaved =
      new ConcurrentHashMap<Long, HashMap<String, String>>(20);

  /**
   * Locks Count
   *
   * @author Variazioni
   * @param lockName
   * @return int The current number of locked resources. If the resource is not locked, 0 is
   *         returned
   * @throws LockException
   */
  public int locksCount(String lockName) throws LockException {
    int result = lock.locksCount(lockName);
    if (result == -1) {
      throw new LockException(
          "Query failed! Please check the database configuration and connection status!");
    }
    return result;
  }

  /**
   * Shared lock
   *
   * @author Variazioni
   * @param resourceName
   * @param lockinTime
   * @param limit The maximum number of locks on this resource. If the number exceeds, locking will
   *        wait for other threads to complete and unlock before locking.
   * @param wait true or null:If the lock is not successful after waiting for 10 minutes, an
   *        exception will be thrown;false:If one time lock fails, an exception will be thrown
   */
  public void lock(String lockName, int lockinTime, int limit, Boolean wait) throws LockException {
    if (wait == null) {
      wait = true;
    }
    if (!lock.lock(lockName, createLockKey(lockName), limit, lockinTime, wait)) {
      throw new LockException("Locking failed! Please check the database configuration!");
    }
  }

  /**
   * Exclusive lock
   *
   * @author Variazioni
   * @param resourceName
   * @param lockinTime
   * @param wait true or null:If the lock is not successful after waiting for 10 minutes, an
   *        exception will be thrown;false:If one time lock fails, an exception will be thrown
   */
  public void lock(String lockName, int lockinTime, Boolean wait) throws LockException {
    if (wait == null) {
      wait = true;
    }
    if (!lock.lock(lockName, createLockKey(lockName), lockinTime, wait)) {
      throw new LockException("Locking failed! Please check the database configuration!");
    }
  }

  /**
   * Unlock
   * 
   * @author Variazioni
   * @param resourceName
   * @throws LockException
   */
  public void unlock(String resourceName) throws LockException {

    // Get LockKey
    String lockKey = getLockKey(Thread.currentThread().getId(), resourceName);

    // If lockkey is null, means lock is exclusive lock
    if (lockKey == null) {
      lockKey = "SINGLE_LOCK";
    }

    // Unlock
    if (!lock.unlock(resourceName, lockKey)) {
      throw new LockException("Unlocking failed! Please check whether the lock has timed out!");
    }
  }

  /**
   * Waiting for lock release
   * 
   * @author Variazioni
   * @param resourceName
   * @throws LockException
   */
  public void waitLock(String resourceName) throws LockException {
    if (!lock.waitLock(resourceName)) {
      throw new LockException("Query lock failed!");
    }
  }

  /**
   * Lock timeout extended
   * 
   * @author Variazioni
   * @param resourceName
   * @param second
   * @throws LockException
   */
  public void extendLock(String resourceName, int second) throws LockException {

    // Get lockKey
    String lockKey = getLockKey(Thread.currentThread().getId(), resourceName);

    // If lockkey is null, means lock is exclusive lock
    if (lockKey == null) {
      lockKey = "SINGLE_LOCK";
    }

    if (!lock.extendLock(resourceName, lockKey, second)) {
      throw new LockException("Extend lock time failed! Please check whether it has timed out!");
    }
  }

  /**
   * Create lockkey
   * 
   * @author Variazioni
   * @param resourceName
   * @return String
   */
  private String createLockKey(String resourceName) {
    Long threadId = Thread.currentThread().getId();
    String lockKey = UUID.randomUUID().toString().replace("-", "") + ":" + threadId;

    HashMap<String, String> lockKeyMap = threadSaved.get(threadId);
    if (lockKeyMap == null || lockKeyMap.size() == 0) {
      lockKeyMap = new HashMap<String, String>();
    }
    lockKeyMap.put(lockKey, resourceName);
    threadSaved.put(threadId, lockKeyMap);

    return lockKey;
  }

  /**
   * Get lockkey
   * 
   * @author Variazioni
   * @param threadId
   * @param resourceName
   * @return String
   */
  private String getLockKey(Long threadId, String resourceName) {
    String lockKey = null;
    /*
     * Traverse the map to obtain the lockkey and lockname of the current thread. If the current
     * thread has multiple locks on a resource, the first one will be obtained
     */
    HashMap<String, String> lockKeyMap = threadSaved.get(threadId);
    try {
      Iterator<String> iter = lockKeyMap.keySet().iterator();
      while (iter.hasNext()) {

        String key = iter.next();
        String value = lockKeyMap.get(key);

        if (value.equals(resourceName)) {
          lockKey = key;
          lockKeyMap.remove(key);
          threadSaved.put(threadId, lockKeyMap);
          break;
        }
      }
    } catch (Exception e) {
      lockKey = null;
    }
    return lockKey;
  }
}
