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
package cern.c2mon.client.core.jms.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import cern.c2mon.client.core.jms.EnqueuingEventListener;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test of AbstractQueuedWrapper.
 *
 * @author Mark Brightwell
 *
 */
public class AbstractQueuedWrapperTest {

  /**
   * Class to test.
   */
  private AbstractQueuedWrapper<Object> wrapper;

  private volatile int listenerNotified = 0;

  private SlowConsumerListener mockSlowConsumerListener;

  private EnqueuingEventListener mockEnqueuingEventListener;

  private TextMessage mockMessage;

  private IMocksControl mocksControl;

  /**
   * Extension for testing AbstractQueuedWrapper.
   * @author Mark Brightwell
   *
   */
  private class QueuedWrapper extends AbstractQueuedWrapper<Object> {

    public QueuedWrapper(int queueCapacity, SlowConsumerListener slowConsumerListener, EnqueuingEventListener enqueuingEventListener) {
      super(queueCapacity, slowConsumerListener, enqueuingEventListener, Executors.newFixedThreadPool(2));
      this.setNotificationTimeBeforeWarning(100); //reset delay before warning to 300ms
    }

    @Override
    protected Object convertMessage(Message message) throws JMSException {
      return new Object();
    }

    @Override
    protected void notifyListeners(Object event) {
      listenerNotified++;
      try {
        Thread.sleep(500); //queue will overflow as this consumer is slow
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    @Override
    protected String getDescription(Object event) {
      return "test description";
    }

    @Override
    protected String getQueueName() {
      return "Test";
    }

  }

  @Before
  public void beforeTest() {
    mocksControl = EasyMock.createNiceControl();
    mockMessage = mocksControl.createMock(TextMessage.class);
    mockSlowConsumerListener = mocksControl.createMock(SlowConsumerListener.class);
    wrapper = new QueuedWrapper(10, mockSlowConsumerListener, mockEnqueuingEventListener);
    wrapper.start();
  }

  @After
  public void afterTest() {
    wrapper.stop();
  }

  /**
   * Notification happens because: the listener calls on the internal threads take 1s 
   *                                & the second notification is made after 200ms, at
   *                                which point the delay of 100ms is passed 
   * @throws InterruptedException
   */
  @Test
  public void testHealthNotification() throws InterruptedException {
    mocksControl.reset();
    //will get several calls: important is that once is made
    mockSlowConsumerListener.onSlowConsumer(EasyMock.isA(String.class));
    mocksControl.replay();
    int i = 0;
    while (i < 2) {
      wrapper.onMessage(mockMessage);
      i++;
      Thread.sleep(300); //must sleep because notification happens on incoming message
    }
    mocksControl.verify();
    assertTrue(listenerNotified > 0);
  }

  /**
   * No notification here as the notification is fast enough
   * @throws InterruptedException
   */
  @Test
  public void testNoHealthNotification() throws InterruptedException {
    wrapper.setNotificationTimeBeforeWarning(1000); //allow up to 1s
    mocksControl.reset();
    mocksControl.replay();
    int i = 0;
    while (i < 2) {
      wrapper.onMessage(mockMessage);
      Thread.sleep(300);
      i++;
    }
    Thread.sleep(500); //wait for both notifications to happen
    mocksControl.verify();
    assertEquals(2, listenerNotified);
  }

}
