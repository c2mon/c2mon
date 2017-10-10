package cern.c2mon.server.cache.device;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.server.common.device.Device;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class DeviceService {

  private final Cache<Long, Device> deviceCacheRef;

  @Autowired
  public DeviceService(final Cache<Long, Device> deviceCacheRef) {
    this.deviceCacheRef = deviceCacheRef;
  }
}
