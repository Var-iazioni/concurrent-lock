package com.variazioni.concurrent.lock.aspect;

import java.util.UUID;
import javax.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import com.variazioni.concurrent.lock.Lock;
import com.variazioni.concurrent.lock.anno.ExclusiveLock;

/**
 * Exclusive lock aspect class
 * 
 * @author Variazioni
 * @date 2021/07/18
 */
@Aspect
@Component
public class ExclusiveLockAspect {

  @Resource(name = "lockByDatabase")
  private Lock lock;

  /**
   * Pointcut
   * 
   * @author Variazioni
   */
  @Pointcut("@annotation(com.variazioni.concurrent.lock.anno.ExclusiveLock)")
  public void addExclusiveLockAdvice() {}

  /**
   * Around notice
   * 
   * @author Variazioni
   * @param proceedingJoinPoint
   * @param exclusiveLock
   * @return Object
   * @throws Throwable
   */
  @Around("@annotation(exclusiveLock)")
  public Object exclusiveLockAround(ProceedingJoinPoint proceedingJoinPoint,
      ExclusiveLock exclusiveLock) throws Throwable {
    Object returnValue = null;

    String lockKey =
        UUID.randomUUID().toString().replace("-", "") + ":" + Thread.currentThread().getId();
    String lockName = proceedingJoinPoint.getSignature().getDeclaringType().toString() + "."
        + proceedingJoinPoint.getSignature().getName();

    // Preposition
    if (!lock.lock(lockName, lockKey, exclusiveLock.timeout(), true)) {
      throw new RuntimeException(proceedingJoinPoint.getSignature().getDeclaringType().toString()
          + "." + proceedingJoinPoint.getSignature().getName() + "locking error!");
    }

    Object[] args = proceedingJoinPoint.getArgs();
    returnValue = proceedingJoinPoint.proceed(args);

    // Postposition
    if (!lock.unlock(lockName, lockKey)) {
      throw new RuntimeException(proceedingJoinPoint.getSignature().getDeclaringType().toString()
          + "." + proceedingJoinPoint.getSignature().getName() + "unlocking error!");
    }
    return returnValue;
  }
}
