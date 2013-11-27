package cern.c2mon.daq.common.messaging;

import javax.jms.JMSException;

import cern.c2mon.shared.daq.datatag.DataTagValueUpdate;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;

/**
 * The interface that must be implemented by a class used to send updates
 * to the server via JMS.
 * @author mbrightw
 *
 */
public interface JmsSender {
  
  /**
   * Connects to the JMS broker.
   */
  void connect();
  
  /**
   * Disconnects from the JMS broker. In DAQ core, only used on shutdown of the DAQ.
   */
  void disconnect();
  
  /**
   * The processValues method creates an XML JMS message with a content of the 
   * set SourceDataTagsValue objects (encapsulated inside DataTagValueUpdate object).
   * This method is called automatically every time a PullEvent comes from dataTags 
   * synchrobuffer.
   * @param dataTagValueUpdate the collection of SourceDataTagValue's to send
   * @throws JMSException if a JMS exception is caught while sending the values
   */
  void processValues(DataTagValueUpdate dataTagValueUpdate) throws JMSException;
  
  /**
   * The ProcessValue method creates a JMS message with a content of the DataTagValue 
   * object (passed as an argument) encoded into XML.
   * @param sourceDataTagValue the source value to send
   * @throws JMSException if a JMS exception occurs
   */
  void processValue(SourceDataTagValue sourceDataTagValue) throws JMSException;

  /**
   * Do final shutdown.
   */
  void shutdown();  
  
  /**
   * This method is used for JMX
   * @return The Spring name for the current JmsSender
   */
  String getBeanName();
  
  /**
   * Sets the isEnabled current value
   * 
   * @param value Enabling/disabling the action of sending information to the brokers
   */
  void setEnabled(final boolean value);
  
  /**
   * Gets the isEnabled current value
   * 
   * @return isEnabled Current status of the action of sending information to the brokers
   */
  boolean getEnabled();
}
