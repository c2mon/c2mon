package cern.c2mon.server.jcacheref.various.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Szymon Halastra
 */

@Aspect
public class AspectsSystem {

  @Pointcut("@@annotation(cern.c2mon.server.jcacheref.various.annotations.WriteCacheGuard)")
  public void SafeWriteCache() {

  }

  @Pointcut("@@annotation(cern.c2mon.server.jcacheref.various.annotations.ReadCacheGuard)")
  public void SafeReadCache() {

  }
}
