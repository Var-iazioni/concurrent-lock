package com.variazioni.concurrent.lock.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shared lock annotation class
 * 
 * @author Variazioni
 * @date 2021/07/18
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SharedLock {

  /**
   * Timeout second
   * 
   * @author Variazioni
   * @return int
   */
  public int timeout();

  /**
   * Lock number limit
   * 
   * @author Variazioni
   * @return int
   */
  public int limit();
}
