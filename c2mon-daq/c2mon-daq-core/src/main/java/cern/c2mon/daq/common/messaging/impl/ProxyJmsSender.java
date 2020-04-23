/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.daq.common.messaging.impl;

import java.util.Iterator;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.messaging.JmsSender;
import cern.c2mon.shared.common.datatag.DataTagValueUpdate;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.util.buffer.PullEvent;
import cern.c2mon.shared.util.buffer.PullException;
import cern.c2mon.shared.util.buffer.SynchroBuffer;
import cern.c2mon.shared.util.buffer.SynchroBufferListener;

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
public class ProxyJmsSender implements JmsSender {


  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ProxyJmsSender.class);

  /**
   * The JMSSender to wrap.
   */
  private final JmsSender wrappedSender;

  /**
   * Buffer storing the high priority messages
   * (sent with processValue).
   */
  private SynchroBuffer<SourceDataTagValue> highPriorityBuffer;

  /**
   * Buffer storing the low priority messages
   * (sent with processValues).
   */
  private SynchroBuffer<DataTagValueUpdate> lowPriorityBuffer;

  public ProxyJmsSender(final JmsSender wrappedSender) {
    this.wrappedSender = wrappedSender;
    init();
  }

  /**
   * Init method called on bean initialization.
   */
  private void init() {
    //initialize high priority buffer
    highPriorityBuffer = new SynchroBuffer<>(100, 200, 100, SynchroBuffer.DUPLICATE_OK, 10000);
    highPriorityBuffer.setSynchroBufferListener(new HighPriorityListener());
    highPriorityBuffer.enable();

    lowPriorityBuffer = new SynchroBuffer<>(100, 500, 100, SynchroBuffer.DUPLICATE_OK, 10000);
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
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("pushing SourceDataTagValue into proxy buffer");
    }
    highPriorityBuffer.push(sourceDataTagValue);
  }

  /**
   * @param dataTagValueUpdate the collection of updates to process
   * @throws JMSException not used in proxy
   */
  @Override
  public final void processValues(final DataTagValueUpdate dataTagValueUpdate) throws JMSException {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("pushing DataTagValueUpdate into proxy buffer");
    }
    lowPriorityBuffer.push(dataTagValueUpdate);
  }

  /**
   * The buffer used to store the received collections of updates.
   * @author mbrightw
   *
   */
  private class LowPriorityListener implements SynchroBufferListener<DataTagValueUpdate> {

    /**
     * Retrieve the DataTagValueUpdate objects and call the wrapped processValues method for each
     * of these.
     * @param event pull event
     * @throws PullException not used
     */
    @Override
    public void pull(final PullEvent<DataTagValueUpdate> event) throws PullException {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("entering pull() of proxy low priority buffer...");
        LOGGER.trace("\t Number of pulled dataTagValueUpdate objects (collections!) : " + event.getPulled().size());
      }

      Iterator<DataTagValueUpdate> it = event.getPulled().iterator();

      while (it.hasNext()) {
        //catch and log JMSExceptions (proxy should shield DAQ)
        try {
          wrappedSender.processValues(it.next());
        } catch (Exception ex) {
          LOGGER.error("JMSException caught when calling the proxied JMSSender's processValue method", ex);
        }
      }
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("leaving pull()...");
      }
    }

  }

  /**
   * The buffer used to store the single data tag source values.
   * @author mbrightw
   *
   */
  private class HighPriorityListener implements SynchroBufferListener<SourceDataTagValue> {

    /**
     * Method called when the buffer triggers and event.
     *
     * Simply call the processValue method on the wrapped JMSSender for each
     * {@link SourceDataTagValue} in the buffer.
     * @param event the pull event
     * @throws PullException not used in this case
     */
    @Override
    public void pull(final PullEvent<SourceDataTagValue> event) throws PullException {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("entering pull() of proxy high priority buffer...");
        LOGGER.trace("\t Number of pulled objects : " + event.getPulled().size());
      }

      Iterator<SourceDataTagValue> it = event.getPulled().iterator();

      while (it.hasNext()) {
        //catch and log JMSExceptions (proxy should shield DAQ)
        try {
          wrappedSender.processValue(it.next());
        } catch (Exception ex) {
          LOGGER.error("Exception caught when calling the proxied JMSSender's processValue method: " , ex);
        }
      }
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("leaving pull()...");
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
}
