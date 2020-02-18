package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.common.alarm.Alarm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class AlarmCacheConfig {

  @Bean
  public C2monCache<Alarm> alarmCache(AbstractCacheFactory igniteInstance) {
    return igniteInstance.createCache("alarm", Alarm.class);
  }
}
