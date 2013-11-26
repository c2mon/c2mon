/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
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
package cern.c2mon.daq.common.messaging.impl;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.conf.core.RunOptions;
import cern.c2mon.daq.common.jmx.JmxRegistrationMXBean;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.common.messaging.JmsSender;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.daq.datatag.DataTagValueUpdate;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTagValue;
import cern.tim.util.buffer.PullEvent;
import cern.tim.util.buffer.PullException;
import cern.tim.util.buffer.SynchroBuffer;
import cern.tim.util.buffer.SynchroBufferListener;

/**
 * The ProcessMessageSender class is responsible for sending JMS messages from
 * the daq to the application server. This class supports sending the updates
 * to multiple JMS connections. The sending itself is performed by a JMSSender
 * class. Several of these can be specified in the jmsSenders collection field.
 * Notice that the calls to these senders are made on the same threads, so it is
 * up to the JMSSenders to release the threads, if for instance they are not
 * critical (see @see cern.c2mon.daq.common.messaging.ActiveProxySender for
 * wrapping non-essential JMSSender's).
 * 
 * For low priority messages, two synchrobuffer's are used (one for persistent,
 * the other for non-persistent messages).
 */
public class ProcessMessageSender implements IProcessMessageSender {

    /**
     * The buffer for non-persistent SourceDataTags objects
     */
    private SynchroBuffer dataTagsBuffer;

    /**
     * The buffer for persistent SourceDataTags objects
     */
    private SynchroBuffer persistentTagsBuffer;

    /**
     * The reference for the AliveTimer object
     */
    private AliveTimer aliveTimer;

    /**
     * The collection of JMS senders (each responsible for sending updates to a
     * specific broker). Injected in Spring XML configuration file.
     */
    private Collection<JmsSender> jmsSenders;

    /**
     * The system's logger
     */
    private static final Logger LOGGER = Logger.getLogger(ProcessMessageSender.class);

    /**
     * The reference for the ConfigurationController object.
     */
    private ConfigurationController configurationController;

    /**
     * Creates a new ProcessMessageSender.
     * 
     * @param configurationController The configurationController to access
     * all configuration values.
     */
    @Autowired
    public ProcessMessageSender(final ConfigurationController configurationController) {
        this.configurationController = configurationController;
    }

    /**
     * Init method called on bean initialization.
     */
    @PostConstruct
    public void init() {
        aliveTimer = new AliveTimer(this);

        ProcessConfiguration processConfiguration = getConfigurationController().getProcessConfiguration();
        // TODO move the min window size to properties or database
        // create and initialize dataTagsBuffer for non-persistent tags
        dataTagsBuffer = new SynchroBuffer(200, processConfiguration.getMaxMessageDelay(), 100, SynchroBuffer.DUPLICATE_OK);
        // create and initialize dataTagsBuffer for persistent tags
        persistentTagsBuffer = new SynchroBuffer(200, processConfiguration.getMaxMessageDelay(), 100, SynchroBuffer.DUPLICATE_OK);

        dataTagsBuffer.setSynchroBufferListener(new SynchroBufferEventsListener());
        persistentTagsBuffer.setSynchroBufferListener(new SynchroBufferEventsListener());

        dataTagsBuffer.enable();
        persistentTagsBuffer.enable();
    }

    /**
     * This method initializes and starts the AliveTimer. Since it's initialized
     * it periodically takes action to send AliveTag to TIM server (using
     * ProcessMessageSender's JMS queue connection)
     */
    public final void startAliveTimer() {
        ProcessConfiguration processConfiguration = getConfigurationController().getProcessConfiguration();
        aliveTimer.setInterval(processConfiguration.getAliveInterval());
    }
    
    /**
     * Stops the Process alive timer. Used at final DAQ shutdown.
     */
    public final void stopAliveTimer() {
      aliveTimer.terminateTimer();
    }

    /**
     * This method is responsible for creating a JMS XML message containing
     * alive tag and putting it to the TIM JMS queue
     */
    public final void sendAlive() {
        ProcessConfiguration processConfiguration = getConfigurationController().getProcessConfiguration();
        LOGGER.debug("sending AliveTag. tag id : " + processConfiguration.getAliveTagID());

        // Just to know what are the arguments :
        // SourceDataTagValue(Long id,
        // String name,
        // boolean controlTag,
        // Object value,
        // SourceDataQuality quality,
        // long timestamp,
        // int priority,
        // boolean guaranteedDelivery,
        // int timeToLive)

        long timestamp = System.currentTimeMillis();
        try {
            SourceDataTagValue aliveTagValue = new SourceDataTagValue(Long.valueOf(processConfiguration.getAliveTagID()), processConfiguration.getProcessName() + "::AliveTag", true, Long
                    .valueOf(timestamp), new SourceDataQuality(), timestamp,
            /* DataTagAddress.PRIORITY_HIGH */9, // set the highest possible
                                                 // prority
                    false, null, 3 * processConfiguration.getAliveInterval());
            distributeValue(aliveTagValue);
        } catch (JMSException ex) {
            LOGGER.error("sendAlive : JMSException caught :" + ex.getMessage());
        } catch (Throwable e) {
            LOGGER.error("sendAlive : Unexpected Exception caught :", e);
        }

    }

    /**
     * This methods is responsible for sending CommFaultTag message
     * 
     * @param tagID
     *            The CommFaultTag identifier
     */
    public final synchronized void sendCommfaultTag(final long tagID, final Object value) {
        sendCommfaultTag(tagID, value, null);
    }

    public void sendCommfaultTag(long tagID, Object value, String pDescription) {
        ProcessConfiguration processConfiguration = getConfigurationController().getProcessConfiguration();
        LOGGER.debug("Sending CommfaultTag. tag id : " + tagID);

        // Just to know what are the arguements :
        // SourceDataTagValue(Long id,
        // String name,
        // boolean controlTag,
        // Object value,
        // SourceDataQuality quality,
        // long timestamp,
        // int priority,
        // boolean guaranteedDelivery,
        // int timeToLive)

        long timestamp = System.currentTimeMillis();
        SourceDataTagValue commfaultTagValue = new SourceDataTagValue(Long.valueOf(tagID), processConfiguration.getProcessName() + "::CommFaultTag", true, value, new SourceDataQuality(), timestamp,
                DataTagAddress.PRIORITY_HIGH, false, pDescription, 9999999);

        try {
            distributeValue(commfaultTagValue);
        } catch (JMSException ex) {
            LOGGER.error("sendCommfaultTag : JMSException caught :" + ex.getMessage());
        }
    }

    @Override
    public final void addValue(final SourceDataTagValue dataTagValue) {
        LOGGER.debug("entering addValue()..");
        LOGGER.debug("adding data tag " + dataTagValue.getId() + " to a sending buffer");
        if (dataTagValue.getPriority() == DataTagAddress.PRIORITY_HIGH) {
            LOGGER.debug("\t sourceDataTagValue priority is HIGH");
            try {
                this.distributeValue(dataTagValue);
            } catch (JMSException ex) {
                LOGGER.error("addValue : JMSException caught :" + ex.getMessage());
            }
        } else {
            LOGGER.debug("\t sourceDataTagValue priority is LOW");
            // check whether it's message with guaranteed delivery or not
            if (dataTagValue.isGuaranteedDelivery()) {
                LOGGER.debug("\t guaranteedDelivery is TRUE");

                // note : synchrobuffer's push method is thread-safety,
                // so no external synchronization is needed
                this.persistentTagsBuffer.push(dataTagValue);
            } else {
                LOGGER.debug("\t guaranteedDelivery is FALSE");

                // note : synchrobuffer's push method is thread-safety,
                // so no external synchronization is needed
                this.dataTagsBuffer.push(dataTagValue);
            }
        }

        LOGGER.debug("leaving addValue()");
    }

    /**
     * Connects to all the registered brokers (individual JMSSenders should
     * implement this on separate threads if the connection is unessential).
     */
    public final void connect() { 
        Iterator<JmsSender> it = jmsSenders.iterator();
        while (it.hasNext()) {
          JmsSender jmsSender = it.next();
          
          // Helper class for registering the MXBean to the current server
          JmxRegistrationMXBean jmxRegistrationMXBean = new JmxRegistrationMXBean(JmxRegistrationMXBean.MBeanType.JMS, jmsSender.getBeanName());
          
          // Register JmsSender as MXBean
          jmxRegistrationMXBean.registerMBean(jmsSender);
         
          // Connection
          jmsSender.connect();
        }
    }

    /**
     * This methods gently closes and disables ProcessMessageSender's
     * synchrobuffers.
     */
    public final void closeSourceDataTagsBuffers() {
        dataTagsBuffer.disable();
        dataTagsBuffer.close();
        persistentTagsBuffer.disable();
        persistentTagsBuffer.close();
    }

    /**
     * Forwards the value to all the JMS senders.
     * 
     * @param sourceDataTagValue
     *            the value to send
     * @throws JMSException
     *             if one of the senders fails
     */
    private void distributeValue(final SourceDataTagValue sourceDataTagValue) throws JMSException {
        Iterator<JmsSender> it = jmsSenders.iterator();
        while (it.hasNext()) {
          try {
            it.next().processValue(sourceDataTagValue);
          } catch (Exception e) {
            LOGGER.error("Unhandled exception caught while sending a source value (tag id " + sourceDataTagValue.getId() + ") - the value update will be lost.", e);
          }            
        }
        // log value in appropriate log file
        sourceDataTagValue.log();
    }

    /**
     * Forwards the list of values to all the JMS senders.
     * 
     * @throws JMSException
     *             if one of the senders throws one (individual senders should
     *             also listen to these locally to take any necessary action)
     * @param dataTagValueUpdate
     *            the values to send
     */
    private void distributeValues(final DataTagValueUpdate dataTagValueUpdate) throws JMSException {
        Iterator<JmsSender> it = jmsSenders.iterator();
        while (it.hasNext()) {
          try {
            it.next().processValues(dataTagValueUpdate);
          } catch (Exception e) {
            LOGGER.error("Unhandled exception caught while sending a collection of source values - the updates will be lost.", e);
          }            
        }
        // log value in appropriate log file
        dataTagValueUpdate.log();
    }

    /**
     * Setter method.
     * 
     * @param jmsSenders
     *            the jmsSenders to set
     */
    public final void setJmsSenders(final Collection<JmsSender> jmsSenders) {
        this.jmsSenders = jmsSenders;
    };

    public void setConfigurationController(final ConfigurationController configurationController) {
        this.configurationController = configurationController;
    }

    public ConfigurationController getConfigurationController() {
        return configurationController;
    }

    /**
     * This class implements SynchroBuffer's SychroBufferListener, so that both
     * ProcessMessageSender's tag buffers (for persistent and non-persistent)
     * tags are able to handle Pull events.
     */
    class SynchroBufferEventsListener implements SynchroBufferListener {

        /**
         * This method is called by Synchorbuffer, each time a PullEvent occurs.
         * 
         * @param event
         *            the pull event, containing the collection of objects to be
         *            sent
         * @throws ch.cern.tim.tools.synchrobuffer.PullException
         * @throws cern.laser.util.buffer.PullException
         */
        public void pull(PullEvent event) throws PullException {
            ProcessConfiguration processConfiguration = getConfigurationController().getProcessConfiguration();
            LOGGER.debug("entering pull()..");
            LOGGER.debug("\t Number of pulled objects : " + event.getPulled().size());
            
            RunOptions runOptions = getConfigurationController().getRunOptions();
            // If we don't work with the PIK we act as we used to before PIK era, else we add the PIK to our communication process
            DataTagValueUpdate dataTagValueUpdate;
            if (runOptions.isNoPIK()) {
              dataTagValueUpdate = new DataTagValueUpdate(processConfiguration.getProcessID());
            }
            else {
              dataTagValueUpdate = new DataTagValueUpdate(processConfiguration.getProcessID(), processConfiguration.getprocessPIK());
            }

            Iterator it = event.getPulled().iterator();
            long currentMsgSize = 0;

            while (it.hasNext()) {
                // check if the maximum allowed message size has been reached
                if (currentMsgSize == processConfiguration.getMaxMessageSize()) {
                    try {
                        // send the message
                        distributeValues(dataTagValueUpdate);
                        // clear the dataTagValueUpdate object reference !!
                        dataTagValueUpdate = null;
                        LOGGER.debug("\t sent " + currentMsgSize + " SourceDataTagValue objects");
                    } catch (JMSException ex) {
                        LOGGER.error("\tpull : JMSException caught while invoking processValues methods :" + ex.getMessage());
                    }

                    // clear the message size counter
                    currentMsgSize = 0;
                    
                    // create new dataTagValueUpdate object
                    
                    // If we don't work with the PIK we act as we used to before PIK era, else we add the PIK to our communication process
                    if (runOptions.isNoPIK()) {
                      dataTagValueUpdate = new DataTagValueUpdate(processConfiguration.getProcessID());
                    }
                    else {
                      dataTagValueUpdate = new DataTagValueUpdate(processConfiguration.getProcessID(), processConfiguration.getprocessPIK());
                    }
                } // if

                // append next SourceDataTagValue object to the message
                dataTagValueUpdate.addValue((SourceDataTagValue) it.next());
                // increase the message size counter
                currentMsgSize++;
            } // while

            // find out if there'was something left inside dataTagValueUpdate
            // if yes - prepare the message and send it too
            if (dataTagValueUpdate != null && currentMsgSize > 0) {
                try {
                    distributeValues(dataTagValueUpdate);
                    LOGGER.debug("\t sent " + dataTagValueUpdate.getValues().size() + " SourceDataTagValue objects");
                } catch (JMSException ex) {
                    LOGGER.error("\t pull : JMSException caught while invoking processValues methods :" + ex.getMessage());
                }
            } // if

            LOGGER.debug("leaving pull method");
        }
    }

    /**
     * Shuts down all JmsSenders.
     */
    public void shutdown() {
      Iterator<JmsSender> it = jmsSenders.iterator();
      while (it.hasNext()) {
          it.next().shutdown();
      }
    }
}
