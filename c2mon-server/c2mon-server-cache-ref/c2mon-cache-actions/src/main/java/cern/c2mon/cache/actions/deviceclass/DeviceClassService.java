package cern.c2mon.cache.actions.deviceclass;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.device.DeviceClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Service
public class DeviceClassService {

  private C2monCache<DeviceClass> deviceClassCacheRef;

  @Inject
  public DeviceClassService(C2monCache<DeviceClass> deviceClassCacheRef) {
    this.deviceClassCacheRef = deviceClassCacheRef;
  }

  public List<String> getDeviceClassNames() {
    List<String> classNames = new ArrayList<>();

    for (Long deviceClassId : deviceClassCacheRef.getKeys()) {
      DeviceClass deviceClass = deviceClassCacheRef.get(deviceClassId);
      classNames.add(deviceClass.getName());
    }

    return classNames;
  }
}

