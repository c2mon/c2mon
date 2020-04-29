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

import java.util.*;

import org.springframework.jms.support.QosSettings;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.daq.common.conf.core.ProcessConfigurationHolder;
import cern.c2mon.daq.config.DaqProperties;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagValueUpdate;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.shared.util.buffer.SynchroBufferQueue;

/**
* Used by {@link ProcessMessageSender} to add {@link SourceDataTagValue} objects
* to a {@link SynchroBufferQueue}. The queue and the corresponding consumer thread is
* managed by this class.
* 
* @author Matthias Braeger
*/
@Slf4j
@AllArgsConstructor
class SynchroBufferFactory {
  
  private final Map<QosSettings, SynchroBufferQueue<SourceDataTagValue>> synchroBufferMap = new HashMap<>();
  
  private final ProcessConfiguration processConfiguration = ProcessConfigurationHolder.getInstance();
  
  private final DaqProperties daqProperties;
  
  private final ProcessMessageSender processMessageSender;
  
  /**
   * Returns the {@link SynchroBufferQueue} for the given Quality-of-Service JMS settings.
   * In case this is the first time, a new {@link SynchroBufferQueue} is created including
   * a new consumer thread. 
   * 
   * @param settings Quality-of-Service JMS settings
   * @return a {@link SynchroBufferQueue}
   */
  synchronized SynchroBufferQueue<SourceDataTagValue> getSynchroBuffer(QosSettings settings) {
    if (!synchroBufferMap.containsKey(settings)) { 
      createSynchroBufferQueue(settings);
    }
    return synchroBufferMap.get(settings);
  }
  
  /**
   * Create new {@link SynchroBufferQueue} and start a new message dispatcher thread.
   * @param settings Quality-of-Service JMS settings
   */
  private void createSynchroBufferQueue(QosSettings settings) {
    SynchroBufferQueue<SourceDataTagValue> buffer = new SynchroBufferQueue<>();
    synchroBufferMap.put(settings, buffer);
    String name = "LOW-MSG-BUFFER-";
    long timeout = daqProperties.getJms().getMaxMessageDelayPriorityLow();
    
    switch (settings.getPriority()) {
      case DataTagConstants.PRIORITY_HIGH:
        timeout = daqProperties.getJms().getMaxMessageDelayPriorityHigh();
        name = "HIGH-MSG-BUFFER-";
        break;
      case DataTagConstants.PRIORITY_MEDIUM:
        timeout = daqProperties.getJms().getMaxMessageDelayPriorityMedium();
        name = "MEDIUM-MSG-BUFFER-";
        break;
      case DataTagConstants.PRIORITY_LOW:
        timeout = daqProperties.getJms().getMaxMessageDelayPriorityLow();
        name = "LOW-MSG-BUFFER-";
        break;
      default:
        log.warn("Got unsupported JMS priority: {}. Please check the DAQ configuration. Will use PRIORITY_LOW", settings.getPriority());
        break;
    }
    startMessageConsumer(name + settings.getTimeToLive(), buffer, timeout);
  }
  
  private void startMessageConsumer(String name, SynchroBufferQueue<SourceDataTagValue> buffer, long timeout) {
    Thread thread = new Thread(() ->  {
      List<SourceDataTagValue> events;
      while (true) {
        try {
          events = buffer.poll(timeout, daqProperties.getJms().getMaxMessageFrameSize());
        } catch (InterruptedException e) {
          log.error("SynchroBuffer polling got interrupted... due to shutdown? -> exiting endless loop.");
          break;
        }
        try {
          sendMessages(events);
        } catch (Exception e) {
          log.error("Exception occured while trying to send data from SynchroBufferQueue. Data might be lost.", e);
        }
        
      }
      
      // Thread is getting ready to die, but first,
      // drain remaining elements on the queue and process them.
      final List<SourceDataTagValue> remainingEvents = new ArrayList<>();
      buffer.drainTo(remainingEvents);
      sendMessages(remainingEvents);
    });
    
    thread.setName(name);
    thread.start();
  }
  
  private void sendMessages(List<SourceDataTagValue> events) {
    DataTagValueUpdate dataTagValueUpdate = createNewDataTagValueUpdate();
    
    events.stream().filter(sdt -> !isMessageExpired(sdt))
                   .forEach(sdt -> dataTagValueUpdate.addValue(sdt));
    sendMessage(dataTagValueUpdate);
  }
  
  private DataTagValueUpdate createNewDataTagValueUpdate() {
    return new DataTagValueUpdate(processConfiguration.getProcessID(), processConfiguration.getprocessPIK());
  }
  
  private void sendMessage(DataTagValueUpdate dataTagValueUpdate) {
    if (!dataTagValueUpdate.getValues().isEmpty()) {
      processMessageSender.distributeValues(dataTagValueUpdate);
      log.debug("\t sent " + dataTagValueUpdate.getValues().size() + " SourceDataTagValue objects");
    }
  }

  /**
   * @param sdtValue the message value that shall be checked
   * @return <code>true</code>, if the message has expired
   */
  private boolean isMessageExpired(final SourceDataTagValue sdtValue) {
    return sdtValue.getTimeToLive() != DataTagAddress.TTL_FOREVER 
        && (sdtValue.getDaqTimestamp().getTime() + sdtValue.getTimeToLive()) < System.currentTimeMillis();
  }
}
