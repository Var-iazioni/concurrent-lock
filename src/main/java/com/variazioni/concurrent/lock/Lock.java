package com.variazioni.concurrent.lock;

/**
 * 分布式锁接口
 *
 * @author 王振宇
 * @date 2020/07/13
 */
public interface Lock {

  /**
   * 查询锁数量
   *
   * @author 王振宇
   * @param lockName 资源名称
   * @return int 当前已加锁的数量，若资源未加锁则返回0
   */
  public int locksCount(String lockName);

  /**
   * 加锁
   *
   * @author 王振宇
   * @param lockName 资源名称
   * @param lockKey 持锁人key
   * @param limit 共享锁限制数量
   * @param timeout 锁超时时间,单位:秒
   * @return boolean
   */
  public boolean lock(String lockName, String lockKey, int limit, int timeout);

  /**
   * 解锁
   *
   * @author 王振宇
   * @param lockName 资源名称
   * @param lockKey 持锁人key
   * @return boolean
   */
  public boolean unlock(String lockName, String lockKey);

  /**
   * 等待锁释放
   *
   * @author 王振宇
   * @param lockName 资源名称
   * @return void
   */
  public boolean waitLock(String lockName);

  /**
   * 超时时间延长
   *
   * @author 王振宇
   * @param lockName 资源名称
   * @param lockKey 持锁人key
   * @param timeout 超时时间
   * @return boolean
   * @throws
   */
  public boolean extendLock(String lockName, String lockKey, int timeout);
}
