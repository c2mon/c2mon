/**
 *
 */
package cern.c2mon.client.ext.device.cache;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.ext.device.Device;
import cern.c2mon.client.ext.device.DeviceImpl;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/client/ext/device/config/c2mon-devicecache-test.xml" })
public class DeviceCacheImplTest {

  /** Component to test */
  @Autowired
  private DeviceCache deviceCache;

  private static Device device1;
  private static Device device2;
  private static Device device3;

  @BeforeClass
  public static void setUp() {
    device1 = new DeviceImpl(1000L, "test_device", 1L, "test_device_class_1", null);
    device2 = new DeviceImpl(2000L, "test_device", 1L, "test_device_class_1", null);
    device3 = new DeviceImpl(3000L, "test_device", 1L, "test_device_class_2", null);
  }

  @Test
  public void testAddDevice() {
    deviceCache.add(device1);
    deviceCache.add(device2);
    Assert.assertTrue(deviceCache.get(device1.getId()) != null);
    Assert.assertTrue(deviceCache.get(device2.getId()) != null);
  }

  @Test
  public void testRemoveDevice() {
    deviceCache.add(device1);
    deviceCache.remove(device1);
    Assert.assertTrue(deviceCache.get(device1.getId()) == null);
  }

  @Test
  public void testGetDevice() {
    deviceCache.add(device1);
    deviceCache.add(device2);
    Assert.assertTrue(deviceCache.get(device1.getId()) == device1);
    Assert.assertTrue(deviceCache.get(device2.getId()) == device2);
  }

  @Test
  public void testGetAllDevicesOfClass() {
    deviceCache.add(device1);
    deviceCache.add(device2);
    deviceCache.add(device3);
    List<Device> devices = deviceCache.getAllDevices("test_device_class_1");
    Assert.assertTrue(devices.size() == 2);
    Assert.assertTrue(devices.contains(device1));
    Assert.assertTrue(devices.contains(device2));

    devices = deviceCache.getAllDevices("test_device_class_2");
    Assert.assertTrue(devices.size() == 1);
    Assert.assertTrue(devices.contains(device3));
  }

  @Test
  public void testGetAllDevices() {
    deviceCache.add(device1);
    deviceCache.add(device2);
    deviceCache.add(device3);
    List<Device> devices = deviceCache.getAllDevices();
    Assert.assertTrue(devices.size() == 3);
    Assert.assertTrue(devices.contains(device1));
    Assert.assertTrue(devices.contains(device2));
    Assert.assertTrue(devices.contains(device3));
  }
}
