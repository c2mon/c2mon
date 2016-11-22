package cern.c2mon.server.cache.device;

import cern.c2mon.server.cache.DeviceCache;
import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class DeviceCacheMock {

  @Bean
  public DeviceCache deviceCache() {
    return EasyMock.createMock(DeviceCache.class);
  }
}
