package cern.c2mon.cache.config.device;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.AbstractCacheLoaderTest;
import cern.c2mon.server.cache.dbaccess.DeviceMapper;
import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.shared.client.device.DeviceProperty;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static cern.c2mon.server.test.device.ObjectComparison.assertDevicePropertyListContains;
import static org.junit.Assert.assertEquals;

/**
 * @author Alexandros Papageorgiou
 */
public class DeviceCacheLoaderTest extends AbstractCacheLoaderTest<Device> {

  @Inject
  private C2monCache<Device> deviceCacheRef;

  @Inject
  private DeviceMapper deviceMapper;

  @Override
  protected LoaderMapper<Device> getMapper() {
    return deviceMapper;
  }

  @Override
  protected void customCompare(List<Device> mapperList, Map<Long, Device> cacheList) throws ClassNotFoundException {
    for (Device device : mapperList) {
      Device fromCache = cacheList.get(device.getId());

      assertEquals("Cached Device should have the same name as in DB", device.getName(), fromCache.getName());

      // Compare properties
      for (DeviceProperty property : device.getDeviceProperties()) {
        assertDevicePropertyListContains(fromCache.getDeviceProperties(), property);
      }
    }

    for (Device currentDevice : mapperList) {
      // Equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached Device should have the same name as in DB",
        currentDevice.getName(), (deviceCacheRef.get(currentDevice.getId()).getName()));
    }
  }

  @Override
  protected Device getSample() {
    return new DeviceCacheObject();
  }

  @Override
  protected Long getExistingKey() {
    return 300L;
  }

  @Override
  protected C2monCache<Device> getCache() {
    return deviceCacheRef;
  }
}
