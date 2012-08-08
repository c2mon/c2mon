package cern.c2mon.client.jms.impl;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Executors;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test of AbstractListenerWrapper.
 * 
 * @author Mark Brightwell
 *
 */
public class AbstractListenerWrapperTest {

  private volatile int listenerCalled;
  
  private AbstractListenerWrapper<Object, Long> wrapper = new AbstractListenerWrapper<Object, Long>(10, null, Executors.newSingleThreadExecutor()) {
    
    @Override
    protected String getDescription(Long event) {
      return event.toString();
    }
    
    @Override
    protected Long convertMessage(Message message) throws JMSException {
      return 100L;
    }
    
    @Override
    protected void invokeListener(Object listener, Long event) {
      listenerCalled++;
    }
    
    @Override
    protected boolean filterout(Long event) {      
      return event > 100;
    }
  };
  
  @Before
  public void setUp() {
    wrapper.addListener(1L); //fake listener: register one so the invoke listener method above is called once
  }
  
  @Test
  public void testFilterout() {
    wrapper.notifyListeners(101L);
    wrapper.notifyListeners(1000L);
    assertEquals(0L, listenerCalled);
  }
  
  @Test
  public void testNotFilterOut() {
    wrapper.notifyListeners(100L);
    wrapper.notifyListeners(90L);
    assertEquals(2L, listenerCalled);
  }
  
}
