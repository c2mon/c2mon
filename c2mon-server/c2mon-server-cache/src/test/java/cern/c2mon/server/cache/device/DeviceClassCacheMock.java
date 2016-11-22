package cern.c2mon.server.cache.device;

import cern.c2mon.server.cache.DeviceClassCache;
import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class DeviceClassCacheMock {

  @Bean
  public DeviceClassCache deviceClassCache() {
    return EasyMock.createMock(DeviceClassCache.class);
  }
}
