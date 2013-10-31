/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.filter;

import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;

import org.apache.log4j.Logger;

import cern.c2mon.daq.common.conf.core.CommonConfiguration;
import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.messaging.JmsLifecycle;
import cern.tim.shared.daq.filter.FilteredDataTagValue;
import cern.tim.shared.daq.filter.FilteredDataTagValueUpdate;
import cern.tim.util.buffer.PullEvent;
import cern.tim.util.buffer.PullException;
import cern.tim.util.buffer.SynchroBuffer;
import cern.tim.util.buffer.SynchroBufferListener;


/**
 * Partial implementation of an IFilterMessageSender. Also currently
 * provides the abstract lifecycle methods needed by the DriverKernel.
 * 
 * <p>On shutdown, call the closeTagBuffer() method,
 * which will wait for the buffer to empty before returning.
 * 
 * @author Mark Brightwell
 * 
 */

public abstract class FilterMessageSender implements IFilterMessageSender, JmsLifecycle {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(FilterMessageSender.class); 
  
  /**
   * The SynchroBuffer minimum window size.
   */
  private static final long MIN_WINDOW_SIZE = 200;

  /**
   * The SynchroBuffer window growth factor.
   */
  private static final int WINDOW_GROWTH_FACTOR = 100;

  /**
   * The maximum message delay for the filter module SynchroBuffer.
   */
  private static final long MAX_MESSAGE_DELAY = 1000;

  /**
   * The maximum message size (in terms of number of tag values sent per
   * message)
   */
  private static final int MAX_MESSAGE_SIZE = 100;

  /**
   * The buffer used for collecting the tag update values before sending.
   */
  private SynchroBuffer tagBuffer;
  
  /**
   * The configuration controller to access all the configuration objects.
   */
  protected ConfigurationController configurationController;
    
  /**
   * Sends the update collection to the filter queue.
   * @param filteredDataTagValueUpdate the update to send
   * @throws JMSException if a problem occurs during sending
   */
  protected abstract void processValues(final FilteredDataTagValueUpdate filteredDataTagValueUpdate) throws JMSException;
  
  /**
   * Constructor.
   * @param configurationController for accessing the DAQ configuration
   */
  public FilterMessageSender(final ConfigurationController configurationController) {
    super();
    this.configurationController = configurationController;
  }

  /**
   * Method run at bean initialization.
   */
  @PostConstruct
  public void init() {
      CommonConfiguration commonConf = configurationController.getCommonConfiguration();
      // set up and enable the synchrobuffer for storing the tags
      if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("initializing filtering synchrobuffer with max delay :" + MAX_MESSAGE_DELAY + " and capacity : " + commonConf.getFilterBufferCapacity());
      }

      tagBuffer = new SynchroBuffer(MIN_WINDOW_SIZE, MAX_MESSAGE_DELAY, WINDOW_GROWTH_FACTOR, SynchroBuffer.DUPLICATE_OK, commonConf.getFilterBufferCapacity(), true);
      tagBuffer.setSynchroBufferListener(new SynchroBufferEventsListener());
      tagBuffer.enable();
  }
  
  /**
   * This method is called from other classes to pass a datatag value for
   * sending to the Filter module. It currently simply adds them to the
   * SynchroBuffer.
   * 
   * @param dataTagValue
   *            a datatag value to be sent
   */
  @Override
  public final void addValue(final FilteredDataTagValue dataTagValue) {
      if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("entering addValue()...");
          LOGGER.debug("\tadding value to buffer");
      }

      tagBuffer.push(dataTagValue);

      if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("...leaving addValue()");
      }

  }
  
  /**
   * Returns the current size of the buffer used for storing the fitlered values.
   * @return size of the internal buffer
   */
  protected int getBufferSize() {
    return tagBuffer.getSize();    
  }
  
  /**
   * Closes the SynchroBuffer on disconnecting.
   */
  protected void closeTagBuffer() {
      tagBuffer.disable();
      //wait the max time to allow the buffer to empty, then empty it if it fails.
      try {
        Thread.sleep(MAX_MESSAGE_DELAY);
      } catch (InterruptedException e) {
        LOGGER.warn("Interrupted exception caught while waiting for filter buffer to empty", e);
      }
      tagBuffer.empty();
      tagBuffer.close();   
  }
  
  /**
   * This class is hooked up with the tagBuffer SynchroBuffer. When a
   * PullEvent is triggered by the SynchroBuffer, the pull method below is
   * called, which then processes all tag values in the buffer.
   * 
   * @author mbrightw
   * 
   */
  class SynchroBufferEventsListener implements SynchroBufferListener {
      /**
       * When a PullEvent occurs, collects all tag values in the buffer into
       * DataTagValueUpdate objects and forwards them to the processValues
       * method (which then sends them to the JMS broker).
       * 
       * @param event
       *            the PullEvent triggered by the SynchroBuffer
       * @throws PullException
       *             exception in SychroBuffer pull method
       */
      public void pull(final PullEvent event) throws PullException {
          if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("entering FilterMessageSender pull()...");
              LOGGER.debug("\t Number of pulled objects : " + event.getPulled().size());
          }
          ProcessConfiguration pconf = configurationController.getProcessConfiguration();
          FilteredDataTagValueUpdate dataTagValueUpdate = new FilteredDataTagValueUpdate(pconf.getProcessID());

          Iterator it = event.getPulled().iterator();
          long currentMsgSize = 0;

          // iterate through all pulled SourceDataTagValue's
          while (it.hasNext()) {
              // check if the maximum allowed message size has been reached;
              if (currentMsgSize == MAX_MESSAGE_SIZE) {
                  // if so, send them (the values have been gathered in the
                  // dataTagValueUpdate object)
                  try {

                      // send the message
                      processValues(dataTagValueUpdate);

                      // clear the dataTagValueUpdate object reference for the
                      // next batch of values
                      dataTagValueUpdate = null;

                      if (LOGGER.isDebugEnabled()) {
                          LOGGER.debug("\t sent " + currentMsgSize + " SourceDataTagValue objects to Statistics module");
                      }
                  } catch (JMSException ex) {
                      LOGGER.error("\tpull : JMSException caught while invoking processValue method:" + ex.getMessage());
                  }

                  // create new dataTagValueUpdate object for the next batch
                  // of tag values
                  dataTagValueUpdate = new FilteredDataTagValueUpdate(pconf.getProcessID());

                  // clear the message size counter
                  currentMsgSize = 0;

              } // if

              // append next SourceDataTagValue object to the message
              dataTagValueUpdate.addValue((FilteredDataTagValue) it.next());

              // increase the message size counter
              currentMsgSize++;

          } // while

          // the final batch of values in dataTagValueUpdate still needs
          // sending, if not empty
          if (dataTagValueUpdate != null && currentMsgSize > 0) {
              try {
                  processValues(dataTagValueUpdate);
                  if (LOGGER.isDebugEnabled()) {
                      LOGGER.debug("\t sent " + dataTagValueUpdate.getValues().size() + " FilteredDataTagValue objects");
                  }
              } catch (JMSException ex) {
                  LOGGER.error("\t pull : JMSException caught while invoking processValue method :" + ex.getMessage());
              }
          } // if
          if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("leaving FilterMessageSender pull method");
          }
      }
  }
}
