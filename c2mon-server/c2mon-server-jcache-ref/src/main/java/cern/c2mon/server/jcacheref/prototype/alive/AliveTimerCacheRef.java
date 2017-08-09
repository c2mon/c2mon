package cern.c2mon.server.jcacheref.prototype.alive;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.jcacheref.prototype.alive.operations.AliveTimerManager;
import cern.c2mon.server.jcacheref.prototype.alive.operations.AliveTimerOperation;
import cern.c2mon.server.jcacheref.prototype.common.AbstractCacheRef;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Component
public class AliveTimerCacheRef /*extends AbstractCacheRef<Long, AliveTimer> implements BasicCache<Long, AliveTimer>, Serializable*/ {

  public static final String ALIVE_TIMER_CACHE = "aliveTimerCacheRef";

  public AliveTimerCacheRef() {
    super();
  }

//  @Override
//  protected CacheConfiguration<Long, AliveTimer> configureCache() {
//    CacheConfiguration<Long, AliveTimer> config = new CacheConfiguration<>(ALIVE_TIMER_CACHE);
//
//    config.setIndexedTypes(Long.class, AliveTimer.class);
//    config.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
//
//    return config;
//  }
//
//  @Override
//  public C2monCacheName getName() {
//    return C2monCacheName.ALIVETIMER;
//  }
//
//  private List<Long> getKeys() {
//    Iterator<Cache.Entry<Long, AliveTimer>> iterator = cache.iterator();
//
//    return null;
//  }
//
//
//  public Object invoke(Long id, AliveTimerManager aliveTimerManager, AliveTimerOperation start) {
//    return null;
//  }
//
//  public Iterator<Cache.Entry<Long, AliveTimer>> iterator() {
//    return null;
//  }
//
//  public void put(Long id, AliveTimer aliveTimer) {
//
//  }
//
//  public AliveTimer get(long l) {
//    return null;
//  }
//
//  public void putAll(Map<Long, AliveTimer> aliveTimers) {
//
//  }
//
//  public Map<Long, AliveTimer> getAll(Set<Long> longs) {
//
//    return null;
//  }
}
