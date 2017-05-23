package cern.c2mon.server.jcacheref;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Szymon Halastra
 */

@Aspect
public class AspectsSystem {

  @Pointcut("@@annotation(cern.c2mon.server.jcacheref.WriteCacheGuard)")
  public void SafeWriteCache() {

  }

  @Pointcut("@@annotation(cern.c2mon.server.jcacheref.ReadCacheGuard)")
  public void SafeReadCache() {

  }
}
