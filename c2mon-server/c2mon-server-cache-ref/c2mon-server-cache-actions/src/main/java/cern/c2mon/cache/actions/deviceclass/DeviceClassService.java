package cern.c2mon.cache.actions.deviceclass;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.device.DeviceClass;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Service
public class DeviceClassService extends AbstractCacheServiceImpl<DeviceClass> {

  @Inject
  public DeviceClassService(C2monCache<DeviceClass> deviceClassCacheRef) {
    super(deviceClassCacheRef, new DefaultCacheFlow<>());
  }

  public List<String> getDeviceClassNames() {
    return cache.getKeys().stream()
      .map(cache::get)
      .map(DeviceClass::getName)
      .collect(Collectors.toList());
  }
}

