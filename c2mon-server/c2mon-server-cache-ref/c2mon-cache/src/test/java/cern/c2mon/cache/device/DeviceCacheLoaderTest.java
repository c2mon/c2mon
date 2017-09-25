package cern.c2mon.cache.device;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
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
  private C2monCache<Long, Device> deviceCacheRef;

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

    Set<Long> keySet = deviceList.stream().map(Device::getId).collect(Collectors.toSet());
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

    Iterator<Device> it = deviceList.iterator();
    while (it.hasNext()) {
      Device currentDevice = it.next();
      // Equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached Device should have the same name as in DB",
              currentDevice.getName(), (deviceCacheRef.get(currentDevice.getId()).getName()));
    }
  }
}
