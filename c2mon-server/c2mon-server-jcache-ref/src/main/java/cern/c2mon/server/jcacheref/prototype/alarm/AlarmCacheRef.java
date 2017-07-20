package cern.c2mon.server.jcacheref.prototype.alarm;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.stereotype.Component;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.jcacheref.prototype.common.AbstractCacheRef;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Component
public class AlarmCacheRef extends AbstractCacheRef<Long, Alarm> implements BasicCache, Serializable {

  private static final String ALARM_TAG_CACHE = "alarmTagCache";

  public AlarmCacheRef() {
    super();
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.ALARM;
  }

  @Override
  protected CacheConfiguration configureCache() {
    CacheConfiguration<Long, Alarm> config = new CacheConfiguration<>(ALARM_TAG_CACHE);

    config.setIndexedTypes(Long.class, Alarm.class);
    config.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

    return config;
  }
}
