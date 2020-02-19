package cern.c2mon.cache.actions.deviceclass;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.server.common.device.DeviceClass;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Named
@Singleton
public class DeviceClassService extends AbstractCacheServiceImpl<DeviceClass> {

  @Inject
  public DeviceClassService(C2monCache<DeviceClass> deviceClassCacheRef) {
    super(deviceClassCacheRef, new DefaultCacheFlow<>());
  }

  public long getIdByName(String name) {
    return cache.query(new CacheQuery<DeviceClass>(deviceClass -> deviceClass.getName().equalsIgnoreCase(name))
      .maxResults(1))
      .stream()
      .findFirst()
      .orElseThrow(CacheElementNotFoundException::new)
      .getId();
  }

  public List<String> getDeviceClassNames() {
    return cache.getKeys().stream()
      .map(cache::get)
      .map(DeviceClass::getName)
      .collect(Collectors.toList());
  }
}

