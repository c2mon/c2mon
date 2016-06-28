package cern.c2mon.daq.rest;
/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.rest.config.WebConfigTest;
import cern.c2mon.daq.rest.controller.RestController;
import cern.c2mon.daq.rest.address.RestAddressFactory;
import cern.c2mon.daq.rest.address.RestPostAddress;
import cern.c2mon.daq.rest.scheduling.PostScheduler;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Created by fritter on 05/02/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfigTest.class)
@WebAppConfiguration
public class RestPostTesting {

  @Autowired
  WebApplicationContext webApplicationContext;

  @Autowired
  RestController restController;


  IEquipmentMessageSender equipmentMessageSender;
  IEquipmentConfiguration equipmentConfiguration;
  EquipmentLogger equipmentLogger;
  ISourceDataTag sourceDataTag;

  MockMvc mockMvc;

  @Before
  public void setUp() {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();

    equipmentMessageSender = EasyMock.createStrictMock(IEquipmentMessageSender.class);
    equipmentConfiguration = EasyMock.createStrictMock(IEquipmentConfiguration.class);
    sourceDataTag = EasyMock.createStrictMock(ISourceDataTag.class);
//    equipmentLogger = EasyMock.createMock(EquipmentLogger.class);

  }

  @After
  public void afterTest(){
    EasyMock.reset(equipmentConfiguration, sourceDataTag, equipmentMessageSender);
  }


  /**
   * This test tests if a message can be received and than sent to the server.
   */
  @Test
  public void messageReceivedInInterval() {
    // setup
    PostScheduler scheduler = new PostScheduler(equipmentMessageSender, equipmentConfiguration, equipmentLogger);
    restController.setPostScheduler(scheduler);

    HashMap<String, String> map = new HashMap<>();
    map.put("mode", "POST");
    map.put("postFrequency", "10");

    RestPostAddress hardwareAddress = (RestPostAddress) RestAddressFactory.createHardwareAddress(map);

    // mocks for setup
    EasyMock.expect(equipmentConfiguration.getSourceDataTag(1L)).andReturn(sourceDataTag);
    EasyMock.expect(sourceDataTag.getHardwareAddress()).andReturn(hardwareAddress);

    EasyMock.replay(equipmentConfiguration, sourceDataTag);
    scheduler.addTask(1L);
    EasyMock.verify(equipmentConfiguration, sourceDataTag);
    EasyMock.reset(equipmentConfiguration, sourceDataTag);

    // mocks for mvc post
    EasyMock.expect(equipmentConfiguration.getSourceDataTag(1L)).andReturn(sourceDataTag);

    EasyMock.expect(equipmentConfiguration.getSourceDataTag(1L)).andReturn(sourceDataTag);
    EasyMock.expect(sourceDataTag.getHardwareAddress()).andReturn(hardwareAddress);

    EasyMock.expect(sourceDataTag.getDataType()).andReturn("String");
    EasyMock.expect(sourceDataTag.getDataType()).andReturn("String");

    EasyMock.expect(equipmentMessageSender.sendTagFiltered(isA(ISourceDataTag.class), isA(String.class), EasyMock.anyLong())).andReturn(true);

    EasyMock.replay(equipmentConfiguration, sourceDataTag, equipmentMessageSender);


    try {
      // let some time pass and send than a message to the server
      Thread.sleep(10_000);
      mockMvc.perform((post("/tags/1").contentType(MediaType.TEXT_PLAIN_VALUE).content("testText"))).andExpect(status().isOk());

      // after additional 10 seconds there should not send a fault message because the last message should have reset the timer
      Thread.sleep(10_000);

    } catch (Exception e) {
      e.printStackTrace();
    }

    EasyMock.verify(equipmentConfiguration, sourceDataTag, equipmentMessageSender);
  }

  /**
   * Test if the daq send an invalid message if no message is received
   */
  @Test
  public void messageNotReceivedInInterval() {
    // setup
    PostScheduler scheduler = new PostScheduler(equipmentMessageSender, equipmentConfiguration, equipmentLogger);
    restController.setPostScheduler(scheduler);

    HashMap<String, String> map = new HashMap<>();
    map.put("mode", "POST");
    map.put("postFrequency", "3");

    RestPostAddress hardwareAddress = (RestPostAddress) RestAddressFactory.createHardwareAddress(map);

    // mocks for setup
    EasyMock.expect(equipmentConfiguration.getSourceDataTag(1L)).andReturn(sourceDataTag);
    EasyMock.expect(sourceDataTag.getHardwareAddress()).andReturn(hardwareAddress);

    EasyMock.replay(equipmentConfiguration, sourceDataTag);
    scheduler.addTask(1L);
    EasyMock.verify(equipmentConfiguration, sourceDataTag);
    EasyMock.reset(equipmentConfiguration, sourceDataTag);

    // mocks for timeOut
    EasyMock.expect(equipmentConfiguration.getSourceDataTag(1L)).andReturn(sourceDataTag);

    equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.DATA_UNAVAILABLE, "No value received in the given time interval of the DataTag-1");
    expectLastCall().once();

    EasyMock.replay(equipmentConfiguration, sourceDataTag, equipmentMessageSender);

    try {
      // let some time pass and send than a message to the server
      Thread.sleep(7_000);

    } catch (Exception e) {
      e.printStackTrace();
    }

    EasyMock.verify(equipmentConfiguration, sourceDataTag, equipmentMessageSender);
  }

  /**
   * Test if the wrong message is send to the web service
   */
  @Test
  public void wrongMessageReceived() {
    // setup
    PostScheduler scheduler = new PostScheduler(equipmentMessageSender, equipmentConfiguration, equipmentLogger);
    restController.setPostScheduler(scheduler);

    HashMap<String, String> map = new HashMap<>();
    map.put("mode", "POST");
    map.put("postFrequency", "10");

    RestPostAddress hardwareAddress = (RestPostAddress) RestAddressFactory.createHardwareAddress(map);

    // mocks for setup
    EasyMock.expect(equipmentConfiguration.getSourceDataTag(1L)).andReturn(sourceDataTag);
    EasyMock.expect(sourceDataTag.getHardwareAddress()).andReturn(hardwareAddress);

    EasyMock.replay(equipmentConfiguration, sourceDataTag);
    scheduler.addTask(1L);
    EasyMock.verify(equipmentConfiguration, sourceDataTag);
    EasyMock.reset(equipmentConfiguration, sourceDataTag);

    EasyMock.replay(equipmentConfiguration, sourceDataTag, equipmentMessageSender);

    try {
      // unknown url:
      mockMvc.perform((post("wrong/1").contentType(MediaType.TEXT_PLAIN_VALUE).content("testText"))).andExpect(status().isNotFound());

      // unknown method:
      mockMvc.perform((post("/wrong/1").contentType(MediaType.TEXT_PLAIN_VALUE).content("testText"))).andExpect(status().isMethodNotAllowed());

      // missing PathVariable:
      mockMvc.perform((post("/tags/").contentType(MediaType.TEXT_PLAIN_VALUE).content("testText"))).andExpect(status().isMethodNotAllowed());

      // missing content:
      mockMvc.perform((post("/tags/1").contentType(MediaType.TEXT_PLAIN_VALUE))).andExpect(status().isBadRequest());

    } catch (Exception e) {
      e.printStackTrace();
    }

    EasyMock.verify(equipmentConfiguration, sourceDataTag, equipmentMessageSender);
  }

  /**
   * Test if the daq sends a messageReceived after the timeout happened
   */
  @Test
  public void messageReceivedAfterInterval() {
    // setup
    PostScheduler scheduler = new PostScheduler(equipmentMessageSender, equipmentConfiguration, equipmentLogger);
    restController.setPostScheduler(scheduler);

    HashMap<String, String> map = new HashMap<>();
    map.put("mode", "POST");
    map.put("postFrequency", "3");

    RestPostAddress hardwareAddress = (RestPostAddress) RestAddressFactory.createHardwareAddress(map);

    // mocks for setup
    EasyMock.expect(equipmentConfiguration.getSourceDataTag(1L)).andReturn(sourceDataTag);
    EasyMock.expect(sourceDataTag.getHardwareAddress()).andReturn(hardwareAddress);

    EasyMock.replay(equipmentConfiguration, sourceDataTag);
    scheduler.addTask(1L);
    EasyMock.verify(equipmentConfiguration, sourceDataTag);
    EasyMock.reset(equipmentConfiguration, sourceDataTag);

    // mocks for timeOut
    EasyMock.expect(equipmentConfiguration.getSourceDataTag(1L)).andReturn(sourceDataTag);

    equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.DATA_UNAVAILABLE, "No value received in the given time interval of the DataTag-1");
    expectLastCall().once();

    // mocks for mvc post
    EasyMock.expect(equipmentConfiguration.getSourceDataTag(1L)).andReturn(sourceDataTag);

    EasyMock.expect(equipmentConfiguration.getSourceDataTag(1L)).andReturn(sourceDataTag);
    EasyMock.expect(sourceDataTag.getHardwareAddress()).andReturn(hardwareAddress);

    EasyMock.expect(sourceDataTag.getDataType()).andReturn("String");
    EasyMock.expect(sourceDataTag.getDataType()).andReturn("String");

    EasyMock.expect(equipmentMessageSender.sendTagFiltered(isA(ISourceDataTag.class), isA(String.class), EasyMock.anyLong())).andReturn(true);

    EasyMock.replay(equipmentConfiguration, sourceDataTag, equipmentMessageSender);

    try {
      // let some time pass and send than a message to the server
      Thread.sleep(7_000);
      mockMvc.perform((post("/tags/1").contentType(MediaType.TEXT_PLAIN_VALUE).content("testText"))).andExpect(status().isOk());

    } catch (Exception e) {
      e.printStackTrace();
    }

    EasyMock.verify(equipmentConfiguration, sourceDataTag, equipmentMessageSender);
  }

}
