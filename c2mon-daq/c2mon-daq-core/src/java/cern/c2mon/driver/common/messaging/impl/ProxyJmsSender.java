package cern.c2mon.driver.common.messaging.impl;

import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import cern.c2mon.driver.common.jmx.JmsSenderMXBean;
import cern.c2mon.driver.common.messaging.JmsSender;
import cern.tim.shared.daq.datatag.DataTagValueUpdate;
import cern.tim.shared.daq.datatag.SourceDataTagValue;
import cern.tim.util.buffer.PullEvent;
import cern.tim.util.buffer.PullException;
import cern.tim.util.buffer.SynchroBuffer;
import cern.tim.util.buffer.SynchroBufferListener;

/**
 * This class wraps a JMSSender so that all JMS sending occurs on separate threads from
 * the main application (in fact only needed for the processValue method since processValues
 * already runs on a separate thread in ProcessMessageSender).
 * 
 * It also puts all messages through FIFO buffers to prevent out of memory problems.
 * 
 * It can be wired in place of the usual JMSSender.
 * 
 * @author mbrightw
 *
 */

public class ProxyJmsSender implements JmsSender, JmsSenderMXBean {
  
  
  /**
   * The logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ProxyJmsSender.class);
  
  /**
   * The JMSSender to wrap.
   */
  private JmsSender wrappedSender;
  
  /**
   * Buffer storing the high priority messages
   * (sent with processValue).
   */
  private SynchroBuffer highPriorityBuffer;
  
  /**
   * Buffer storing the low priority messages
   * (sent with processValues).
   */
  private SynchroBuffer lowPriorityBuffer;
  
  /**
   * The Spring name for the ProxyJmsSender
   */
  private String beanName;

  /**
   * Init method called on bean initialization.
   */
  @PostConstruct
  public void init() {
    //initialize high priority buffer
    highPriorityBuffer = new SynchroBuffer(100, 200, 100, SynchroBuffer.DUPLICATE_OK, 10000);
    highPriorityBuffer.setSynchroBufferListener(new HighPriorityListener());
    highPriorityBuffer.enable();
    
    lowPriorityBuffer = new SynchroBuffer(100, 500, 100, SynchroBuffer.DUPLICATE_OK, 10000);
    lowPriorityBuffer.setSynchroBufferListener(new LowPriorityListener());
    lowPriorityBuffer.enable();
  }
  /**
   * Connect the wrapped JMSSender.
   */
  @Override
  public final void connect() {
    Thread proxyConnectorThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        wrappedSender.connect();
      }
    }, "Proxy sender connection thread");
    
    proxyConnectorThread.start();
  }

  /**
   * Disconnect the wrapped JMSSender.
   */
  @Override
  public final void disconnect() {
    wrappedSender.disconnect();
  }

  /**
   * Push the single value into the buffer.
   * @throws JMSException not used in proxy
   * @param sourceDataTagValue the value to process
   */
  @Override
  public final void processValue(final SourceDataTagValue sourceDataTagValue) throws JMSException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("pushing SourceDataTagValue into proxy buffer");
    }
    highPriorityBuffer.push(sourceDataTagValue);
  }

  /**
   * @param dataTagValueUpdate the collection of updates to process
   * @throws JMSException not used in proxy
   */
  @Override
  public final void processValues(final DataTagValueUpdate dataTagValueUpdate) throws JMSException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("pushing DataTagValueUpdate into proxy buffer");
    }
    lowPriorityBuffer.push(dataTagValueUpdate);
  }

  /**
   * Setter method.
   * @param wrappedSender the wrappedSender to set
   */
  public final void setWrappedSender(final JmsSender wrappedSender) {
    this.wrappedSender = wrappedSender;
  }
  
  
   /**
   * The buffer used to store the received collections of updates.
   * @author mbrightw
   *
   */
  private class LowPriorityListener implements SynchroBufferListener {

    /**
     * Retrieve the DataTagValueUpdate objects and call the wrapped processValues method for each
     * of these.
     * @param event pull event
     * @throws PullException not used 
     */
    @Override
    public void pull(final PullEvent event) throws PullException {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("entering pull() of proxy low priority buffer...");
        LOGGER.debug("\t Number of pulled dataTagValueUpdate objects (collections!) : " + event.getPulled().size());        
      }
      
      Iterator<DataTagValueUpdate> it = event.getPulled().iterator();      

      while (it.hasNext()) {
        //catch and log JMSExceptions (proxy should shield DAQ)
        try {
          wrappedSender.processValues(it.next());
        }
        catch (Exception ex) {
          LOGGER.error("JMSException caught when calling the proxied JMSSender's processValue method: " + ex.getMessage());
        }        
      }
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("leaving pull()...");
      }
    }
    
  }
  
  /**
   * The buffer used to store the single data tag source values.
   * @author mbrightw
   *
   */
  private class HighPriorityListener implements SynchroBufferListener {

    /**
     * Method called when the buffer triggers and event.
     * 
     * Simply call the processValue method on the wrapped JMSSender for each
     * {@link SourceDataTagValue} in the buffer.
     * @param event the pull event
     * @throws PullException not used in this case
     */
    @Override
    public void pull(final PullEvent event) throws PullException {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("entering pull() of proxy high priority buffer...");
        LOGGER.debug("\t Number of pulled objects : " + event.getPulled().size());        
      }
      
      Iterator<SourceDataTagValue> it = event.getPulled().iterator();      

      while (it.hasNext()) {
        //catch and log JMSExceptions (proxy should shield DAQ)
        try {
          wrappedSender.processValue(it.next());
        }
        catch (Exception ex) {
          LOGGER.error("Exception caught when calling the proxied JMSSender's processValue method: " + ex.getMessage());
        }        
      }
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("leaving pull()...");
      }      
    }
    
  }

  @Override
  public void shutdown() {
    wrappedSender.shutdown();
  }
  
  /**
   * Sets the isEnabled current value
   * 
   * @param value Enabling/disabling the action of sending information to the brokers
   */
  @Override
  public final void setEnabled(final boolean value) {
    this.wrappedSender.setEnabled(value);
  }
  
  /**
   * Gets the isEnabled current value
   * 
   * @return isEnabled Current status of the action of sending information to the brokers
   */
  @Override
  public final boolean getEnabled() {
    return this.wrappedSender.getEnabled();
  }
  
  /**
   * Sets the Spring name for the ProxyJmsSender
   */
  @Required
  public final void setBeanName(final String name) {
    this.beanName = name;
  }
  
  @Override
  public final String getBeanName() {
    return this.beanName;
  }

  @Override
  public final void jmsBrokerDataConnectionEnable(final boolean value) {
    setEnabled(value);
  }

}
