package com.variazioni.concurrent.lock.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exclusive lock annotation class
 * 
 * @author Variazioni
 * @date 2021/07/18
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExclusiveLock {

  /**
   * Timeout second
   * 
   * @author Variazioni
   * @return int
   */
  public int timeout();

}
