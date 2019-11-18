package cern.c2mon.cache.actions.deviceclass;

import cern.c2mon.cache.actions.AbstractCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.device.DeviceClass;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Service
public class DeviceClassService extends AbstractCacheService<DeviceClass> {

  @Inject
  public DeviceClassService(C2monCache<DeviceClass> deviceClassCacheRef) {
    super(deviceClassCacheRef, new DefaultC2monCacheFlow<>());
  }

  public List<String> getDeviceClassNames() {
    return cache.getKeys().stream()
      .map(cache::get)
      .map(DeviceClass::getName)
      .collect(Collectors.toList());
  }
}

