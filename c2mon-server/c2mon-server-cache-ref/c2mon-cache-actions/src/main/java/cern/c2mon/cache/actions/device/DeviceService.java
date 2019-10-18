package cern.c2mon.cache.actions.device;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.device.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Service
public class DeviceService {

  private C2monCache<Device> deviceCacheRef;

  @Autowired
  public DeviceService(final C2monCache<Device> deviceCacheRef) {
    this.deviceCacheRef = deviceCacheRef;
  }
}
