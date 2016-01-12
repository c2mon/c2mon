/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.client.request;

import java.math.BigDecimal;
import java.util.*;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.alarm.TagWithAlarmsImpl;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.common.metadata.Metadata;
import cern.c2mon.shared.util.json.GsonFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.test.broker.TestBrokerService;
import cern.c2mon.shared.client.device.DeviceClassNameResponse;
import cern.c2mon.shared.client.device.TransferDevice;
import cern.c2mon.shared.client.request.ClientRequestImpl;

import static cern.c2mon.server.client.request.util.CompareClientRequestResult.compareAlarmValuesWithAlarCacheObject;
import static cern.c2mon.server.client.request.util.CompareClientRequestResult.compareTagUpdateWithDataTagCacheObject;
import static cern.c2mon.server.client.request.util.CompareClientRequestResult.compareTagUpdates;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Justin Lewis Salmon
 * @author Franz Ritter
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/server/client/config/server-client-requestdelegator-test.xml" })
public class ClientRequestDelegatorTest {

  /** Component to test */
  @Autowired
  ClientRequestDelegator clientRequestDelegator;

  /** Mocked components */
  @Autowired
  DeviceFacade deviceFacadeMock;

  @Autowired
  DeviceClassFacade deviceClassFacadeMock;

  @Autowired
  ClientTagRequestHelper tagRequestHelper;

  @Autowired
  RuleTagCache ruleTagCache;

  @Autowired
  ControlTagCache controlTagCache;

  @Autowired
  DataTagCache dataTagCache;

  @Autowired
  TagLocationService tagLocationService;

  @Autowired
  AliveTimerFacade aliveTimerFacade;

  @Autowired
  TagFacadeGateway tagFacadeGateway;

  @Value("${jms.client.request.queue}")
  private String requestQueue;

  private static TestBrokerService testBrokerService = new TestBrokerService();

  @BeforeClass
  public static void startJmsBroker() throws Exception {
    testBrokerService.createAndStartBroker();
  }

  @AfterClass
  public static void stopBroker() throws Exception {
    testBrokerService.stopBroker();
  }

  @Before
  public void resetMocks() {
    EasyMock.reset(ruleTagCache, controlTagCache, dataTagCache, tagLocationService, tagFacadeGateway, aliveTimerFacade);
  }


  @Test
  public void testHandleDeviceClassNamesRequest() throws JMSException {
    // TODO: turn this into an integration test using the actual JMS
    // infrastructure

    // Reset the mock
    EasyMock.reset(deviceFacadeMock, deviceClassFacadeMock);

    Session session = testBrokerService.getConnectionFactory().createConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);

    Destination replyDestination = session.createTemporaryQueue();
    TextMessage message = new ActiveMQTextMessage();
    message.setJMSReplyTo(replyDestination);

    ClientRequestImpl<DeviceClassNameResponse> request = new ClientRequestImpl<>(DeviceClassNameResponse.class);
    message.setText(request.toJson());

    List<String> classNames = new ArrayList<>();
    classNames.add("test_device_class_name_1");
    classNames.add("test_device_class_name_2");

    // Expect the request handler to delegate and get class names from the
    // device cache
    EasyMock.expect(deviceClassFacadeMock.getDeviceClassNames()).andReturn(classNames);

    // Setup is finished, need to activate the mock
    EasyMock.replay(deviceFacadeMock, deviceClassFacadeMock);

    // Pass in a dummy message
    clientRequestDelegator.onMessage(message, session);

    // Verify that everything happened as expected
    EasyMock.verify(deviceFacadeMock, deviceClassFacadeMock);
  }

  @Test
  public void testHandleDeviceRequest() throws JMSException {
    // TODO: turn this into an integration test using the actual JMS
    // infrastructure

    // Reset the mock
    EasyMock.reset(deviceFacadeMock);

    Session session = testBrokerService.getConnectionFactory().createConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);

    Destination replyDestination = session.createTemporaryQueue();
    TextMessage message = new ActiveMQTextMessage();
    message.setJMSReplyTo(replyDestination);

    ClientRequestImpl<TransferDevice> request = new ClientRequestImpl<>(TransferDevice.class);
    request.setRequestParameter("test_device_class_name_1");
    message.setText(request.toJson());

    Long deviceClassId = 1L;
    List<Device> devices = new ArrayList<>();
    Device device1 = new DeviceCacheObject(1000L, "test_device_1", deviceClassId);
    Device device2 = new DeviceCacheObject(2000L, "test_device_2", deviceClassId);
    devices.add(device1);
    devices.add(device2);

    // Expect the request handler to look up the device class ID
    EasyMock.expect(deviceFacadeMock.getDevices("test_device_class_name_1")).andReturn(devices);
    EasyMock.expect(deviceFacadeMock.getClassNameForDevice(EasyMock.<Long> anyObject())).andReturn(request.getRequestParameter()).times(2);

    // Setup is finished, need to activate the mock
    EasyMock.replay(deviceFacadeMock);

    // Pass in a dummy message
    clientRequestDelegator.onMessage(message, session);

    // Verify that everything happened as expected
    EasyMock.verify(deviceFacadeMock);
  }

  @Test
  public void handleClientRequestTagValue(){

    // calling of the isInTagCache() Method in getTagsById() [ClientTagRequestHelper]
    EasyMock.expect(tagLocationService.isInTagCache(1L)).andReturn(true);

    DataTagCacheObject dataTagObject = CacheObjectCreation.createTestDataTag();
    dataTagObject.setMetadata(Metadata.builder().addMetadata("testString","hello").addMetadata("tesInt",1).addMetadata("booleanFoo",true).addMetadata("tesLong",1L).addMetadata("tesFloat",1.0f).addMetadata("tesDouble",1.0).build());
    dataTagObject.setProcessId(10L);
    dataTagObject.setProcessId(10L);

    // build Tag
    TagWithAlarms tagWithAlarm = new TagWithAlarmsImpl(dataTagObject, Collections.EMPTY_LIST);

    EasyMock.expect(tagFacadeGateway.getTagWithAlarms(1L)).andReturn(tagWithAlarm);
    EasyMock.expect(aliveTimerFacade.isRegisteredAliveTimer(1L)).andReturn(false);
    EasyMock.replay(tagLocationService, tagFacadeGateway, aliveTimerFacade);

    ClientRequestImpl<TagUpdate> request = new ClientRequestImpl<>(TagUpdate.class);
    request.addTagId(1L);

    // build the TagUpdate through handleTagRequest() call
    Collection<? extends ClientRequestResult> tagUpdates = tagRequestHelper.handleTagRequest(request);
    assertTrue(tagUpdates.size() == 1);
    assertTrue(((List)tagUpdates).get(0) instanceof TagUpdate);

    // compare tag data of the Tag
    TagUpdate tagUpdate = (TagUpdate) ((List)tagUpdates).get(0);
    compareTagUpdateWithDataTagCacheObject(tagUpdate ,dataTagObject);

    // verify mocks
    EasyMock.verify(tagLocationService, tagFacadeGateway, aliveTimerFacade);

    // test the parsing to Json and from json:
    String jsonMessage = GsonFactory.createGson().toJson(tagUpdates);

    ClientRequestImpl<TagUpdate> clientRequest = new ClientRequestImpl<>(TagUpdate.class);
    Collection<TagUpdate> tagUpdateFromJson = clientRequest.fromJsonResponse(jsonMessage);

    // compare tag on the sever side with the on on the client side:
    TagUpdate tagUpdateClient =(TagUpdate) ((List)tagUpdateFromJson).get(0);
    compareTagUpdates(tagUpdateClient, tagUpdate);
  }

  @Test
  public void handleClientRequestTagValueWithAlarm(){

    // calling of the isInTagCache() Method in getTagsById() [ClientTagRequestHelper]
    EasyMock.expect(tagLocationService.isInTagCache(1L)).andReturn(true);

    DataTagCacheObject dataTagObject = CacheObjectCreation.createTestDataTag();
    dataTagObject.setMetadata(Metadata.builder().addMetadata("testString","hello").addMetadata("tesInt",1).addMetadata("booleanFoo",true).addMetadata("tesLong",1L).addMetadata("tesFloat",1.0f).addMetadata("tesDouble",1.0).build());
    dataTagObject.setProcessId(10L);
    dataTagObject.setProcessId(10L);

    // build Alarms
    AlarmCacheObject alarmServer1 = CacheObjectCreation.createTestAlarm1();
    AlarmCacheObject alarmServer2 = CacheObjectCreation.createTestAlarm2();

    alarmServer1.setDataTagId(dataTagObject.getId());
    alarmServer2.setDataTagId(dataTagObject.getId());
    alarmServer1.setMetadata(Metadata.builder().addMetadata("testString","hello").addMetadata("tesInt",1).addMetadata("booleanFoo",true).build());
    alarmServer2.setMetadata(Metadata.builder().addMetadata("testString","hello").addMetadata("tesInt",1).addMetadata("booleanFoo",true).build());

    TagWithAlarms tagWithAlarm = new TagWithAlarmsImpl(dataTagObject, new ArrayList<Alarm>(Arrays.asList(alarmServer1,alarmServer2)));

    EasyMock.expect(tagFacadeGateway.getTagWithAlarms(1L)).andReturn(tagWithAlarm);
    EasyMock.expect(aliveTimerFacade.isRegisteredAliveTimer(1L)).andReturn(false);
    EasyMock.replay(tagLocationService, tagFacadeGateway, aliveTimerFacade);

    ClientRequestImpl<TagUpdate> request = new ClientRequestImpl<>(TagUpdate.class);
    request.addTagId(1L);

    // build the TagUpdate through handleTagRequest() call
    Collection<? extends ClientRequestResult> tagUpdates = tagRequestHelper.handleTagRequest(request);
    assertTrue(tagUpdates.size() == 1);
    assertTrue(((List)tagUpdates).get(0) instanceof TagUpdate);

    // compare tag data of the Tag
    TagUpdate tagUpdate = (TagUpdate) ((List)tagUpdates).get(0);
    compareTagUpdateWithDataTagCacheObject(tagUpdate ,dataTagObject);
    compareAlarmValuesWithAlarCacheObject((AlarmValue) ((List)tagUpdate.getAlarms()).get(0), alarmServer1);
    compareAlarmValuesWithAlarCacheObject((AlarmValue) ((List)tagUpdate.getAlarms()).get(1), alarmServer2);

    // verify mocks
    EasyMock.verify(tagLocationService, tagFacadeGateway, aliveTimerFacade);

    // test the parsing to Json and from json:
    String jsonMessage = GsonFactory.createGson().toJson(tagUpdates);

    ClientRequestImpl<TagUpdate> clientRequest = new ClientRequestImpl<>(TagUpdate.class);
    Collection<TagUpdate> tagUpdateFromJson = clientRequest.fromJsonResponse(jsonMessage);

    // compare tag on the sever side with the on on the client side:
    TagUpdate tagUpdateClient =(TagUpdate) ((List)tagUpdateFromJson).get(0);
    compareTagUpdates(tagUpdateClient, tagUpdate);
  }

}
