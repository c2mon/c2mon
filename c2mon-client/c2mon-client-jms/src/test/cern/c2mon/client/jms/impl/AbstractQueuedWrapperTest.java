package cern.c2mon.client.jms.impl;

import static org.junit.Assert.*;

import java.util.Enumeration;
import java.util.concurrent.Executors;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.easymock.MockControl;
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
  
  private TextMessage mockMessage;
  
  private IMocksControl mocksControl;

  /**
   * Extendsion for testing AbstractQueuedWrapper.
   * @author Mark Brightwell
   *
   */
  private class QueuedWrapper extends AbstractQueuedWrapper<Object> {

    public QueuedWrapper(int queueCapacity, SlowConsumerListener slowConsumerListener) {
      super(queueCapacity, slowConsumerListener, Executors.newFixedThreadPool(2));      
    }

    @Override
    protected Object convertMessage(Message message) throws JMSException {
      return new Object();
    }

    @Override
    protected void notifyListeners(Object event) {
      listenerNotified++;
      try {
        Thread.sleep(1000); //queue will overflow as this consumer is slow
      } catch (InterruptedException e) {
        e.printStackTrace();
      } 
    }

    @Override
    protected String getDescription(Object event) {
      return "test description";
    }
    
  }
  
  @Before
  public void beforeTest() {
    mocksControl = EasyMock.createNiceControl();
    mockMessage = mocksControl.createMock(TextMessage.class);
    mockSlowConsumerListener = mocksControl.createMock(SlowConsumerListener.class);
    wrapper = new QueuedWrapper(10, mockSlowConsumerListener);
    wrapper.start();    
  }
  
  @After
  public void afterTest() {
    wrapper.stop();
  }
  
  @Test
  public void testHealthNotification() {
    mocksControl.reset();
    //will get several calls: important is that once is made
    mockSlowConsumerListener.onSlowConsumer("Slow consumer warning: " + QueuedWrapper.class.getSimpleName() + " unable to keep up with incoming data. " + " Info: test description");    
    mocksControl.replay();
    int i = 0;
    while (i < 13) {
      wrapper.onMessage(mockMessage);
      i++;
    }
    mocksControl.verify();
    assertTrue(listenerNotified > 0);
  }
  
  @Test
  public void testNoHealthNotification() throws InterruptedException {
    mocksControl.reset();
    mocksControl.replay();
    int i = 0;
    while (i < 2) {
      wrapper.onMessage(mockMessage);
      Thread.sleep(2000);
      i++;
    }
    mocksControl.verify();
    assertEquals(2, listenerNotified);    
  }
  
}
