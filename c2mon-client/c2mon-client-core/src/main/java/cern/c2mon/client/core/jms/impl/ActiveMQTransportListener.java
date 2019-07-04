package cern.c2mon.client.core.jms.impl;

import java.io.IOException;

import org.apache.activemq.transport.TransportListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around the ActiveMQ {@link TransportListener} interface which is used within the {@link JmsProxyImpl} to
 * detect JMS disconnections
 *
 * @author Matthias Braeger
 */
interface ActiveMQTransportListener extends TransportListener {

  /** Logger instance */
  static final Logger LOG = LoggerFactory.getLogger(ActiveMQTransportListener.class);

  /**
   * called to process a command
   * @param command
   */
  @Override
  default void onCommand(Object command) {}

  /**
   * An unrecoverable exception has occured on the transport
   * @param error
   */
  @Override
  default void onException(IOException error) {
    String message = "JMSException caught by JMS connection exception listener (attempting to reconnect): " + error.getMessage();
    if (LOG.isDebugEnabled()) {
      LOG.debug(message, error);
    } else {
      LOG.error(message);
    }
    transportInterupted();
  }

  /**
   * The transport has suffered an interuption from which it hopes to recover
   *
   */
  @Override
  void transportInterupted();


  /**
   * The transport has resumed after an interuption
   *
   */
  @Override
  default void transportResumed() {}
}
