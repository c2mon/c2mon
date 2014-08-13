/**
 *
 */
package cern.c2mon.client.ext.device;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.cache.BasicCacheHandler;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.ext.device.cache.DeviceCache;
import cern.c2mon.client.ext.device.request.DeviceRequestHandler;
import cern.c2mon.client.ext.device.util.DeviceTestUtils;
import cern.c2mon.shared.client.device.DeviceClassNameResponse;
import cern.c2mon.shared.client.device.DeviceClassNameResponseImpl;
import cern.c2mon.shared.client.device.TransferDevice;
import cern.c2mon.shared.client.device.TransferDeviceImpl;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/client/ext/device/config/c2mon-devicemanager-test.xml" })
public class DeviceManagerTest {

  /** Component to test */
  @Autowired
  private DeviceManager deviceManager;

  /** Mocked components */
  @Autowired
  private C2monTagManager tagManagerMock;

  @Autowired
  private DeviceCache deviceCacheMock;

  @Autowired
  private BasicCacheHandler dataTagCacheMock;

  @Autowired
  private DeviceRequestHandler requestHandlerMock;

  @Test
  public void testGetAllDeviceClassNames() throws JMSException {
    // Reset the mock
    reset(requestHandlerMock);

    List<DeviceClassNameResponse> deviceClassNamesReturnMap = new ArrayList<DeviceClassNameResponse>();
    deviceClassNamesReturnMap.add(new DeviceClassNameResponseImpl("test_device_class_1"));
    deviceClassNamesReturnMap.add(new DeviceClassNameResponseImpl("test_device_class_2"));

    // Expect the device manager to query the server
    expect(requestHandlerMock.getAllDeviceClassNames()).andReturn(deviceClassNamesReturnMap).once();

    // Setup is finished, need to activate the mock
    replay(requestHandlerMock);

    List<String> deviceClassNames = deviceManager.getAllDeviceClassNames();
    Assert.assertNotNull(deviceClassNames);
    Assert.assertTrue(deviceClassNames.get(0) == deviceClassNamesReturnMap.get(0).getDeviceClassName());
    Assert.assertTrue(deviceClassNames.get(1) == deviceClassNamesReturnMap.get(1).getDeviceClassName());

    // Verify that everything happened as expected
    verify(requestHandlerMock);
  }

  @Test
  public void testGetAllDevices() throws JMSException {
    // Reset the mock
    EasyMock.reset(tagManagerMock, deviceCacheMock, dataTagCacheMock);
    reset(requestHandlerMock);

    List<TransferDevice> devicesReturnList = new ArrayList<TransferDevice>();
    final TransferDeviceImpl device1 = new TransferDeviceImpl(1000L, "test_device_1", 1L);
    final TransferDeviceImpl device2 = new TransferDeviceImpl(1000L, "test_device_2", 1L);
    devicesReturnList.add(device1);
    devicesReturnList.add(device2);

    // Expect the device manager to check the cache
    EasyMock.expect(deviceCacheMock.getAllDevices("test_device_class")).andReturn(new ArrayList<Device>());
    // Expect the device manager to retrieve the devices
    expect(requestHandlerMock.getAllDevices(EasyMock.<String> anyObject())).andReturn(devicesReturnList);
    // Expect the device manager to add the devices to the cache
    deviceCacheMock.add(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall().times(2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock, deviceCacheMock, dataTagCacheMock);
    replay(requestHandlerMock);

    List<Device> devices = deviceManager.getAllDevices("test_device_class");
    Assert.assertNotNull(devices);

    for (Device device : devices) {
      Assert.assertTrue(device.getDeviceClassName().equals("test_device_class"));
    }

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock, deviceCacheMock, dataTagCacheMock);
    verify(requestHandlerMock);
  }

  @Test
  public void testSubscribeDevice() {

    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(200000L);

    Map<String, ClientDataTagValue> propertyValues = new HashMap<String, ClientDataTagValue>();
    propertyValues.put("test_property_name_1", cdt1);
    propertyValues.put("test_property_name_2", cdt2);

    final DeviceImpl device1 = new DeviceImpl(1L, "test_device", 1L, "test_device_class", tagManagerMock);
    device1.setPropertyValues(propertyValues);

    Map<Long, ClientDataTag> cacheReturnMap = getCacheReturnMap();

    // Expect the tag manager to subscribe to the tags
    EasyMock.expect(tagManagerMock.subscribeDataTags(EasyMock.<Set<Long>> anyObject(), EasyMock.<DataTagUpdateListener> anyObject())).andReturn(true).once();
    // Expect the device to get the tags from the cache
    // EasyMock.expect(dataTagCacheMock.get(EasyMock.<Set<Long>>
    // anyObject())).andReturn(cacheReturnMap).once();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock, deviceCacheMock, dataTagCacheMock);

    TestDeviceUpdateListener listener = new TestDeviceUpdateListener();
    deviceManager.subscribeDevices(new HashSet<Device>() {
      {
        add(device1);
      }
    }, listener);

    // Simulate the tag update calls
    for (ClientDataTagValue tag : cacheReturnMap.values()) {
      device1.onUpdate(tag);
    }

    listener.await(5000L);
    Device device = listener.getDevice();
    Assert.assertNotNull(device);
    Assert.assertTrue(device.getId() == device1.getId());

    Map<String, ClientDataTagValue> values = device.getPropertyValues();
    Assert.assertTrue(values.get("test_property_name_1").getId().equals(100000L));
    Assert.assertTrue(values.get("test_property_name_2").getId().equals(200000L));

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock, deviceCacheMock, dataTagCacheMock);
    // Reset the mock
    EasyMock.reset(tagManagerMock, deviceCacheMock, dataTagCacheMock);
  }

  @Test
  public void testSubscribeLazyDevice() {

    Map<String, Long> sparsePropertyMap = new HashMap<String, Long>();
    sparsePropertyMap.put("test_property_name_1", 100000L);
    sparsePropertyMap.put("test_property_name_2", 200000L);

    final DeviceImpl device1 = new DeviceImpl(1L, "test_device", 1L, "test_device_class", tagManagerMock);
    device1.setPropertyValues(sparsePropertyMap);

    Map<Long, ClientDataTag> cacheReturnMap = getCacheReturnMap();

    // Expect the device to not call getDataTags() but instead to
    // get the tags from the cache
    // EasyMock.expect(dataTagCacheMock.get(EasyMock.<Set<Long>>
    // anyObject())).andReturn(cacheReturnMap).once();
    // Expect the tag manager to subscribe to the tags
    EasyMock.expect(tagManagerMock.subscribeDataTags(EasyMock.<Set<Long>> anyObject(), EasyMock.<DataTagUpdateListener> anyObject())).andReturn(true).once();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock, deviceCacheMock, dataTagCacheMock);

    TestDeviceUpdateListener listener = new TestDeviceUpdateListener();
    deviceManager.subscribeDevices(new HashSet<Device>() {
      {
        add(device1);
      }
    }, listener);

    // Simulate the tag update calls
    for (ClientDataTagValue tag : cacheReturnMap.values()) {
      device1.onUpdate(tag);
    }

    listener.await(5000L);
    Device device = listener.getDevice();
    Assert.assertNotNull(device);
    Assert.assertTrue(device.getId() == device1.getId());

    Map<String, ClientDataTagValue> values = device.getPropertyValues();
    Assert.assertTrue(values.get("test_property_name_1").getId().equals(100000L));
    Assert.assertTrue(values.get("test_property_name_2").getId().equals(200000L));

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock, deviceCacheMock, dataTagCacheMock);
    // Reset the mock
    EasyMock.reset(tagManagerMock, deviceCacheMock, dataTagCacheMock);
  }

  @Test
  public void testUnsubscribeDevices() {
    // Reset the mock
    EasyMock.reset(tagManagerMock, deviceCacheMock, dataTagCacheMock);

    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(200000L);

    Map<String, ClientDataTagValue> propertyValues = new HashMap<String, ClientDataTagValue>();
    propertyValues.put("test_property_name_1", cdt1);
    propertyValues.put("test_property_name_2", cdt2);

    final DeviceImpl device1 = new DeviceImpl(1L, "test_device", 1L, "test_device_class", tagManagerMock);
    device1.setPropertyValues(propertyValues);

    Map<Long, ClientDataTag> cacheReturnMap = getCacheReturnMap();

    // Expect the tag manager to subscribe to the tags
    EasyMock.expect(tagManagerMock.subscribeDataTags(EasyMock.<Set<Long>> anyObject(), EasyMock.<DataTagUpdateListener> anyObject())).andReturn(true).times(2);
    // Expect the device to get the tags from the cache
    // EasyMock.expect(dataTagCacheMock.get(EasyMock.<Set<Long>>
    // anyObject())).andReturn(cacheReturnMap).times(2);
    // Expect the device manager to unsubscribe the tags
    tagManagerMock.unsubscribeDataTags(EasyMock.<Set<Long>> anyObject(), EasyMock.<DataTagUpdateListener> anyObject());
    EasyMock.expectLastCall().times(2);
    // Expect the device to be removed from the cache
    deviceCacheMock.remove(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall().once();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock, deviceCacheMock, dataTagCacheMock);

    TestDeviceUpdateListener listener1 = new TestDeviceUpdateListener();
    TestDeviceUpdateListener listener2 = new TestDeviceUpdateListener();

    Set<Device> devices = new HashSet<Device>();
    devices.add(device1);

    // Subscribe multiple listeners
    deviceManager.subscribeDevices(devices, listener1);
    deviceManager.subscribeDevices(devices, listener2);

    // Remove a listener
    deviceManager.unsubscribeDevices(devices, listener1);
    Assert.assertFalse(device1.getDeviceUpdateListeners().contains(listener1));
    Assert.assertTrue(device1.getDeviceUpdateListeners().contains(listener2));

    // Remove another listener
    deviceManager.unsubscribeDevices(devices, listener2);
    Assert.assertFalse(device1.getDeviceUpdateListeners().contains(listener2));

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock, deviceCacheMock, dataTagCacheMock);
  }

  @Test
  public void testUnsubscribeAllDevices() {
    // Reset the mock
    EasyMock.reset(tagManagerMock, deviceCacheMock, dataTagCacheMock);

    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(200000L);
    Map<String, ClientDataTagValue> propertyValues1 = new HashMap<String, ClientDataTagValue>();
    Map<String, ClientDataTagValue> propertyValues2 = new HashMap<String, ClientDataTagValue>();
    propertyValues1.put("test_property_name_1", cdt1);
    propertyValues2.put("test_property_name_2", cdt2);

    final DeviceImpl device1 = new DeviceImpl(1L, "test_device_1", 1L, "test_device_class", tagManagerMock);
    final DeviceImpl device2 = new DeviceImpl(2L, "test_device_2", 1L, "test_device_class", tagManagerMock);
    device1.setPropertyValues(propertyValues1);
    device2.setPropertyValues(propertyValues2);

    Set<Device> devices = new HashSet<Device>();
    devices.add(device1);
    devices.add(device2);

    Map<Long, ClientDataTag> cacheReturnMap = getCacheReturnMap();

    // Expect the tag manager to subscribe to the tags
    EasyMock.expect(tagManagerMock.subscribeDataTags(EasyMock.<Set<Long>> anyObject(), EasyMock.<DataTagUpdateListener> anyObject())).andReturn(true).times(2);
    // Expect the device manager to get the tags from the cache
    // EasyMock.expect(dataTagCacheMock.get(EasyMock.<Set<Long>>
    // anyObject())).andReturn(cacheReturnMap).times(2);
    // Expect the device manager to get all cached devices
    EasyMock.expect(deviceCacheMock.getAllDevices()).andReturn(new ArrayList<Device>(devices)).once();
    // Expect the device manager to unsubscribe the tags
    tagManagerMock.unsubscribeDataTags(EasyMock.<Set<Long>> anyObject(), EasyMock.<DataTagUpdateListener> anyObject());
    EasyMock.expectLastCall().times(2);
    // Expect the devices to be removed from the cache
    deviceCacheMock.remove(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall().times(2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagManagerMock, deviceCacheMock, dataTagCacheMock);

    TestDeviceUpdateListener listener = new TestDeviceUpdateListener();
    deviceManager.subscribeDevices(devices, listener);
    deviceManager.unsubscribeAllDevices(listener);

    Assert.assertFalse(device1.getDeviceUpdateListeners().contains(listener));
    Assert.assertFalse(device2.getDeviceUpdateListeners().contains(listener));

    // Verify that everything happened as expected
    EasyMock.verify(tagManagerMock, deviceCacheMock, dataTagCacheMock);
  }

  class TestDeviceUpdateListener implements DeviceUpdateListener {

    final CountDownLatch latch = new CountDownLatch(1);
    Device device;

    @Override
    public void onUpdate(Device device, String propertyValueName) {
      this.device = device;
      latch.countDown();
    }

    public void await(Long timeout) {
      try {
        latch.await(5000, TimeUnit.MILLISECONDS);
        System.out.println("latch released");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    public Device getDevice() {
      return device;
    }
  }

  private Map<Long, ClientDataTag> getCacheReturnMap() {
    Map<Long, ClientDataTag> cacheReturnMap = new HashMap<Long, ClientDataTag>();
    ClientDataTagImpl cdt1 = new ClientDataTagImpl(100000L);
    ClientDataTagImpl cdt2 = new ClientDataTagImpl(200000L);

    TagUpdate tu1 = DeviceTestUtils.createValidTransferTag(cdt1.getId(), "test_tag_name_1");
    TagUpdate tu2 = DeviceTestUtils.createValidTransferTag(cdt2.getId(), "test_tag_name_2");

    try {
      cdt1.update(tu1);
      cdt2.update(tu2);
    } catch (RuleFormatException e1) {
      e1.printStackTrace();
    }

    cacheReturnMap.put(cdt1.getId(), cdt1);
    cacheReturnMap.put(cdt2.getId(), cdt2);
    return cacheReturnMap;
  }
}
