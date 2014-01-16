package cern.c2mon.server.daqcommunication.out.impl;


/**
 * Specification of the beans responsible for sending messages to the DAQ
 * layer and receiving a response. 
 * 
 * <p>This interface should remain provider-independent.
 * 
 * @author Mark Brightwell
 *
 */
public interface JmsProcessOut {

  /**
   * Sends a text message to the DAQ with the text as content, using
   * the JMS queue provided (TODO still Topic in implementation - 
   * should be changed at some point??).
   *  
   * @param text the content of the message
   * @param jmsListenerQueue the JMS queue to send the message to (as String)
   * @param timeout the timeout while waiting for a response from the DAQ
   * @return the text of the response message
   */
  String sendTextMessage(String text, String jmsListenerQueue, long timeout);
  
}
