/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.jms.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.jms.JMSException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.LongRange;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.shared.client.process.ProcessXmlResponse;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestImpl;
import cern.c2mon.shared.client.request.JsonRequest;
import cern.tim.shared.client.command.CommandExecutionStatus;
import cern.tim.shared.client.command.CommandReport;
import cern.tim.shared.client.command.CommandReportImpl;
import cern.tim.shared.client.command.CommandTagHandleImpl;
import cern.tim.shared.client.configuration.ConfigurationReport;

/**
 * Unit test of the RequestHandler implementation.
 * @author Mark Brightwell
 *
 */
public class RequestHandlerImplTest {

  /**
   * Class to test.
   */
  private RequestHandlerImpl requestHandlerImpl;

  /**
   * Mocks
   */
  private JmsProxy jmsProxy;

  @Before
  public void setUp() {
    jmsProxy = EasyMock.createMock(JmsProxy.class);    
    requestHandlerImpl = new RequestHandlerImpl(jmsProxy);
    requestHandlerImpl.setRequestQueue("request queue");
    requestHandlerImpl.setRequestTimeout(10);
  }

  /**
   * Checks JmsProxy method is called correctly
   * @throws JMSException
   */
  @Test
  public void getCurrentSupervisionStatus() throws JMSException {
    EasyMock.expect(jmsProxy.sendRequest(EasyMock.isA(JsonRequest.class), EasyMock.eq("request queue"), EasyMock.eq(10))).andReturn(null);

    EasyMock.replay(jmsProxy);

    requestHandlerImpl.getCurrentSupervisionStatus();

    EasyMock.verify(jmsProxy);
  }

  /**
   * Checks JmsProxy method is called correctly
   * @throws JMSException
   * @throws InterruptedException 
   */
  @Test
  public void getTagValues() throws JMSException, InterruptedException {
    Collection<Object> returnCollection = Arrays.asList(new Object());
    EasyMock.expect(jmsProxy.sendRequest(EasyMock.isA(JsonRequest.class), EasyMock.eq("request queue"), EasyMock.eq(10))).andReturn(returnCollection);

    EasyMock.replay(jmsProxy);

    requestHandlerImpl.requestTagValues(Arrays.asList(100L));

    EasyMock.verify(jmsProxy);
  }

  /**
   * Checks JmsProxy method is not called for empty list request.
   * @throws JMSException
   * @throws InterruptedException 
   */
  @Test
  public void getNoTagValues() throws JMSException, InterruptedException {    
    EasyMock.replay(jmsProxy);

    requestHandlerImpl.requestTagValues(Collections.EMPTY_LIST);

    EasyMock.verify(jmsProxy);
  }

  /**
   * Checks JmsProxy method is called correctly
   * @throws JMSException
   * @throws InterruptedException 
   */
  @Test
  public void getTags() throws JMSException, InterruptedException {
    Collection<Object> returnCollection = Arrays.asList(new Object());
    EasyMock.expect(jmsProxy.sendRequest(EasyMock.isA(JsonRequest.class), EasyMock.eq("request queue"), EasyMock.eq(10))).andReturn(returnCollection);

    EasyMock.replay(jmsProxy);

    requestHandlerImpl.requestTags(Arrays.asList(100L));   

    EasyMock.verify(jmsProxy);
  }

  /**
   * Tests that a request is split into bunches of 250 and results are gathered in the correct way.
   * @throws JMSException 
   */
  @Test
  public void getManyTags() throws JMSException {
    Collection<Object> returnCollection = Arrays.asList(new Object(), new Object());
    EasyMock.expect(jmsProxy.sendRequest(EasyMock.isA(JsonRequest.class), EasyMock.eq("request queue"), EasyMock.eq(10))).andReturn(returnCollection).times(40);

    EasyMock.replay(jmsProxy);

    LongRange range = new LongRange(1, 10000);

    long[] arrayRange = range.toArray();
    Collection<Long> ids = Arrays.asList(ArrayUtils.toObject(arrayRange)); 
    Collection result = requestHandlerImpl.requestTags(ids);
    Assert.assertEquals(80,result.size());

    EasyMock.verify(jmsProxy);
  }

  /**
   * Tests correct exception is thrown.
   */
  @Test(expected = NullPointerException.class)
  public void testRequestTagsWithNull() throws JMSException {
    requestHandlerImpl.requestTags(null);
  }

  /**
   * Tests correct exception is thrown.
   */
  @Test(expected = NullPointerException.class)
  public void testRequestTagValuesWithNull() throws JMSException {
    requestHandlerImpl.requestTagValues(null);
  }

  /**
   * Tests getProcessXml method.
   * @throws JMSException
   */
  @Test
  public void testGetProcessXml() throws JMSException {
    String processName = "name";
    Collection<ProcessXmlResponse> response = new ArrayList<ProcessXmlResponse>();
    response.add(new ProcessXmlResponse() {

      @Override
      public String getProcessXML() {
        return "process xml";
      }

      @Override
      public String getErrorMessage() {
        // TODO Auto-generated method stub
        return null;
      }
    });
    EasyMock.expect(jmsProxy.sendRequest(EasyMock.isA(JsonRequest.class), EasyMock.eq("request queue"), EasyMock.eq(10))).andReturn(response);

    EasyMock.replay(jmsProxy);

    String xmlString = requestHandlerImpl.getProcessXml(processName);

    EasyMock.verify(jmsProxy);
    Assert.assertEquals("process xml", xmlString);
  }

  /**
   * Tests getProcessXml method.
   * @throws JMSException
   */
  @Test
  public void testExecuteCommand() throws JMSException {
    
    Long id = 123L;

    ClientRequestImpl<CommandReportImpl> executeCommandRequest =
      new ClientRequestImpl<CommandReportImpl>(CommandReportImpl.class);
    
    CommandTagHandleImpl commandTagHandle = createCommandTagHandleImpl(id);
    executeCommandRequest.setObjectParameter(commandTagHandle);
    
    Collection<CommandReport> response = new ArrayList<CommandReport>();
    response.add(createCommandReport(executeCommandRequest));
    
    EasyMock.expect(jmsProxy.sendRequest(EasyMock.isA(JsonRequest.class), EasyMock.eq("request queue"), EasyMock.eq(10))).andReturn(response);

    EasyMock.replay(jmsProxy);

    CommandReport report = requestHandlerImpl.executeCommand(commandTagHandle);

    EasyMock.verify(jmsProxy);
    Assert.assertEquals(id, report.getId());
    
    System.out.println("id=" + id + " id=" + report.getId());
  }


  private CommandReport createCommandReport(final ClientRequest clientRequest) {

    CommandTagHandleImpl handle = (CommandTagHandleImpl) clientRequest.getObjectParameter() ;

    CommandReport report = new CommandReportImpl(handle.getId(), CommandExecutionStatus.STATUS_EXECUTION_FAILED, "test report") ;
    return report;
  }

  private ConfigurationReport createReport (final Long id) {

    return new ConfigurationReport(id, "test name", "test user");
  }

  private CommandTagHandleImpl createCommandTagHandleImpl (final Long id) {

    //    AuthorizationDetails authDetails = new AuthorizationDetails

    CommandTagHandleImpl commandTagHadle = new CommandTagHandleImpl(id,"test Host Name");

    return commandTagHadle;
  }

  /**
   * Tests when an error occurs on the server side.
   * @throws JMSException
   */
  @Test(expected=RuntimeException.class)
  public void testGetProcessXmlError() throws JMSException {
    String processName = "name";
    Collection<ProcessXmlResponse> response = new ArrayList<ProcessXmlResponse>();
    response.add(new ProcessXmlResponse() {

      @Override
      public String getProcessXML() {
        return null;
      }

      @Override
      public String getErrorMessage() {
        return "Error message";
      }
    });
    EasyMock.expect(jmsProxy.sendRequest(EasyMock.isA(JsonRequest.class), EasyMock.eq("request queue"), EasyMock.eq(10))).andReturn(response);

    EasyMock.replay(jmsProxy);

    String xmlString = requestHandlerImpl.getProcessXml(processName);

  }

}
