package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.config.CacheName;
import org.apache.ignite.Ignite;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@EnableTransactionManagement
public class AlarmCache extends IgniteC2monCache<Alarm> {

  /**
   * It is important to remember the cache is not yet initialized, even after the constructor
   * is finished! You need to explicitly call the {@link C2monCache#init()} method
   * to "activate" the ignite cache and preload with data
   *
   * @param igniteInstance a working ignite reference
   */
  @Inject
  public AlarmCache(Ignite igniteInstance) {
    super(CacheName.ALARM.name(), new DefaultIgniteCacheConfiguration<>(CacheName.ALARM.name()), igniteInstance);
  }
}
