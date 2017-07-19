package cern.c2mon.server.jcacheref.prototype.alarm;

import java.io.Serializable;

import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.jcacheref.prototype.common.AbstractCacheRef;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;

/**
 * @author Szymon Halastra
 */

@Configuration
public class AlarmCacheConfig extends AbstractCacheRef implements BasicCache, Serializable {

  private static final String ALARM_TAG_CACHE = "alarmTagCache";

  public AlarmCacheConfig() {
    super();
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.ALARM;
  }

  @Override
  protected CacheConfiguration configureCache() {
    return null;
  }
}
