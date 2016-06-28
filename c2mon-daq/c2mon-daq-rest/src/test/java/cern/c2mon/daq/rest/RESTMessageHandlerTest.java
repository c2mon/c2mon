package cern.c2mon.daq.rest;
/******************************************************************************
 * Copyright (C) 2010- CERN. All rights not expressly granted are reserved.
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

import cern.c2mon.daq.rest.config.WebConfigTest;
import cern.c2mon.daq.rest.webaccess.RESTConnector;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import lombok.extern.slf4j.Slf4j;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * Created by fritter on 22/01/16.
 */

@UseHandler(RESTMessageHandler.class)
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfigTest.class)
@WebAppConfiguration
public class RESTMessageHandlerTest extends GenericMessageHandlerTst {

  // reference to the instance of the handler to test
  RESTMessageHandler theHandler;

  @Override
  protected void beforeTest() throws Exception {
    log.info("entering beforeTest()..");

    // cast the reference (declared in the parent class) to the expected type
    theHandler = (RESTMessageHandler) msgHandler;
//    theHandler.setRequestDelegator(new RequestDelegator(theHandler.getEquipmentMessageSender(), theHandler.getEquipmentConfiguration(), theHandler.getEquipmentLogger(RESTMessageHandler.class), restController));

    log.info("leaving beforeTest()");
  }

  @Override
  protected void afterTest() throws Exception {
    log.info("entering afterTest()..");

//    theHandler.disconnectFromDataSource();

    log.info("leaving afterTest()");

  }

  @UseConf("e_rest_test1.xml")
  @Test
  @DirtiesContext
  public void restGetCommFaultSuccessful() {
    // messageSender mock setup
    Capture<Long> id = new Capture<>();
    Capture<Boolean> val = new Capture<>();
    Capture<String> msg = new Capture<>();

    messageSender.sendCommfaultTag(EasyMock.captureLong(id), EasyMock.captureBoolean(val), EasyMock.capture(msg));
    expectLastCall().once();

    // record the mock
    replay(messageSender);

    // call your handler's connectToDataSource() - in real operation the DAQ core will do it!
    try {
      theHandler.connectToDataSource();
    } catch (EqIOException e) {
      e.printStackTrace();
    }
    try {
      Thread.sleep(1_000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    verify(messageSender);

    // check commFault sending
    assertEquals(107211L, id.getValue().longValue());
    assertEquals(true, val.getValue());
    assertEquals("setConnected - Accessed all web services", msg.getValue());

  }

  @UseConf("e_rest_test1.xml")
  @Test
  @DirtiesContext
  public void restGetSendSuccessful() {
    // create junit captures for the tag id, value and message (for the commmfault tag)
    Capture<SourceDataTagValue> sdtv = new Capture<>();

    messageSender.sendCommfaultTag(107211L, true, "setConnected - Accessed all web services");
    expectLastCall().once();
    messageSender.addValue(EasyMock.capture(sdtv));
    expectLastCall().once();
    replay(messageSender);

    // rest mock setup:
    MockRestServiceServer mockServer = MockRestServiceServer.createServer(RESTConnector.getRestTemplate());
    mockServer.expect(requestTo("http://www.testaddress.org/")).andExpect(method(HttpMethod.GET)).andRespond(withSuccess("resultSuccess", MediaType.TEXT_PLAIN));

    // call yout handler's connectToDataSource() - in real operation the DAQ core will do it!
    try {

      theHandler.connectToDataSource();
      Thread.sleep(6_000);

    } catch (EqIOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    verify(messageSender);
    mockServer.verify();

    assertEquals(sdtv.getValue().getId().longValue(), 54675L);
    assertEquals(sdtv.getValue().getValue(), "resultSuccess");
  }

  @UseConf("e_rest_test3.xml")
  @Test
  @DirtiesContext
  public void restGetWithExpressionSendSuccessful() {
    // create junit captures for the tag id, value and message (for the commmfault tag)
    Capture<SourceDataTagValue> sdtv = new Capture<>();

    messageSender.sendCommfaultTag(107211L, true, "setConnected - Accessed all web services");
    expectLastCall().once();
    messageSender.addValue(EasyMock.capture(sdtv));
    expectLastCall().once();
    replay(messageSender);

    // reply get Message + mock setup:
    MockRestServiceServer mockServer = MockRestServiceServer.createServer(RESTConnector.getRestTemplate());

    String jsonMessage = "{" +
        "        \"id\": 1701," +
        "        \"name\": \"Max Mustermann\"," +
        "        \"age\": 31" +
        "        }";

    mockServer.expect(requestTo("http://www.testaddress.org/")).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(jsonMessage, MediaType.TEXT_PLAIN));

    // call your handler's connectToDataSource() - in real operation the DAQ core will do it!
    try {

      theHandler.connectToDataSource();
      Thread.sleep(6_000);

    } catch (EqIOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    verify(messageSender);
    mockServer.verify();

    assertEquals(sdtv.getValue().getId().longValue(), 54675L);
    assertEquals(sdtv.getValue().getValue(), 1701L);
  }

  @UseConf("e_rest_test2.xml")
  @Test
  @DirtiesContext
  public void restPostCommFaultSuccessful() {
    // messageSender mock setup
    Capture<Long> id = new Capture<>();
    Capture<Boolean> val = new Capture<>();
    Capture<String> msg = new Capture<>();

    messageSender.sendCommfaultTag(EasyMock.captureLong(id), EasyMock.captureBoolean(val), EasyMock.capture(msg));
    expectLastCall().once();

    // record the mock
    replay(messageSender);

    try {
      theHandler.connectToDataSource();
    } catch (EqIOException e) {
      e.printStackTrace();
    }
    verify(messageSender);

    // check commFault sending
    assertEquals(107211L, id.getValue().longValue());
    assertEquals(true, val.getValue());
    assertEquals("setConnected - Accessed all web services", msg.getValue());
  }

}
