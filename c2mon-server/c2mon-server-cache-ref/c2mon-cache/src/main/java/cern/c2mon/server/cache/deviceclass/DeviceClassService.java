package cern.c2mon.server.cache.deviceclass;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.device.DeviceClass;

/**
 * @author Szymon Halastra
 */

@Service
public class DeviceClassService {

  private C2monCache<Long, DeviceClass> deviceClassCache;

  @Autowired
  public DeviceClassService(C2monCache<Long, DeviceClass> deviceClassCache) {
    this.deviceClassCache = deviceClassCache;
  }

  public List<String> getDeviceClassNames() {
    List<String> classNames = new ArrayList<>();

    for (Long deviceClassId : deviceClassCache.getKeys()) {
      DeviceClass deviceClass = deviceClassCache.get(deviceClassId);
      classNames.add(deviceClass.getName());
    }

    return classNames;
  }
}

