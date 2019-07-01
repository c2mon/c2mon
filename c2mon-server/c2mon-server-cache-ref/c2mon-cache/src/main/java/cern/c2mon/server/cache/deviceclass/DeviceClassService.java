package cern.c2mon.server.cache.deviceclass;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCacheBase;
import cern.c2mon.server.common.device.DeviceClass;

/**
 * @author Szymon Halastra
 */

@Service
public class DeviceClassService {

  private C2monCacheBase<Long, DeviceClass> deviceClassCacheRef;

//  @Autowired
//  public DeviceClassService(C2monCache<Long, DeviceClass> deviceClassCacheRef) {
//    this.deviceClassCacheRef = deviceClassCacheRef;
//  }

  public List<String> getDeviceClassNames() {
    List<String> classNames = new ArrayList<>();

    for (Long deviceClassId : deviceClassCacheRef.getKeys()) {
      DeviceClass deviceClass = deviceClassCacheRef.get(deviceClassId);
      classNames.add(deviceClass.getName());
    }

    return classNames;
  }
}

