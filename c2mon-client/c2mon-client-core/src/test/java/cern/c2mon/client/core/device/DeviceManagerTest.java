/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.core.device;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.CommandTag;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.cache.BasicCacheHandler;
import cern.c2mon.client.core.config.mock.JmsProxyMock;
import cern.c2mon.client.core.device.cache.DeviceCache;
import cern.c2mon.client.core.device.config.DeviceManagerTestConfig;
import cern.c2mon.client.core.device.exception.DeviceNotFoundException;
import cern.c2mon.client.core.device.listener.DeviceInfoUpdateListener;
import cern.c2mon.client.core.device.property.*;
import cern.c2mon.client.core.device.property.Property;
import cern.c2mon.client.core.device.request.DeviceRequestHandler;
import cern.c2mon.client.core.device.util.DeviceTestUtils;
import cern.c2mon.client.core.service.CommandService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.core.tag.CommandTagImpl;
import cern.c2mon.client.core.tag.TagController;
import cern.c2mon.client.core.tag.TagImpl;
import cern.c2mon.shared.client.device.*;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = "c2mon.client.device.test.mock=true")
@ContextConfiguration(classes = {
    DeviceManagerTestConfig.class,
    JmsProxyMock.class,
})
@DirtiesContext
@Slf4j
public class DeviceManagerTest {

  /** Component to test */
  @Autowired
  private DeviceManager deviceManager;

  /** Mocked components */
  @Autowired
  private TagService tagServiceMock;

  @Autowired
  private CommandService commandManagerMock;

  @Autowired
  private DeviceCache deviceCacheMock;

  @Autowired
  private BasicCacheHandler dataTagCacheMock;

  @Autowired
  private DeviceRequestHandler requestHandlerMock;

  @Test
  public void testGetAllDeviceClassNames() throws JMSException {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    List<DeviceClassNameResponse> deviceClassNamesReturnMap = new ArrayList<>();
    deviceClassNamesReturnMap.add(new DeviceClassNameResponseImpl("test_device_class_1"));
    deviceClassNamesReturnMap.add(new DeviceClassNameResponseImpl("test_device_class_2"));

    // Expect the device manager to query the server
    expect(requestHandlerMock.getAllDeviceClassNames()).andReturn(deviceClassNamesReturnMap).once();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    List<String> deviceClassNames = deviceManager.getAllDeviceClassNames();
    Assert.assertNotNull(deviceClassNames);
    Assert.assertTrue(deviceClassNames.get(0) == deviceClassNamesReturnMap.get(0).getDeviceClassName());
    Assert.assertTrue(deviceClassNames.get(1) == deviceClassNamesReturnMap.get(1).getDeviceClassName());

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  @Test
  public void testGetDevice() throws DeviceNotFoundException, JMSException {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    TransferDevice transferDevice = new TransferDeviceImpl(1000L, "test_device", 1L, "test_class");

    // Expectations
    EasyMock.expect(requestHandlerMock.getDevices(EasyMock.<Set<DeviceInfo>> anyObject())).andReturn(Arrays.asList(transferDevice));
    final Capture<Device> capturedDevice = new Capture<>();
    deviceCacheMock.add(EasyMock.capture(capturedDevice));
    EasyMock.expectLastCall();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    // Run the actual code to test
    DeviceInfo info = new DeviceInfo("test_class", "test_device");
    Device device = deviceManager.getDevice(info);

    Assert.assertTrue(device.getDeviceClassName().equals("test_class"));
    Assert.assertTrue(device.getName().equals("test_device"));

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  @Test
  public void testGetUnknownDevice() throws DeviceNotFoundException, JMSException {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    // Expectations
    EasyMock.expect(requestHandlerMock.getDevices(EasyMock.<Set<DeviceInfo>> anyObject())).andReturn(new ArrayList<TransferDevice>());

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    // Run the actual code to test
    // Test retrieving an unknown device - should throw exception
    try {
      deviceManager.getDevice(new DeviceInfo("unknown", "unknown"));
      Assert.fail("Exception not thrown");
    } catch (DeviceNotFoundException e) {
    }

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  @Test
  public void testGetAllDevices() throws JMSException {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    List<TransferDevice> devicesReturnList = new ArrayList<>();
    final TransferDeviceImpl device1 = new TransferDeviceImpl(1000L, "test_device_1", 1L, "test_device_class");
    final TransferDeviceImpl device2 = new TransferDeviceImpl(1000L, "test_device_2", 1L, "test_device_class");
    device1.addDeviceProperty(new DeviceProperty(1L, "TEST_PROPERTY_1", "100430", "tagId", null));
    device2.addDeviceProperty(new DeviceProperty(2L, "TEST_PROPERTY_2", "100431", "tagId", null));
    device1.addDeviceCommand(new DeviceCommand(1L, "TEST_COMMAND_1", "4287", "commandTagId", null));
    device2.addDeviceCommand(new DeviceCommand(2L, "TEST_COMMAND_2", "4288", "commandTagId", null));
    devicesReturnList.add(device1);
    devicesReturnList.add(device2);

    Set<CommandTag<Object>> returnedCommandTags = new HashSet<>();
    returnedCommandTags.add(new CommandTagImpl(4287L));
    returnedCommandTags.add(new CommandTagImpl(4288L));

    // Expect the device manager to check the cache
    EasyMock.expect(deviceCacheMock.getAllDevices("test_device_class")).andReturn(new ArrayList<Device>());
    // Expect the device manager to retrieve the devices
    expect(requestHandlerMock.getAllDevices(EasyMock.<String> anyObject())).andReturn(devicesReturnList);
    // Expect the device manager to get the command tags
    EasyMock.expect(commandManagerMock.getCommandTags(EasyMock.<Set<Long>> anyObject())).andReturn(returnedCommandTags);
    // Expect the device manager to add the devices to the cache
    deviceCacheMock.add(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall().times(2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    List<Device> devices = deviceManager.getAllDevices("test_device_class");
    Assert.assertNotNull(devices);
    Assert.assertTrue(devices.size() == 2);

    for (Device device : devices) {
      Assert.assertTrue(device.getDeviceClassName().equals("test_device_class"));
      Assert.assertTrue(device.getProperties().size() == 1);
      Assert.assertTrue(device.getCommands().size() == 1);
    }

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  @Test
  public void testSubscribeDevice() {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    TagImpl cdt1 = new TagImpl(100000L);
    TagImpl cdt2 = new TagImpl(200000L);

    Map<String, Tag> deviceProperties = new HashMap<>();
    deviceProperties.put("test_property_name_1", cdt1);
    deviceProperties.put("test_property_name_2", cdt2);

    final DeviceImpl device1 = new DeviceImpl(1L, "test_device", 1L, "test_device_class");
    device1.setDeviceProperties(deviceProperties);

    final Map<Long, Tag> cacheReturnMap = getCacheReturnMap();

    // Expect the tag manager to subscribe to the tags
    tagServiceMock.subscribe(EasyMock.<Set<Long>> anyObject(), EasyMock.<TagListener> anyObject());
    EasyMock.expectLastCall().andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        deviceManager.onInitialUpdate(cacheReturnMap.values());
        return true;
      }
    }).once();

    // Expect the device manager to get the devices from the cache - once to
    // call onInitialUpdate() and once to call onUpdate()
    EasyMock.expect(deviceCacheMock.getAllDevices()).andReturn(Arrays.asList((Device) device1)).times(3);
    // Expect the device manager to add the devices to the cache
    deviceCacheMock.add(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    TestDeviceUpdateListener listener = new TestDeviceUpdateListener();
    deviceManager.subscribeDevice(device1, listener);

    // Simulate the tag update calls
/*    new Thread(new Runnable() {
      @Override
      public void run() {*/
        for (Tag tag : cacheReturnMap.values()) {
          deviceManager.onUpdate(tag);
        }
/*      }
    }).start();*/

    listener.await(5000L);
    Device device = listener.getDevice();
    Assert.assertNotNull(device);
    Assert.assertTrue(device.getId() == device1.getId());

    for (Property property : device1.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagServiceMock);
    }

    Assert.assertTrue(device.getProperty("test_property_name_1").getTag().getId().equals(100000L));
    Assert.assertTrue(device.getProperty("test_property_name_2").getTag().getId().equals(200000L));

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  @Test
  public void testSubscribeLazyDevice() throws RuleFormatException, ClassNotFoundException {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    List<DeviceProperty> sparsePropertyMap = new ArrayList<>();
    sparsePropertyMap.add(new DeviceProperty(1L, "test_property_name_1", "100000", "tagId", null));
    sparsePropertyMap.add(new DeviceProperty(2L, "test_property_name_2", "200000", "tagId", null));

    final DeviceImpl device1 = new DeviceImpl(1L, "test_device", 1L, "test_device_class");
    device1.setDeviceProperties(sparsePropertyMap);

    final Map<Long, Tag> cacheReturnMap = getCacheReturnMap();

    // Expect the device to not call getDataTags() but instead to
    // get the tags from the cache
    // EasyMock.expect(dataTagCacheMock.get(EasyMock.<Set<Long>>
    // anyObject())).andReturn(cacheReturnMap).once();

    // Expect the tag manager to subscribe to the tags
    tagServiceMock.subscribe(EasyMock.<Set<Long>> anyObject(), EasyMock.<TagListener> anyObject());
    EasyMock.expectLastCall().andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        deviceManager.onInitialUpdate(cacheReturnMap.values());
        return true;
      }
    }).once();

    // Expect the device manager to get the devices from the cache - once to
    // call onInitialUpdate() and once to call onUpdate()
    EasyMock.expect(deviceCacheMock.getAllDevices()).andReturn(Arrays.asList((Device) device1)).times(2);
    // Expect the device manager to add the devices to the cache
    deviceCacheMock.add(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    TestDeviceUpdateListener listener = new TestDeviceUpdateListener();
    deviceManager.subscribeDevice(device1, listener);

    // Update a property
    deviceManager.onUpdate(cacheReturnMap.get(100000L));

    listener.await(5000L);
    Device device = listener.getDevice();
    Assert.assertNotNull(device);
    Assert.assertTrue(device.getId() == device1.getId());

    // Check all properties are subscribed to at this point
    Assert.assertTrue(areAllPropertiesAndFieldsSubscribed(listener.getDevices()));

    for (Property property : device.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagServiceMock);
    }

    Assert.assertTrue(device.getProperty("test_property_name_1").getTag().getId().equals(100000L));
    Assert.assertTrue(device.getProperty("test_property_name_2").getTag().getId().equals(200000L));

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  @Test
  public void testSubscribeDeviceWithFields() throws RuleFormatException {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    Map<String, Field> deviceFields = new HashMap<>();

    for (int i = 0; i < 1000; i++) {
      deviceFields.put("test_field_name_" + i, new FieldImpl("test_field_name_" + i, Category.DATATAG, new Long(i)));
    }

    HashMap<String, Property> deviceProperties = new HashMap<>();
    deviceProperties.put("test_property_name", new PropertyImpl("test_property_name", Category.MAPPED_PROPERTY, deviceFields));

    final DeviceImpl device1 = new DeviceImpl(1L, "test_device", 1L, "test_device_class");
    device1.setDeviceProperties(deviceProperties);

    final Map<Long, Tag> cacheReturnMap = new HashMap<>();

    for (int i = 0; i < 1000; i++) {
      TagController cdt = new TagController(Long.valueOf(i));
      cdt.update(DeviceTestUtils.createValidTransferTag(Long.valueOf(i), "test_tag_name_" + i, "test_value_" + i));
      cacheReturnMap.put(Long.valueOf(i), cdt.getTagImpl());
    }

    // Expect the tag manager to subscribe to the tags
    tagServiceMock.subscribe(EasyMock.<Set<Long>> anyObject(), EasyMock.<TagListener> anyObject());
    EasyMock.expectLastCall().andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        deviceManager.onInitialUpdate(cacheReturnMap.values());
        return true;
      }
    }).once();

    // Expect the device manager to get the devices from the cache - once to
    // call onInitialUpdate() and once to call onUpdate()
    EasyMock.expect(deviceCacheMock.getAllDevices()).andReturn(Arrays.asList((Device) device1)).times(2);
    // Expect the device manager to add the devices to the cache
    deviceCacheMock.add(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    TestDeviceUpdateListener listener = new TestDeviceUpdateListener();
    deviceManager.subscribeDevices(new HashSet<Device>(Arrays.asList(device1)), listener);

    // Update a property
    deviceManager.onUpdate(cacheReturnMap.get(0L));

    listener.await(5000L);
    Device device = listener.getDevice();
    Assert.assertNotNull(device);
    Assert.assertTrue(device.getId() == device1.getId());

    Assert.assertTrue(areAllPropertiesAndFieldsSubscribed(listener.getDevices()));

    Property propertyWithFields = device.getProperty("test_property_name");
    ((PropertyImpl) propertyWithFields).setTagManager(tagServiceMock);

    Assert.assertTrue(propertyWithFields.getCategory().equals(Category.MAPPED_PROPERTY));
    for (int i = 0; i < 1000; i++) {
      Tag tag = propertyWithFields.getField("test_field_name_" + i).getTag();
      Assert.assertTrue(tag.getId().equals(new Long(i)));
      Assert.assertTrue(tag.getValue() != null);
      Assert.assertTrue(tag.getValue().equals("test_value_" + i));
    }

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  @Test
  public void testSubscribeDeviceByName() throws JMSException, ClassNotFoundException, RuleFormatException {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    List<TransferDevice> devicesReturnList = new ArrayList<>();
    final TransferDeviceImpl transferDevice = new TransferDeviceImpl(1000L, "test_device_1", 1L, "test_device_class");
    transferDevice.addDeviceProperty(new DeviceProperty(1L, "TEST_PROPERTY_1", "100430", "tagId", null));
    transferDevice.addDeviceCommand(new DeviceCommand(1L, "TEST_COMMAND_1", "4287", "commandTagId", null));
    devicesReturnList.add(transferDevice);

    Set<CommandTag<Object>> returnedCommandTags = new HashSet<>();
    returnedCommandTags.add(new CommandTagImpl<>(-1L));

    // Expect the device manager to check the cache
    EasyMock.expect(deviceCacheMock.get(EasyMock.<String> anyObject())).andReturn(null);
    // Expect the device manager to retrieve the devices
    expect(requestHandlerMock.getDevices(EasyMock.<Set<DeviceInfo>> anyObject())).andReturn(devicesReturnList);
    // Expect the device manager to get the command tag
    EasyMock.expect(commandManagerMock.getCommandTags(EasyMock.<Set<Long>> anyObject())).andReturn(returnedCommandTags).times(1);
    // Expect the device manager to add the device to the cache
    final Capture<Device> capturedDevice = new Capture<>();
    deviceCacheMock.add(EasyMock.capture(capturedDevice));
    EasyMock.expectLastCall();

    final TestDeviceUpdateListener listener = new TestDeviceUpdateListener();

    // Expect the device manager to subscribe to the tag
    tagServiceMock.subscribe(EasyMock.<Set<Long>> anyObject(), EasyMock.<TagListener> anyObject());
    EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Boolean answer() throws Throwable {
        deviceManager.onInitialUpdate(Arrays.asList((Tag) new TagImpl(100430L)));
        return true;
      }
    }).once();

    // Expect the device manager to get the devices from the cache - once to
    // call onInitialUpdate() and once to call onUpdate()
    EasyMock.expect(deviceCacheMock.getAllDevices()).andAnswer(new IAnswer<List<Device>>() {
      @Override
      public List<Device> answer() throws Throwable {
        return Arrays.asList(capturedDevice.getValue());
      }
    }).times(2);

    // Expect the device manager to add the devices to the cache
    deviceCacheMock.add(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    // Run the actual code to be tested
    deviceManager.subscribeDevice(new DeviceInfo("test_device_class", "test_device_1"), listener);

    // Simulate the tag update calls
/*    new Thread(new Runnable() {
      @Override
      public void run() {*/
        deviceManager.onUpdate(new TagImpl(100430L));
/*      }
    }).start();*/

    listener.await(5000L);
    Device device2 = listener.getDevice();
    Assert.assertNotNull(device2);
    Assert.assertTrue(device2.getId() == listener.getDevice().getId());

    for (Property property : device2.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagServiceMock);
    }

    Assert.assertTrue(listener.getDevice().getProperty("TEST_PROPERTY_1").getTag().getId().equals(100430L));

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  @Test
  public void testSubscribeNonexistentDeviceByName() throws JMSException, InterruptedException {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    // Expect the device manager to check the cache
    EasyMock.expect(deviceCacheMock.get(EasyMock.<String> anyObject())).andReturn(null);
    // Expect the device manager to retrieve the devices
    expect(requestHandlerMock.getDevices(EasyMock.<Set<DeviceInfo>> anyObject())).andReturn(new ArrayList<TransferDevice>());

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    // Attempt to get a nonexistent device
    final CountDownLatch latch = new CountDownLatch(1);
    deviceManager.subscribeDevice(new DeviceInfo("nonexistent", "nonexistent"), new DeviceInfoUpdateListener() {
      @Override
      public void onUpdate(Device device, PropertyInfo propertyInfo) {
      }

      @Override
      public void onInitialUpdate(List<Device> devices) {
      }

      @Override
      public void onDevicesNotFound(List<DeviceInfo> unknownDevices) {
        latch.countDown();
      }
    });

    latch.await(1000, TimeUnit.MILLISECONDS);
    Assert.assertTrue(latch.getCount() == 0);

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  @Test
  public void testSubscribeDevicesByName() throws JMSException {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    List<TransferDevice> devicesReturnList = new ArrayList<>();
    final TransferDeviceImpl transferDevice = new TransferDeviceImpl(1000L, "test_device_1", 1L, "test_device_class");
    transferDevice.addDeviceProperty(new DeviceProperty(1L, "TEST_PROPERTY_1", "100430", "tagId", null));
    devicesReturnList.add(transferDevice);

    DeviceInfo known = new DeviceInfo("test_device_class", "test_device_1");
    DeviceInfo unknown = new DeviceInfo("test_device_class", "unknown_device");
    HashSet<DeviceInfo> infoList = new HashSet<>(Arrays.asList(known, unknown));

    // Expect the device manager to check the cache
    EasyMock.expect(deviceCacheMock.get(EasyMock.<String> anyObject())).andReturn(null).times(2);
    // Expect the device manager to retrieve the devices
    expect(requestHandlerMock.getDevices(EasyMock.<Set<DeviceInfo>> anyObject())).andReturn(devicesReturnList);
    // Expect the device manager to add the device to the cache
    final Capture<Device> capturedDevice = new Capture<>();
    deviceCacheMock.add(EasyMock.capture(capturedDevice));
    EasyMock.expectLastCall();

    final TestDeviceUpdateListener listener = new TestDeviceUpdateListener();
    // This time we want to make sure that onDevicesNotFound() is also called
    listener.setLatch(2);

    // Expect the device manager to subscribe to the tags
    tagServiceMock.subscribe(EasyMock.<Set<Long>> anyObject(), EasyMock.<TagListener> anyObject());
    EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Boolean answer() throws Throwable {
        deviceManager.onInitialUpdate(Arrays.asList((Tag) new TagImpl(100430L)));
        return true;
      }
    }).once();

    // Expect the device manager to get the devices from the cache - once to
    // call onInitialUpdate() and once to call onUpdate()
    EasyMock.expect(deviceCacheMock.getAllDevices()).andAnswer(new IAnswer<List<Device>>() {
      @Override
      public List<Device> answer() throws Throwable {
        return Arrays.asList(capturedDevice.getValue());
      }
    }).times(2);

    // Expect the device manager to add the devices to the cache
    deviceCacheMock.add(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    // Run the actual code to be tested
    deviceManager.subscribeDevices(infoList, listener);

    // Simulate the tag update calls
/*    new Thread(new Runnable() {
      @Override
      public void run() {*/
        deviceManager.onUpdate(new TagImpl(100430L));
/*      }
    }).start();*/

    listener.await(5000L);
    Device device2 = listener.getDevice();
    Assert.assertNotNull(device2);
    Assert.assertTrue(device2.getId() == listener.getDevice().getId());

    for (Property property : device2.getProperties()) {
      ((PropertyImpl) property).setTagManager(tagServiceMock);
    }

    Assert.assertTrue(listener.getDevice().getProperty("TEST_PROPERTY_1").getTag().getId().equals(100430L));

    Assert.assertTrue(listener.getUnknownDevices().size() == 1);

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  // @Test
  // public void testPropertyUpdate() throws RuleFormatException {
  // // Reset the mock
  // EasyMock.reset(tagManagerMock, deviceCacheMock, dataTagCacheMock,
  // commandManagerMock);
  // reset(requestHandlerMock);
  //
  // DeviceImpl device = new DeviceImpl(1000L, "test_device", 1L,
  // "test_device_class", tagManagerMock, commandManagerMock);
  // TagImpl cdt1 = new TagImpl(100000L);
  // cdt1.update(DeviceTestUtils.createValidTransferTag(cdt1.getId(),
  // "test_tag_name", 1L));
  //
  // Map<String, Tag> deviceProperties = new HashMap<String,
  // Tag>();
  // deviceProperties.put("test_property_name", cdt1);
  // device.setDeviceProperties(deviceProperties);
  //
  // // Update the tag value
  // TagImpl cdt2 = new TagImpl(100000L);
  // cdt2.update(DeviceTestUtils.createValidTransferTag(cdt1.getId(),
  // cdt1.getName(), 2L));
  // deviceManager.onUpdate(cdt2);
  //
  // // Check that the device stored the new update properly
  // Assert.assertTrue(((Long)
  // device.getProperty("test_property_name").getTag().getValue()) == 2L);
  // }

  @Test
  public void testUnsubscribeDevices() {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    TagImpl cdt1 = new TagImpl(100000L);
    TagImpl cdt2 = new TagImpl(200000L);

    final Map<String, Tag> deviceProperties = new HashMap<>();
    deviceProperties.put("test_property_name_1", cdt1);
    deviceProperties.put("test_property_name_2", cdt2);

    final DeviceImpl device1 = new DeviceImpl(11L, "test_device", 1L, "test_device_class");
    device1.setDeviceProperties(deviceProperties);

    final Map<Long, Tag> cacheReturnMap = getCacheReturnMap();

    // Expect the tag manager to subscribe to the tags
    tagServiceMock.subscribe(EasyMock.<Set<Long>> anyObject(), EasyMock.<TagListener> anyObject());
    EasyMock.expectLastCall().andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        deviceManager.onInitialUpdate(cacheReturnMap.values());
        return true;
      }
    }).times(2);

    // Expect the device manager to get the devices from the cache - once to
    // call onInitialUpdate() and once to call onUpdate()
    EasyMock.expect(deviceCacheMock.getAllDevices()).andReturn(Arrays.asList((Device) device1)).times(2);
    // Expect the device manager to add the devices to the cache
    deviceCacheMock.add(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall().times(2);

    // Expect the device manager to unsubscribe the tags
    tagServiceMock.unsubscribe(EasyMock.<Set<Long>> anyObject(), EasyMock.<TagListener> anyObject());
    EasyMock.expectLastCall().times(2);
    // Expect the device to be removed from the cache
    deviceCacheMock.remove(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall().once();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    TestDeviceUpdateListener listener1 = new TestDeviceUpdateListener();
    TestDeviceUpdateListener listener2 = new TestDeviceUpdateListener();

    Set<Device> devices = new HashSet<>();
    devices.add(device1);

    // Subscribe multiple listeners
    deviceManager.subscribeDevices(devices, listener1);
    deviceManager.subscribeDevices(devices, listener2);

    // Remove a listener
    deviceManager.unsubscribeDevices(devices, listener1);
    Assert.assertFalse(deviceManager.getDeviceUpdateListeners().contains(listener1));
    Assert.assertTrue(deviceManager.getDeviceUpdateListeners().contains(listener2));
    Assert.assertTrue(deviceManager.isSubscribed(device1));

    // Remove another listener
    deviceManager.unsubscribeDevices(devices, listener2);
    Assert.assertFalse(deviceManager.getDeviceUpdateListeners().contains(listener2));
    Assert.assertFalse(deviceManager.isSubscribed(device1));

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  @Test
  public void testUnsubscribeAllDevices() {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    final TagImpl cdt1 = new TagImpl(100000L);
    final TagImpl cdt2 = new TagImpl(200000L);
    Map<String, Tag> deviceProperties1 = new HashMap<>();
    Map<String, Tag> deviceProperties2 = new HashMap<>();
    deviceProperties1.put("test_property_name_1", cdt1);
    deviceProperties2.put("test_property_name_2", cdt2);

    final DeviceImpl device1 = new DeviceImpl(10L, "test_device_10", 1L, "test_device_class");
    final DeviceImpl device2 = new DeviceImpl(20L, "test_device_20", 1L, "test_device_class");
    device1.setDeviceProperties(deviceProperties1);
    device2.setDeviceProperties(deviceProperties2);

    Set<Device> devices = new HashSet<>();
    devices.add(device1);
    devices.add(device2);

    Map<Long, Tag> cacheReturnMap = getCacheReturnMap();

    // Expect the tag manager to subscribe to the tags
    tagServiceMock.subscribe(EasyMock.<Set<Long>> anyObject(), EasyMock.<TagListener> anyObject());
    EasyMock.expectLastCall().andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        final Set<Long> tagIds = (Set<Long>) EasyMock.getCurrentArguments()[0];
        // Simulate the tag update calls
        //if (tagIds.contains(cdt1.getId()))
          deviceManager.onUpdate(cdt1);
        //else if (tagIds.contains(cdt2.getId()))
          deviceManager.onUpdate(cdt2);
        return true;
      }
    }).times(1);

    // Expect the device manager to add the devices to the cache
    deviceCacheMock.add(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall().times(2);
    // Expect the device manager to get the devices from the cache - once to
    // call onInitialUpdate() and once to call onUpdate()
    EasyMock.expect(deviceCacheMock.getAllDevices()).andReturn(new ArrayList<>(devices)).times(4);

    // Expect the device manager to get all cached devices before unsubscribing
    EasyMock.expect(deviceCacheMock.getAllDevices()).andReturn(new ArrayList<>(devices)).once();
    // Expect the device manager to unsubscribe the tags
    tagServiceMock.unsubscribe(EasyMock.<Set<Long>> anyObject(), EasyMock.<TagListener> anyObject());
    EasyMock.expectLastCall().times(1);
    // Expect the devices to be removed from the cache
    deviceCacheMock.remove(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall().times(2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    TestDeviceUpdateListener listener = new TestDeviceUpdateListener();
    deviceManager.subscribeDevices(devices, listener);

    // Update a property
    deviceManager.onUpdate(cacheReturnMap.get(100000L));
    deviceManager.onUpdate(cacheReturnMap.get(200000L));

    deviceManager.unsubscribeAllDevices(listener);

    Assert.assertFalse(deviceManager.getDeviceUpdateListeners().contains(listener));
    Assert.assertFalse(deviceManager.getDeviceUpdateListeners().contains(listener));
    Assert.assertTrue(deviceManager.getAllSubscribedDevices(listener).size() == 0);
    Assert.assertFalse(deviceManager.isSubscribed(device1));
    Assert.assertFalse(deviceManager.isSubscribed(device2));

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  @Test
  public void testGetAllSubscribedDevices() throws JMSException {
    // Reset the mock
    EasyMock.reset(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    reset(requestHandlerMock);

    DeviceInfo di1 = new DeviceInfo("test_class", "test_device_1");
    DeviceInfo di2 = new DeviceInfo("test_class", "test_device_2");
    Set<DeviceInfo> deviceInfoList = new HashSet<>(Arrays.asList(di1, di2));

    TransferDevice transferDevice1 = new TransferDeviceImpl(1000L, "test_device_1", 1L, "test_class");
    TransferDevice transferDevice2 = new TransferDeviceImpl(1001L, "test_device_2", 1L, "test_class");
    Collection<TransferDevice> transferDeviceList1 = Arrays.asList(transferDevice1, transferDevice2);
    TransferDevice transferDevice3 = new TransferDeviceImpl(1002L, "test_device_3", 1L, "test_class");
    Collection<TransferDevice> transferDeviceList2 = Arrays.asList(transferDevice3);

    // Expectations
    // Expect the device manager to check the cache
    EasyMock.expect(deviceCacheMock.get(EasyMock.<String> anyObject())).andReturn(null).times(3);
    EasyMock.expect(requestHandlerMock.getDevices(EasyMock.<Set<DeviceInfo>> anyObject())).andReturn(transferDeviceList1);
    deviceCacheMock.add(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall().times(5);
    tagServiceMock.subscribe(EasyMock.<Set<Long>> anyObject(), EasyMock.<TagListener> anyObject());
    EasyMock.expectLastCall();

    EasyMock.expect(requestHandlerMock.getDevices(EasyMock.<Set<DeviceInfo>> anyObject())).andReturn(transferDeviceList2);
    deviceCacheMock.add(EasyMock.<Device> anyObject());
    EasyMock.expectLastCall();
    tagServiceMock.subscribe(EasyMock.<Set<Long>> anyObject(), EasyMock.<TagListener> anyObject());
    EasyMock.expectLastCall();

    // Setup is finished, need to activate the mock
    EasyMock.replay(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    replay(requestHandlerMock);

    // Run the actual code to test
    // Subscribe to a list of devices
    DeviceInfoUpdateListener listener1 = new TestDeviceUpdateListener();
    deviceManager.subscribeDevices(deviceInfoList, listener1);
    Collection<Device> subscribedDevices = deviceManager.getAllSubscribedDevices(listener1);
    Assert.assertTrue(subscribedDevices.size() == 2);

    // Subscribe to a single device
    DeviceInfoUpdateListener listener2 = new TestDeviceUpdateListener();
    deviceManager.subscribeDevice(new DeviceInfo("test_class", "test_device_3"), listener2);
    subscribedDevices = deviceManager.getAllSubscribedDevices(listener2);
    Assert.assertTrue(subscribedDevices.size() == 1);

    // Verify that everything happened as expected
    EasyMock.verify(tagServiceMock, deviceCacheMock, dataTagCacheMock, commandManagerMock);
    verify(requestHandlerMock);
  }

  class TestDeviceUpdateListener implements DeviceInfoUpdateListener {

    CountDownLatch latch = new CountDownLatch(1);
    List<Device> devices;
    List<DeviceInfo> unknownDevices;
    PropertyInfo propertyInfo;

    @Override
    public void onInitialUpdate(List<Device> devices) {
      log.info("onInitialUpdate()");
      this.devices = devices;
      Assert.assertTrue(areAllPropertiesAndFieldsSubscribed(devices));
    }

    @Override
    public void onUpdate(Device device, PropertyInfo propertyInfo) {
      log.info("onUpdate()");
      this.propertyInfo = propertyInfo;
      latch.countDown();
    }

    @Override
    public void onDevicesNotFound(List<DeviceInfo> unknownDevices) {
      this.unknownDevices = unknownDevices;
      log.info("onDevicesNotFound()");
      latch.countDown();
    }

    public void await(Long timeout) {
      try {
        latch.await(timeout, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    public Device getDevice() {
      return devices.get(0);
    }

    public List<Device> getDevices() {
      return devices;
    }

    public List<DeviceInfo> getUnknownDevices() {
      return unknownDevices;
    }

    public void setLatch(int count) {
      latch = new CountDownLatch(count);
    }
  }

  private Map<Long, Tag> getCacheReturnMap() {
    Map<Long, Tag> cacheReturnMap = new HashMap<>();
    TagController cdt1 = new TagController(100000L);
    TagController cdt2 = new TagController(200000L);

    TagUpdate tu1 = DeviceTestUtils.createValidTransferTag(cdt1.getTagImpl().getId(), "test_tag_name_1");
    TagUpdate tu2 = DeviceTestUtils.createValidTransferTag(cdt2.getTagImpl().getId(), "test_tag_name_2");

    try {
      cdt1.update(tu1);
      cdt2.update(tu2);
    } catch (RuleFormatException e1) {
      e1.printStackTrace();
    }

    cacheReturnMap.put(cdt1.getTagImpl().getId(), cdt1.getTagImpl());
    cacheReturnMap.put(cdt2.getTagImpl().getId(), cdt2.getTagImpl());
    return cacheReturnMap;
  }

  private boolean areAllPropertiesAndFieldsSubscribed(List<Device> devices) {
    for (Device device : devices) {
      Map<String, Property> properties = ((DeviceImpl) device).getDeviceProperties();
      for (Property property : properties.values()) {
        if (((PropertyImpl) property).isDataTag()) {
          if (!((PropertyImpl) property).isValueLoaded()) {
            return false;
          }
        }

        if (((PropertyImpl) property).isMappedProperty()) {
          for (Field field : property.getFields()) {
            if (((FieldImpl) field).isDataTag()) {
              if (!((FieldImpl) field).isValueLoaded()) {
                return false;
              }
            }
          }
        }
      }
    }

    return true;
  }
}
