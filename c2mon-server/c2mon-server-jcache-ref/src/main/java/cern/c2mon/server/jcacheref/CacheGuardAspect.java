package cern.c2mon.server.jcacheref;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import cern.c2mon.server.jcacheref.utils.FunctionalReadWriteLock;

/**
 * @author Szymon Halastra
 */

@Component
@Aspect
public class CacheGuardAspect {

  private FunctionalReadWriteLock guard;

  public CacheGuardAspect() {
    guard = new FunctionalReadWriteLock();
  }

  @Around("cern.c2mon.server.jcacheref.AspectsSystem.SafeWriteCache()")
  public Object safeWrite(final ProceedingJoinPoint joinPoint) throws Throwable {
    guard.write(() -> joinPoint.proceed());
  }

  @Around(value = "@annotation(cern.c2mon.server.jcacheref.ReadCacheGuard)")
  public Object safeRead(final ProceedingJoinPoint joinPoint) throws Throwable {
    guard.read(() -> joinPoint.getArgs()[0]);
  }
}
