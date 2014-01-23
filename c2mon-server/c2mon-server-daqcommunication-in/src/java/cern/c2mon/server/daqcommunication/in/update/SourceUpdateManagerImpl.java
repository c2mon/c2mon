/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.daqcommunication.in.update;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ControlTagFacade;
import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.thread.Event;
import cern.c2mon.server.supervision.SupervisionManager;
import cern.c2mon.shared.daq.datatag.DataTagValueUpdate;
import cern.c2mon.shared.daq.datatag.DataTagValueUpdateConverter;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;

/**
 * Implementation of the bean processing incoming updates from the
 * DAQ layer. Notice we use the {@link SessionAwareMessageListener} interface here
 * because of the better exception handling when using Spring containers (JMSException
 * is thrown to Spring container, which can then rollback the transaction).
 * 
 * @author Mark Brightwell
 *
 */
@Service("sourceUpdateManager")
@ManagedResource(objectName = "cern.c2mon:name=sourceUpdateManager")
public class SourceUpdateManagerImpl implements SourceUpdateManager, SessionAwareMessageListener<Message> {

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(SourceUpdateManagerImpl.class);
  
  /**
   * SMS logger.
   */
  private static final Logger SMSLOGGER = Logger.getLogger("AdminSmsLogger");
  
  /**
   * Email logger.
   */
  private static final Logger EMAILLOGGER = Logger.getLogger("AdminEmailLogger");
  
  /**
   * Time of last email log. Will only send every 10min.
   */
  private long lastEmailLog;
  private static final long EMAIL_FREQUENCY_MILLIS = 600000;
  
  /**
   * For converting JMS messages to updates.
   */
  private final DataTagValueUpdateConverter converter;
 
  /**
   * Reference to DataTagFacade bean (singleton).
   */
  private final DataTagFacade dataTagFacade;
 
  /**
   * Reference to the facade for control tags.
   */
  private final ControlTagFacade controlTagFacade;
  
  /**
   * Reference to the supervision manager.
   */
  private final SupervisionManager supervisionManager;
  
  /**
   * Reference to the Process Facade needed to check the process PIK (we avoid to use the Process Cache)
   */
  private final ProcessFacade processFacade;
  
  /**
   * Reference to the Process Cache 
   */
  private final ProcessCache processCache;
  
  /**
   * For management only. Number of JMS threads
   * currently active.
   */
  private volatile AtomicInteger activeUpdateThreads = new AtomicInteger(0);
 
  /**
   * Only send warning every 100000 source update messages when problem occurs. 
   */
  private static final int WARNING_FREQUENCY = 100000;
  private volatile AtomicInteger warningCount= new AtomicInteger(0);
  
  /**
   * Alarm on (warning email sent and no OK email sent)
   */
  private volatile boolean alarmActive = false;
  
  /**
   * "Back to normal" countdown before email message.
   */
  private static final int SWITCH_OFF_COUNTDOWN = 10000;
  private volatile AtomicInteger switchOffCountDown = new AtomicInteger(SWITCH_OFF_COUNTDOWN);
  
  /**
   * IGNORE_UPDATE
   */
  private static final Boolean IGNORE_UPDATE = false;
  
  /**
   * ACCEPT_UPDATE
   */
  private static final Boolean ACCEPT_UPDATE = true;
    
  
  /**
   * Autowired constructor.
   */
  @Autowired
  public SourceUpdateManagerImpl(final DataTagFacade dataTagFacade, 
                                 final ControlTagFacade controlTagFacade, 
                                 final SupervisionManager supervisionManager, 
                                 final DataTagValueUpdateConverter dataTagValueUpdateConverter,
                                 final ProcessFacade processFacade,
                                 final ProcessCache processCache) {
    super();
    this.dataTagFacade = dataTagFacade;
    this.controlTagFacade = controlTagFacade;
    this.supervisionManager = supervisionManager; 
    this.converter = dataTagValueUpdateConverter;
    this.processFacade = processFacade;
    this.processCache = processCache;
  }
  
  /**
   * Implementation of the interface of the Source Update Management module.
   * 
   * The incoming JMS message is only acknowledged once this method has returned successfully
   * (since this method is specified as listener method in the JCA container; the transaction
   * manager manages the acknowledgment).
   * 
   * @param dataTagValueUpdate the incoming collection of updates (could be null if some error
   * occurred in converting the message; in this case, log the problem and ignore this update)
   * @throws NullPointerException if passed DataTagValueUpdate is null
   */
  @Override
  public void processUpdates(final DataTagValueUpdate dataTagValueUpdate) {
    try {
      activeUpdateThreads.getAndIncrement();
      Collection<SourceDataTagValue> values = dataTagValueUpdate.getValues();
      if (values != null && values.size() > 0) {
        
        // process each incoming tag value
        Iterator<SourceDataTagValue> it = values.iterator();
        while (it.hasNext()) {
          SourceDataTagValue sourceDataTagValue = it.next();
          //if the incoming value is a control tag (i.e. alive or commFault)
          if (sourceDataTagValue.isControlTag()) {
            processControl(sourceDataTagValue);
          } else {
            //else is a normal DataTag update
            processDataTag(sourceDataTagValue);
          }
          //log in file          
          sourceDataTagValue.log();          
        }                            
      }
    } finally {
      activeUpdateThreads.getAndDecrement();
      if (activeUpdateThreads.get() > 100) {
        alarmActive = true;
        switchOffCountDown = new AtomicInteger(SWITCH_OFF_COUNTDOWN);
        if (warningCount.getAndIncrement() % WARNING_FREQUENCY == 0) {
          SMSLOGGER.warn("Over 100 source update threads active.");
        }        
      } else if (alarmActive && switchOffCountDown.getAndDecrement() == 0) {
        alarmActive = false;
        SMSLOGGER.warn("Number of active update threads back to normal.");
        warningCount = new AtomicInteger(0);
      }
    }        
  }
  
  @Override
  public void onMessage(final Message message, final Session session) throws JMSException {
    try {
      DataTagValueUpdate update = (DataTagValueUpdate) converter.fromMessage(message);
      
      // We do the process PIK checking in order to accept or not the update
      if(this.checkProcessPIK(update)) {
        processUpdates(update);
      }
      else {
        LOGGER.warn("onMessage - Received update(s) for Process " + update.getProcessId() 
                    + " with wrong PIK  ==> Ignoring " + update.getValues().size() + " updates.");
      }
    } catch (MessageConversionException ex) {  
      String errorMessage = "MessageConversionException caught on incoming update from DAQ: message is being discarded!";
      LOGGER.error(errorMessage, ex);
      if (System.currentTimeMillis() - lastEmailLog > EMAIL_FREQUENCY_MILLIS) {
        EMAILLOGGER.error(errorMessage, ex);
        lastEmailLog = System.currentTimeMillis();
      }      
    }
  }
  
  /**
   * Performs all operations needed on reception of a control tag. Currently very similar to
   * processDataTag method and uses the {@link DataTagFacade} to update the ControlTagCacheObject as it
   * coincides with the underlying DataTagCacheObject. In the future, may with to implement a
   * controlTagFacade if the handling of the Control tags is modified.
   * 
   * <p>Does not synchronize on the control tag, as this is taken care of in the DataTagFacade.
   * 
   * 
   * @param sourceDataTagValue the incoming control tag
   */
  private void processControl(final SourceDataTagValue sourceDataTagValue) {   
    try {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("processControl - Processing incoming update for ControlTag " + sourceDataTagValue.getId());
      }
      Event<Boolean> updatedInCache = controlTagFacade.updateFromSource(sourceDataTagValue.getId(), sourceDataTagValue);     
      if (updatedInCache.getReturnValue()) {
        supervisionManager.processControlTag(sourceDataTagValue); //filter out events that were updated later in the cache
      }
    } catch (CacheElementNotFoundException cacheEx) {      
      LOGGER.warn("processControl - Received unrecognized control tag " + sourceDataTagValue.getId() + " - ignoring the update.", cacheEx);
    }  
  }
  
  /**
   * Performs the necessary operations on reception of a data tag.
   * @param sourceDataTagValue the incoming data tag value
   * @throw NullPointerException if passed null SourceDataTagValue
   */
  private void processDataTag(final SourceDataTagValue sourceDataTagValue) {        
    try {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("processDataTag - Processing incoming update for DataTag " + sourceDataTagValue.getId());
      }
      dataTagFacade.updateFromSource(sourceDataTagValue.getId(), sourceDataTagValue);      
    } catch (CacheElementNotFoundException cacheEx) {      
      LOGGER.warn("processDataTag - Received unrecognized data tag " + sourceDataTagValue.getId() + " - ignoring the update.", cacheEx);      
    }    
  }

  /**
   * For management onlu.
   * @return the number of JMS container threads currently running in the server
   */
  @ManagedAttribute(description = "Number of JMS container threads currently running in the server")
  public final AtomicInteger getActiveUpdateThreads() {
    return activeUpdateThreads;
  } 
  
  /**
   * Function to check all possible process PIK scenarios
   * 
   * @param dataTagValueUpdate Update with all information
   * @return {@link #IGNORE_UPDATE} if PIK registered in server but no PIK or wrong PIK sent
   *         {@link #ACCEPT_UPDATE} in any other case
   */
  private Boolean checkProcessPIK(final DataTagValueUpdate dataTagValueUpdate) {
    Process process;
    
    processCache.acquireWriteLockOnKey(dataTagValueUpdate.getProcessId());
    try {
      process = this.processCache.get(dataTagValueUpdate.getProcessId());
      
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("checkProcessPIK - Processing incoming update for DataTag " + process.getName());
      }

      // if PIK is registered in Server
      if (process.getProcessPIK() != null) {
        // If no PIK sent by the DAQ update or wrong PIK is sent by DAQ update ignore message
        if (dataTagValueUpdate.getProcessPIK() == null) {
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("checkProcessPIK - Processing incoming update for DataTag " + process.getName() + 
                " PIK registered in Server(" + process.getProcessPIK() + ") but no PIK received from update - Ignoring the update.");
          }

          // TODO: Send disconnection
          return IGNORE_UPDATE;
        } else if (!process.getProcessPIK().equals(dataTagValueUpdate.getProcessPIK())) {
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("checkProcessPIK - Processing incoming updates for Process ID " + process.getName() + 
                " Received wrong PIK - cache vs update (" + process.getProcessPIK() + " vs " + 
                dataTagValueUpdate.getProcessPIK() + ") - Ignoring the update.");
          }

          // TODO: Send disconnection
          return IGNORE_UPDATE;
        }
      }
      // If no PIK register in server cache (ie. corrupted) save the PIK and Accept
      else {
        // Backward compatibility use case
        if (dataTagValueUpdate.getProcessPIK() == null) {
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("checkProcessPIK - Processing incoming update for Process ID " + process.getName() + " with no PIK (Backward Compatibility)");
          }

          return ACCEPT_UPDATE;
        }

        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("checkProcessPIK - Processing incoming update for Process ID " + process.getName() + " and saving PIK " + dataTagValueUpdate.getProcessPIK());
        }
        this.processFacade.setProcessPIK(process.getId(), dataTagValueUpdate.getProcessPIK());
      }
    } catch (CacheElementNotFoundException cacheEx) {      
      LOGGER.warn("checkProcessPIK - Receive updates from unrecognized Process ID " + dataTagValueUpdate.getProcessId() +  " - Ignoring the updates.", cacheEx);
    } finally {
      processCache.releaseWriteLockOnKey(dataTagValueUpdate.getProcessId());
    } 

    // If no problems we accept the update
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("checkProcessPIK - Processing incoming update for Process ID " + dataTagValueUpdate.getProcessId() + " with PIK " + dataTagValueUpdate.getProcessPIK());
    }

    return ACCEPT_UPDATE;
  }

}
