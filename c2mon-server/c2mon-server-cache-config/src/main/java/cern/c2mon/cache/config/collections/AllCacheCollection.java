package cern.c2mon.cache.config.collections;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.set.CacheCollection;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.command.CommandTag;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Aggregates all C2MON caches
 *
 * @author Alexandros Papageorgiou
 */
@Named
@Singleton
public class AllCacheCollection extends CacheCollection<Cacheable> {

  @Inject
  public AllCacheCollection(TagCacheCollection tagCacheCollection, SupervisedCacheCollection supervisedCacheCollection,
                            C2monCache<Alarm> alarmCache, C2monCache<CommandTag<?>> commandTagCache,
                            C2monCache<Device> deviceCache, C2monCache<DeviceClass> deviceClassCache) {
    super(new CacheCollection<>(alarmCache, commandTagCache, deviceCache, deviceClassCache),
      tagCacheCollection,
      supervisedCacheCollection);
  }
}
