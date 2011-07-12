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

import java.util.Collections;

import javax.jms.JMSException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.shared.client.request.JsonRequest;

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
   */
  @Test
  public void getTagValues() throws JMSException {
    EasyMock.expect(jmsProxy.sendRequest(EasyMock.isA(JsonRequest.class), EasyMock.eq("request queue"), EasyMock.eq(10))).andReturn(null);
    
    EasyMock.replay(jmsProxy);
    
    requestHandlerImpl.requestTagValues(Collections.EMPTY_LIST);
    
    EasyMock.verify(jmsProxy);
  }
  
  /**
   * Checks JmsProxy method is called correctly
   * @throws JMSException
   */
  @Test
  public void getTags() throws JMSException {
    EasyMock.expect(jmsProxy.sendRequest(EasyMock.isA(JsonRequest.class), EasyMock.eq("request queue"), EasyMock.eq(10))).andReturn(null);
    
    EasyMock.replay(jmsProxy);
    
    requestHandlerImpl.requestTags(Collections.EMPTY_LIST);
    
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
  
}
