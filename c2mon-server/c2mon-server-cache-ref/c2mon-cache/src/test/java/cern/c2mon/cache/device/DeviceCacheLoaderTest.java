package cern.c2mon.cache.device;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.Cache;
import cern.c2mon.server.cache.dbaccess.DeviceMapper;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.shared.client.device.DeviceProperty;

import static cern.c2mon.server.test.device.ObjectComparison.assertDevicePropertyListContains;
import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class DeviceCacheLoaderTest extends AbstractCacheLoaderTest {

  @Autowired
  private Cache<Long, Device> deviceCacheRef;

  @Autowired
  private DeviceMapper deviceMapper;

  @Before
  public void init() {
    deviceCacheRef.init();
  }

  @Test
  public void preloadCache() throws ClassNotFoundException {
    assertNotNull("Device Cache should not be null", deviceCacheRef);

    List<Device> deviceList = deviceMapper.getAll();

    assertTrue("List of devices should not be empty", deviceList.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", deviceList.size(), deviceCacheRef.getKeys().size());
    // Compare all the objects from the cache and buffer
    for (Device device : deviceList) {
      Device fromCache = deviceCacheRef.get(device.getId());

      assertEquals("Cached Device should have the same name as in DB", device.getName(), fromCache.getName());

      // Compare properties
      for (DeviceProperty property : device.getDeviceProperties()) {
        assertDevicePropertyListContains(fromCache.getDeviceProperties(), property);
      }
    }

    for (Device currentDevice : deviceList) {
      // Equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached Device should have the same name as in DB",
              currentDevice.getName(), (deviceCacheRef.get(currentDevice.getId()).getName()));
    }
  }
}
