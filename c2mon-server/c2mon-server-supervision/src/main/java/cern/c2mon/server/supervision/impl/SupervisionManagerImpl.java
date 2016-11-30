/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.supervision.impl;

import java.sql.Timestamp;
import java.util.Collection;

import javax.annotation.Resource;

import cern.c2mon.server.common.config.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.ControlTagFacade;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.cache.ProcessXMLProvider;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject.LocalConfig;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.supervision.SupervisionManager;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import cern.c2mon.shared.daq.process.ProcessConfigurationRequest;
import cern.c2mon.shared.daq.process.ProcessConfigurationResponse;
import cern.c2mon.shared.daq.process.ProcessConnectionRequest;
import cern.c2mon.shared.daq.process.ProcessConnectionResponse;
import cern.c2mon.shared.daq.process.ProcessDisconnectionRequest;
import cern.c2mon.shared.daq.process.XMLConverter;


/**
 *
 * Implementation of the SupervisionManager.
 *
 * <p>The class is designed with 3 layers:
 *
 * <ul>
 * <li>public methods (on alive expiration or process connection/disconnection)
 * <li>private "on"."Process/Equipment/SubEquipment"."Down/Up" (called when informed of current status)
 * <li>other private methods, including "invalidate"."Equipment/SubEquipment" methods
 * </ul>
 *
 * <p>The lifecycle start() method is called by the server (single one in a clustered environment)
 * in the final start up phase and activates all the alive timers. The timer itself is
 * in the AliveTimerManager and is activated there.
 *
 */
@Service("supervisionManager")
public class SupervisionManagerImpl implements SupervisionManager, SmartLifecycle {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SupervisionManagerImpl.class);

  @Resource
  private ProcessCache processCache;

  @Resource
  private EquipmentCache equipmentCache;

  @Resource
  private EquipmentFacade equipmentFacade;

  @Resource
  private SubEquipmentCache subEquipmentCache;

  @Resource
  private SubEquipmentFacade subEquipmentFacade;

  @Resource
  private ControlTagCache controlTagCache;

  @Autowired
  private ControlTagFacade controlTagFacade;

  @Resource
  private CommFaultTagCache commFaultTagCache;

  @Resource
  private AliveTimerFacade aliveTimerFacade;

  @Resource
  private AliveTimerCache aliveTimerCache;

  @Resource
  private ProcessFacade processFacade;

  @Resource
  private ProcessXMLProvider processXMLProvider;

  @Resource
  private ClusterCache clusterCache;

  @Resource
  private ServerProperties properties;

  //@Resource
  //private DataRefreshManager dataRefreshManager;

  /**
   * XML Converter helper class
   */
  private XMLConverter xmlConverter = new XMLConverter();

  /**
   * Starts the alive timer mechanisms at server start up.
   */
  @Override
  public void start() {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("starting all alive timers");
    }

    //the purpose of this synch block is to wait for all caches to be loaded (from DB only, not "refreshing to node" which can be simultaneous!)
    //before starting the alive mechanism: the alive mechanism needs several of
    //these caches and we don't want any server instance up and running before
    //the entire cache has been loaded (another server might still be loading
    //the datatag cache when this instance reaches this point; all cache loads
    //hold a distributed read lock on this lock)
    //Once the lock is write-available, only one server wills start the alive
    //timers, although they all start the timer mechanism on their JVM).
    clusterCache.acquireWriteLockOnKey(C2monCacheLoader.aliveStatusInitialized);
    try {
      if (!clusterCache.hasKey(C2monCacheLoader.aliveStatusInitialized) || !(Boolean) clusterCache.getCopy(C2monCacheLoader.aliveStatusInitialized)) {
        Long aliveTagId;

        //TODO the three sections below unnecessary?
        Process process;
        for (Long id : processCache.getKeys()) {
          processCache.acquireReadLockOnKey(id);
          try {
            process = processCache.get(id); //TODO should never be fake, unless config loader removes processes during runtime
            //TODO add here a check to see if the process is disused - need to add field to process,
            //     equipment (and use mode in datatag)
            //     comment: may not need this after all, just remove from cache when disused
            aliveTagId = process.getAliveTagId();
            if (aliveTagId != null) {
              if (controlTagCache.hasKey(process.getStateTagId())) {
                if (processFacade.isRunning(process)) {
                  aliveTimerFacade.start(aliveTagId);
                }
              } else {
                LOGGER.warn("Unable to locate state tag in cache (id = " + process.getStateTagId() + ") " +
                    "cannot start alive timer for this process.");
              }
            }
          } finally {
            processCache.releaseReadLockOnKey(id);
          }
        }

        //start equipment alive timers
        Equipment equipment;
        for (Long id : equipmentCache.getKeys()) {
          equipmentCache.acquireReadLockOnKey(id);
          try {
            equipment = equipmentCache.get(id); //TODO should never be fake, unless config loader removes processes during runtime
            //TODO add here a check to see if the equipement is disused - need to add field to process, equipment (and use mode in datatag)
            aliveTagId = equipment.getAliveTagId();
            if (aliveTagId != null) {
              if (controlTagCache.hasKey(equipment.getStateTagId())) {
                if (equipmentFacade.isRunning(equipment)) {
                  aliveTimerFacade.start(aliveTagId);
                }
              } else {
                LOGGER.warn("Unable to locate state tag in cache (id = " + equipment.getStateTagId() + ") " +
                    "cannot start alive timer for this equipment.");
              }
            }
          } finally {
            equipmentCache.releaseReadLockOnKey(id);
          }
        }

        //start subequipment alive timers
        SubEquipment subEquipment;
        for (Long id : subEquipmentCache.getKeys()) {
          subEquipmentCache.acquireReadLockOnKey(id);
          try {
            subEquipment = subEquipmentCache.get(id); //TODO should never be fake, unless config loader removes processes during runtime

            // TODO add here a check to see if the equipement is disused - need to add field to process, equipment (and use mode in datatag)
            aliveTagId = subEquipment.getAliveTagId();
            if (aliveTagId != null) {
              if (controlTagCache.hasKey(subEquipment.getStateTagId())) {
                if (subEquipmentFacade.isRunning(subEquipment)) {
                  aliveTimerFacade.start(aliveTagId);
                }
              } else {
                LOGGER.warn("Unable to locate state tag in cache (id = " + subEquipment.getStateTagId() + ") cannot " +
                    "start alive timer for this subequipment.");
              }
            }
          } finally {
            subEquipmentCache.releaseReadLockOnKey(id);
          }
        }

        aliveTimerFacade.startAllTimers();
        clusterCache.put(C2monCacheLoader.aliveStatusInitialized, Boolean.TRUE);
      }

    } catch (Exception e) {
      LOGGER.error("initialise() : Error starting alive timer mechanism.", e);
    } finally {
      clusterCache.releaseWriteLockOnKey(C2monCacheLoader.aliveStatusInitialized);
    }

    //start timer on all servers
    //aliveTimerManager.start();

    //HeartbeatManager.getInstance().start(); //TODO start heartbeatmanager here
    LOGGER.info("Finished initializing all alive timers.");
  }

  @Override
  public void onProcessDisconnection(final ProcessDisconnectionRequest processDisconnectionRequest) {
    // Protect the method against accidental null parameters
    if (processDisconnectionRequest == null) {
      LOGGER.error("onProcessDisconnection(null) called - ignoring the request.");
      return;
    }

    // (1) Print some debug output

    if (LOGGER.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("onProcessDisconnection([");
      str.append(processDisconnectionRequest.getProcessName());
      str.append(", ");
      str.append(processDisconnectionRequest.getProcessPIK());
      str.append(", ");
      str.append(processDisconnectionRequest.getProcessStartupTime());
      str.append("]) called.");
      LOGGER.debug(str.toString());
    }
    //TODO remove inaccessible once fixed on client
    //int invalidationFlags = DataTagQuality.INACCESSIBLE + DataTagQuality.PROCESS_DOWN;

    try {

      Long processId;
      // (2) Check if the process exists (get methods throw exception otherwise) //check if id was sent - if not use name field
      if (processDisconnectionRequest.getProcessId() != ProcessDisconnectionRequest.NO_ID) {
        processId = processDisconnectionRequest.getProcessId();
      } else {
        processId = processCache.getProcessId(processDisconnectionRequest.getProcessName());
      }

      // (3) Get the corresponding state and alive tags
      try {
        Process processCopy = processCache.getCopy(processId);
        try {
          // If the process is stopped the PIK is null
          if (processCopy.getProcessPIK() == null) {
            LOGGER.warn("onProcessDisconnection - Received Process disconnection message for "
                + "a process PIK null. Id is " + processCopy.getId() + ". Message ignored.");
          }
          else {
            // Check if PIK is the same we disconnect otherwise we ignore the message
            if(processDisconnectionRequest.getProcessPIK().equals(processCopy.getProcessPIK())) {
              // (4) Only proceed if the process is actually running
              if (processFacade.isRunning(processId)) {

                String processStopMessage = "DAQ process " + processCopy.getName() + " was stopped.";
                LOGGER.trace("onProcessDisconnection - " + processStopMessage);

                // (5) LEGACY: TODO what does this comment mean?!
                Timestamp stopTime = new Timestamp(System.currentTimeMillis());
              this.processFacade.stop(processId, stopTime); //also stops alive timer

                // (7) Update process state tag
                stopStateTag(processCopy.getStateTagId(), stopTime, processStopMessage);
                stopEquipments(processCopy.getEquipmentIds(), stopTime, processStopMessage); //state tags to stopped
                // (8) Invalidate alive tag (no need to synchronize here as no "if then" update statement
                //dataTagFacade.setQuality(aliveTag, invalidationFlags, 0, processStopMessage, stopTime);
                // (9) Invalidate attached equipment (if any) -- keep lock on parent process (TODO config loader should hold lock on process while it runs?)
                //setControlTagsQuality(process, processStopMessage, stopTime,
                //                        true, true, true,                       //invalidate state, alive and commfault tags
                //                        invalidationFlags, 0);
              } else {
                LOGGER.warn("onProcessDisconnection - Received Process disconnection message for "
                    + "a process that is not running. Id is " + processCopy.getId() + ". Message ignored.");
              }
            }
            else {
              LOGGER.warn("onProcessDisconnection - Received Process disconnection message for "
                  + "a process with a diferent PIK (" + processDisconnectionRequest.getProcessPIK()
                  + " vs " + processCopy.getProcessPIK() + "). Id is " + processCopy.getId() + ". Message ignored.");
            }
          }
        } catch (CacheElementNotFoundException cacheEx) {
          LOGGER.error("State tag " + processCopy.getStateTagId() + " or the alive tag "
              + processCopy.getAliveTagId() + " for process " + processCopy.getId() + " could not be found in the "
              + "cache - disconnection actions could not be completed.", cacheEx);
        }

      } catch (CacheElementNotFoundException cacheEx) {
        LOGGER.error("Unable to locate process " + processId + " in cache.", cacheEx);
      }
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.error("Process object could not be retrieved from cache - disconnection actions may be incomplete.", cacheEx);
    } catch (IllegalArgumentException argEx) {
      LOGGER.error("IllegalArgument exception caught on processing DAQ disconnection - disconnection actions may be incomplete.", argEx);
    } catch (NullPointerException nullEx) {
      LOGGER.error("NullPointer exception caught on processing DAQ disconnection - disconnection actions may be incomplete.", nullEx);
    }
  }

  /**
   * Stops equipments and subequipments linked to this process (updates the cache objects & state tags).
   * Called on DAQ disconnection (not alive expiry).
   * @param equipmentIds List of equipment ids of a given Process
   * @param timestamp time of stop
   * @param message stop message
   */
  private void stopEquipments(final Collection<Long> equipmentIds, final Timestamp timestamp, final String message) {
    Equipment currentEquipmentCopy;
    for (Long equipmentId : equipmentIds) {
      try {
        try {
          currentEquipmentCopy = equipmentCache.getCopy(equipmentId);
          equipmentFacade.stop(equipmentId, timestamp);
          stopStateTag(currentEquipmentCopy.getStateTagId(), timestamp, message);
          for (Long subId : currentEquipmentCopy.getSubEquipmentIds()) {
            try {
              SubEquipment subEquipmentCopy = subEquipmentCache.getCopy(subId);
              stopStateTag(subEquipmentCopy.getStateTagId(), timestamp, message);
              subEquipmentFacade.stop(subId, timestamp);
            } catch (CacheElementNotFoundException ex) {
              LOGGER.error("Subequipment could not be retrieved from cache - unable to update Subequipment state.", ex);
            }
          }
        } catch (CacheElementNotFoundException e) {
          LOGGER.error("Equipment could not be retrieved from cache - unable to update Equipment state.", e);
        }
      } catch (Exception e) {
        LOGGER.error("Unable to acquire lock on Equipment/SubEquipment object", e);
      }
    }
  }

  /**
   * Stops the state tag (for Process disconnection).
   * @param stateTagId id of tag
   * @param pTimestamp time to use
   * @param message stop message
   */
  private void stopStateTag(final Long stateTagId, final Timestamp pTimestamp, final String message) {
    controlTagFacade.updateAndValidate(stateTagId, SupervisionStatus.DOWN.toString(), message, pTimestamp);
  }


  /**
   * Synchronized on the Process cache object. Catches all exceptions. There is no need to
   * send Process PIK to get the configuration file (Test mode can access it then)
   *
   * @param processConfigurationRequest the configuration request object
   * @return the configuration XML as a String or null if there was an exception
   */
  @Override
  public String onProcessConfiguration(final ProcessConfigurationRequest processConfigurationRequest) {
    ProcessConfigurationResponse processConfigurationResponse = new ProcessConfigurationResponse();

    // Protect the method against accidental null parameters
    if (processConfigurationRequest == null) {
      LOGGER.error("onProcessConfiguration(null) called - rejecting the request.");
      processConfigurationResponse.setConfigurationXML(ProcessConfigurationResponse.CONF_REJECTED);
      return this.xmlConverter.toXml(processConfigurationResponse);
    }

    // Process name (NO_PROCESS by default)
    processConfigurationResponse.setProcessName(processConfigurationRequest.getProcessName());

    // Print some debug information
    if (LOGGER.isDebugEnabled()) {
      StringBuilder str = new StringBuilder("onProcessConfiguration([");
      str.append(processConfigurationRequest.getProcessName());
      str.append(", ");
      str.append(processConfigurationRequest.getProcessPIK());
      str.append("]) called.");
      LOGGER.debug(str.toString());
    }

    // Retrieve the cache object for the process as well as its state tag
    try {
      Long processId = processCache.getProcessId(processConfigurationRequest.getProcessName());
      Process processCopy = processCache.getCopy(processId);
      LOGGER.info("onProcessConfiguration - Configuration request for DAQ " + processCopy.getName() + " authorized.");

      // Only If PIK is the same we have in cache we change Y(LOCAL_CONFIG) by N(SERVER_CONFIG) for the current process
      if(processConfigurationRequest.getProcessPIK().equals(processCopy.getProcessPIK())) {
        LOGGER.info("onProcessConfiguration - SERVER_CONFIG");
        this.processFacade.setLocalConfig(processId, LocalConfig.N);
      }

      // We get the configuration XML file (empty by default)
      processConfigurationResponse.setConfigurationXML(processXMLProvider.getProcessConfigXML(processCopy));
      LOGGER.info("onProcessConfiguration - Returning configuration XML to DAQ " + processCopy.getName());
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.warn("onProcessConfiguration - process not found in cache (name = "
          + processConfigurationRequest.getProcessName() + ") - unable to accept connection request.", cacheEx);
      processConfigurationResponse.setConfigurationXML(ProcessConfigurationResponse.CONF_REJECTED);
    } catch (Exception e) {
      LOGGER.error(new StringBuffer("onProcessConfiguration - An unexpected Exception occurred.").toString(), e);
      processConfigurationResponse.setConfigurationXML(ProcessConfigurationResponse.CONF_REJECTED);
    }

    return this.xmlConverter.toXml(processConfigurationResponse);
  }

  /**
   * Calls the ProcessDown, EquipmentDown or SubEquipmentDown methods depending
   * on the type of alive that has expired. The method synchronizes on the containing
   * Process cache object and catches all cache-related exceptions that can be
   * thrown by the private methods.
   * @param aliveTimerId the alive cache id
   */
  @Override
  public void onAliveTimerExpiration(final Long aliveTimerId) {
    // Protect the method against accidental null parameters
    if (aliveTimerId == null) {
      LOGGER.warn("onAliveTimerExpiration(null) called - ignoring the call.");
      return;
    }

    AliveTimer aliveTimer = aliveTimerCache.getCopy(aliveTimerId);

    // Build up a meaningful invalidation message -- USED TO DECIDE WHETHER TO REQUEST VALUES FROM DAQ WHEN UP AGAIN (SEE onProcessUp)
    StringBuffer msg = new StringBuffer("Alive of ");
    msg.append(aliveTimer.getAliveTypeDescription() + " ");
    msg.append(aliveTimer.getRelatedName());
    msg.append(" (alive tag: ");
    msg.append(aliveTimer.getId());
    msg.append(") has expired.");

    // Log the message
    LOGGER.debug(msg.toString());

    if (aliveTimer.getRelatedId() == null) {
      LOGGER.error("AliveTimer has not relatedId - unable to take any action on alive reception.");
    } else {
      try {
        final Long processId = processFacade.getProcessIdFromAlive(aliveTimer.getId());
        if (aliveTimer.isProcessAliveType()) {
          onProcessDown(processId, new Timestamp(System.currentTimeMillis()), msg.toString());
        } else {
          if (aliveTimer.isEquipmentAliveType()) {
            Long equipmentId = aliveTimer.getRelatedId();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            onEquipmentDown(equipmentId, timestamp, msg.toString());

            // Manually set the CommFaultTag (TIMS-972)
            ControlTag commFaultTag = controlTagCache.getCopy(equipmentCache.getCopy(equipmentId).getCommFaultTagId());
            setCommFaultTag(commFaultTag.getId(), false, commFaultTag.getValueDescription(), timestamp);

            // Bring down all SubEquipments
            for (Long subEquipmentId : equipmentCache.get(equipmentId).getSubEquipmentIds()) {
              String message = "Alive timer for parent Equipment expired: " + msg.toString();
              onSubEquipmentDown(subEquipmentId, timestamp, message);

              commFaultTag = controlTagCache.getCopy(subEquipmentCache.getCopy(subEquipmentId).getCommFaultTagId());
              setCommFaultTag(commFaultTag.getId(), false, commFaultTag.getValueDescription(), timestamp);
            }

          } else {
            Long subEquipmentId = aliveTimer.getRelatedId();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            onSubEquipmentDown(subEquipmentId, timestamp, msg.toString());

            // Manually set the CommFaultTag (TIMS-972)
            ControlTag commFaultTag = controlTagCache.getCopy(subEquipmentCache.getCopy(subEquipmentId).getCommFaultTagId());
            setCommFaultTag(commFaultTag.getId(), false, commFaultTag.getValueDescription(), timestamp);
          }
        }
      } catch (CacheElementNotFoundException cacheEx) {
        LOGGER.error("Unable to locate a required element within the cache on Alive Timer expiration.", cacheEx);
      } catch (NullPointerException nullEx) {
        LOGGER.error("NullPointer exception caught on Alive Timer expiration.", nullEx);
      } catch (IllegalArgumentException argEx) {
        LOGGER.error("IllegalArgument exception caught on Aliver Timer expiration", argEx);
      }
    }
  }

  /**
   * Manually set the value of a CommFaultTag. Used to update a CommFaultTag
   * when an AliveTimer expires, in order to keep the two tags consistent with
   * each other.
   *
   * @param commFaultTagId the ID of the tag to set
   * @param value the value of the tag
   * @param message the value description to set for the tag
   */
  private void setCommFaultTag(final Long commFaultTagId, final boolean value, final String message, final Timestamp timestamp) {
    try {
      controlTagFacade.updateAndValidate(commFaultTagId, value, message, timestamp);

    } catch (CacheElementNotFoundException e) {
      LOGGER.error("Could not locate CommFaultTag (id: " + commFaultTagId + ") in cache");
    }
  }

  /**
   * This method is called when the SupervisionManager detects that an Equipment
   * is down or not working.
   *
   * <p>Should be called with a synchronized block (on equipment).
   * @param equipment
   * @param pTimestamp
   * @param string
   */
  private void onEquipmentDown(final Long equipmentId, final Timestamp timestamp, final String message) {
    if (LOGGER.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("onEquipmentDown(");
      str.append("Equipment with id " + equipmentId);
      str.append(", ");
      str.append(timestamp);
      str.append(", ");
      str.append(message);
      LOGGER.debug(str.toString());
    }

    equipmentFacade.suspend(equipmentId, timestamp, message);

    final Equipment equipmentCopy = equipmentCache.getCopy(equipmentId);
    Long stateTagId = equipmentCopy.getStateTagId();
    if (stateTagId == null) {
      LOGGER.error("Could not find any state tag for equipment " + equipmentCopy.getId() + " - this should never happen.");
    } else {
      try {
        controlTagFacade.updateAndValidate(stateTagId, SupervisionStatus.DOWN.toString(), message, timestamp);

      } catch (CacheElementNotFoundException cacheEx) {
        LOGGER.error("Could not locate state tag (Id is " + stateTagId + ") in cache for equipment " + equipmentCopy.getId());
      }
    }
  }

  /**
   * Called whenever a subequipment is detected as being down (alive expiration or
   * reception of commfault tag).
   */
  private void onSubEquipmentDown(final Long subEquipmentId, final Timestamp timestamp, final String message) {
    SubEquipment subEquipmentCopy = subEquipmentCache.getCopy(subEquipmentId);
    if (LOGGER.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("onSubEquipmentDown(");
      str.append(subEquipmentCopy.getName());
      str.append(", ");
      str.append(timestamp);
      str.append(", ");
      str.append(message);
      LOGGER.debug(str.toString());
    }
    subEquipmentFacade.suspend(subEquipmentId, timestamp, message);
    Long stateTagId = subEquipmentCopy.getStateTagId();
    if (stateTagId == null) {
      LOGGER.error("Could not find any state tag for subequipment " + subEquipmentCopy.getId() + " - this should never happen.");
    } else {
      try {
        controlTagFacade.updateAndValidate(stateTagId, SupervisionStatus.DOWN.toString(), message, timestamp);

      } catch (CacheElementNotFoundException cacheEx) {
        LOGGER.error("Could not locate state tag (Id is " + stateTagId + ") in cache for subequipment " + subEquipmentCopy.getId());
      }
    }
  }

  /**
   * Called when an DAQ alive timer expires.
   *
   * <p>The onProcessDown() method sets the value of the state tag associated with
   * the process to "DOWN". If the value of the state tag is already "DOWN",
   * no further action is taken.
   *
   * <p>Call within block synchronized on Process.
   */
  private void onProcessDown(final Long processId, final Timestamp pTimestamp, final String pMessage) {
    if (LOGGER.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("onProcessDown(");
      str.append(processId);
      str.append(", ");
      str.append(pTimestamp);
      str.append(", ");
      str.append(pMessage);
      LOGGER.debug(str.toString());
    }

    processFacade.suspend(processId, pTimestamp, pMessage);
    final Process processCopy = processCache.getCopy(processId);

    //try to update the stateTag of the Process
    try {
      Long stateTagId = processCopy.getStateTagId();
      if (stateTagId == null) {
        LOGGER.error("State tag Id is set to null for Process + " + processCopy.getId() + " - unable to update it.");
      } else {
        controlTagFacade.updateAndValidate(stateTagId, SupervisionStatus.DOWN.toString(), pMessage, pTimestamp);
      }
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.error("Cannot locate the Process State tag in the cache - unable to update it.", cacheEx);
    }
  }

  @Override
  public void processControlTag(final SourceDataTagValue sourceDataTagValue) {
    // Theoretically it is possible that commfault events might be treated in the wrong order,
    // if sent very shortly after each other. In reality this is not really happening, except if there
    // is a huge backlog on the JMS queue. However, this is not a real problem as the incoming alive
    // always revalidates even if 2 commfault tags overtook each other (see TIMS-281). To keep the
    // code simple we accept this and do not take any further precautions.

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Incoming update for ControlTag " + sourceDataTagValue.getId() + " (value " + sourceDataTagValue.getValue() + ")");
    }
    Long tagId = sourceDataTagValue.getId();
    Object tagValue = sourceDataTagValue.getValue();
    String valueDescription = sourceDataTagValue.getValueDescription();
    if (valueDescription == null) {
      valueDescription = "";
    }


    /*
     * At this point we do not yet know whether the control tag we received is
     * (1) an alive tag, (2) a commfault tag or (3) a wrongly configured tag.
     * Therefore, we first try whether it is an alive tag by calling the
     * isRegisteredAliveTimer() method of the AliveTimerManager.
     */

    //first lock the process concerned, so no reconfiguration occurs during the processing?? no - slows the all data acquisition down
    //instead: reconfiguration must lock all tags it modifies (must adapt to reconfiguring DAQ while receiving updates for tags, as
    // it is impossible to lock the process every time we receive a tag update for that process...)

    //catch all cache-related exceptions

    //notice uses once timestamp for
    try {

      if (aliveTimerFacade.isRegisteredAliveTimer(tagId)) {

        //reject old alive tag
        AliveTimer timerCopy = aliveTimerCache.getCopy(tagId);
        //TODO tmp check until all DAQ updates have DAQ t.s. set
        Timestamp useTimestamp;
        if (sourceDataTagValue.getDaqTimestamp() == null) {
          useTimestamp = sourceDataTagValue.getTimestamp();
        } else {
          useTimestamp = sourceDataTagValue.getDaqTimestamp();
        }
        Timestamp aliveTimerTimestamp = new Timestamp(System.currentTimeMillis());
        if (aliveTimerTimestamp.getTime() - useTimestamp.getTime()
                                       > 2 * timerCopy.getAliveInterval()) {
          LOGGER.warn("Rejecting alive " + tagId + " as delayed arrival at server.");
        } else {
       // The tag is an alive tag -> we rewind the corresponding alive timer
          //TODO sychronization on alive timers... needed? use id here, so not possible around update
          aliveTimerFacade.update(tagId);

          Timestamp supervisionTimestamp = new Timestamp(System.currentTimeMillis());
          if (timerCopy.isProcessAliveType()) {
            Long processId = processFacade.getProcessIdFromAlive(tagId);
            onProcessUp(processId, supervisionTimestamp, "Process Alive tag received.");
          } else {
            if (timerCopy.isEquipmentAliveType()) {
              onEquipmentUp(timerCopy.getRelatedId(), supervisionTimestamp, "Equipment Alive tag received.");
            } else {
              // It is a subequipment
              onSubEquipmentUp(timerCopy.getRelatedId(), supervisionTimestamp, "Subequipment Alive tag received.");
            }
          }
        }
      } else {
        // The tag is NOT an alive tag -> we check if it is a communication fault tag

        CommFaultTag commFaultTagCopy = commFaultTagCache.getCopy(tagId);
        if (LOGGER.isDebugEnabled()) {
          StringBuilder str = new StringBuilder("processControlTag() : tag ");
          str.append(tagId);
          str.append(" is a commfault tag.");
          LOGGER.debug(str.toString());
        }

        if (equipmentCache.hasKey(commFaultTagCopy.getEquipmentId())) { //check if equipment

          boolean updateAliveTimer = false; //must be done outside of the process lock as locks the alivetimer!
          Long equipmentId = commFaultTagCopy.getEquipmentId();
          Timestamp supervisionTimestamp = new Timestamp(System.currentTimeMillis());
          if (tagValue.equals(commFaultTagCopy.getFaultValue())) {
            StringBuffer str = new StringBuffer("Communication fault tag indicates that equipment ");
            str.append(commFaultTagCopy.getEquipmentName());
            str.append(" is down.");
            if (!valueDescription.equalsIgnoreCase("")) {
              str.append(" Reason: " + valueDescription);
            }
            onEquipmentDown(equipmentId, supervisionTimestamp, str.toString());
          } else {
            updateAliveTimer = true;
            StringBuffer str = new StringBuffer("Communication fault tag indicates that equipment ");
            str.append(commFaultTagCopy.getEquipmentName());
            str.append(" is up.");
            onEquipmentUp(equipmentId, supervisionTimestamp, str.toString());
          }
          if (updateAliveTimer) {
            if (commFaultTagCopy.getAliveTagId() != null) {
              aliveTimerFacade.update(commFaultTagCopy.getAliveTagId());
            }
          }

        } else if (subEquipmentCache.hasKey(commFaultTagCopy.getEquipmentId())) {     //check if subequipment

          boolean updateAliveTimer = false;
          Long subEquipmentId = commFaultTagCopy.getEquipmentId();
          Timestamp supervisionTimestamp = new Timestamp(System.currentTimeMillis());
          if (tagValue.equals(commFaultTagCopy.getFaultValue())) {
            StringBuffer str = new StringBuffer("Communication fault tag indicates that subequipment ");
            str.append(commFaultTagCopy.getEquipmentName());
            str.append(" is down.");

            if (!valueDescription.equalsIgnoreCase("")) {
              str.append(" Reason: " + valueDescription);
            }
            onSubEquipmentDown(subEquipmentId, supervisionTimestamp, str.toString());
          } else {
            updateAliveTimer = true;
            StringBuffer str = new StringBuffer("Communication fault tag indicates that subequipment ");
            str.append(commFaultTagCopy.getEquipmentName());
            str.append(" is up.");
            onSubEquipmentUp(commFaultTagCopy.getEquipmentId(), supervisionTimestamp, str.toString());
          }
          if (updateAliveTimer) {
            if (commFaultTagCopy.getAliveTagId() != null) {
              aliveTimerFacade.update(commFaultTagCopy.getAliveTagId());
            }
          }
        } else {
          LOGGER.error("Unable to locate equipment/subequipment in cache (id = " + commFaultTagCopy.getEquipmentId() + ") - key could not be located.");
        }
      }
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.error("Unable to locate a required element within the cache while processing control tag " + tagId + ".", cacheEx);
    } catch (Exception ex) {
      LOGGER.error("Unexpected exception caught on Alive Timer expiration for tag " + tagId + ".", ex);
    }
  }





  /**
   * TODO write...
   * The onUp() method is called in one of the following cases:
   * <UL>
   *   <LI>an alive tag attached to a process/equipment has been received
   *   <LI>a communication fault indicating an equipment is running has been received.
   * </UL>
   * The onUp() method sets the value of the state tag associated with the
   * process/equipment to "RUNNING". If the value of the state tag is already
   * "RUNNING", no further action is taken.
   *
   * FURTHER DETAILS:
   *
   * If the state tag is valid and not "RUNNING", it is updated to "RUNNING" (as is the associated
   * process field). In addition, if the state is DOWN due to an alive expiration,
   * then a request is made to the DAQ to send the latest values.
   *
   * Notice that the process alive is of course revalidated on reception of a new alive.
   */
  private void onProcessUp(final Long processId, final Timestamp pTimestamp, final String pMessage) {
    if (LOGGER.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("onProcessUp(");
      str.append("Process with Id " + processId);
      str.append(", ");
      str.append(pTimestamp);
      str.append(") called.");
      LOGGER.debug(str.toString());
    }
    processFacade.resume(processId, pTimestamp, pMessage);

    final Process process = processCache.getCopy(processId);
    //check state tag is correctly set
    Long stateTagId = process.getStateTagId(); //never null
    controlTagCache.acquireWriteLockOnKey(stateTagId);
    try {
      ControlTag stateTag = controlTagCache.get(stateTagId);

      if (stateTag.getValue() == null || !stateTag.getValue().equals(SupervisionStatus.RUNNING.toString()) || !stateTag.isValid()) {

        // If the process is running under a local configuration, set the status
        // tag to RUNNING_LOCAL
        if (process.getLocalConfig() != null && process.getLocalConfig().equals(LocalConfig.Y)) {
          LOGGER.debug("onProcessUp(): Process is running on a local configuration, setting status to RUNNING_LOCAL");
          controlTagFacade.updateAndValidate(stateTagId, SupervisionStatus.RUNNING_LOCAL.toString(), pMessage, pTimestamp);

        } else if (stateTag.getValue() == null || !stateTag.getValue().equals(SupervisionStatus.RUNNING.toString())) {
          controlTagFacade.updateAndValidate(stateTagId, SupervisionStatus.RUNNING.toString(), pMessage, pTimestamp);
        }
      }
    } catch (CacheElementNotFoundException  controlCacheEx) {
      LOGGER.error("Unable to locate state tag in cache (id is " + stateTagId + ")", controlCacheEx);
    } finally {
      controlTagCache.releaseWriteLockOnKey(stateTagId);
    }
  }

  /**
   * TODO add details explaining synchronization
   * <UL>
   * <LI>either on reception of the equipment's alive tag
   * <LI>or on reception of the equipment's commfault tag (good value).
   * </UL>
   * The state tag of the equipment is updated (value RUNNING).
   *
   * <p>Must be called within a block synchronized on the process object.
   *
   * @param pId id of the equipment concerned
   * @param pTimestamp time when the equipment was detected to be "up"
   * @param pMessage custom message with more information of why the equipment is believed to be up.
   */
  private void onEquipmentUp(final Long pId, final Timestamp pTimestamp, final String pMessage) {
    if (LOGGER.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("onEquipmentUp(");
      str.append(pId);
      str.append(", ");
      str.append(pTimestamp);
      str.append(", ");
      str.append(pMessage);
      LOGGER.debug(str.toString());
    }
    // Try to obtain a copy of the state tag with its current value
    try {
      equipmentFacade.resume(pId, pTimestamp, pMessage);
      Equipment equipmentCopy = equipmentCache.getCopy(pId);
      //set state tag if necessary
      Long stateTagId = equipmentCopy.getStateTagId();
      Long commFaultId = equipmentCopy.getCommFaultTagId();
      controlTagCache.acquireWriteLockOnKey(stateTagId);
      try {
        ControlTag stateTag = controlTagCache.get(stateTagId);
        if (stateTag.getValue() == null || !stateTag.getValue().equals(SupervisionStatus.RUNNING.toString()) || !stateTag.isValid()) {
          controlTagFacade.updateAndValidate(stateTagId, SupervisionStatus.RUNNING.toString(), pMessage, pTimestamp);
        }
      } catch (CacheElementNotFoundException controlCacheEx) {
        LOGGER.error("Unable to locate equipment state tag in control tag cache (id is " + stateTagId + ")", controlCacheEx);
      } finally {
        controlTagCache.releaseWriteLockOnKey(stateTagId);
      }

      ControlTag commFaultTag = controlTagCache.getCopy(commFaultId);
      setCommFaultTag(commFaultId, true, commFaultTag.getValueDescription(), pTimestamp);
    } catch (CacheElementNotFoundException equipmentCacheEx) {
      LOGGER.error("Unable to locate equipment in cache (id is " + pId + ") - not taking any invalidation action.", equipmentCacheEx);
    }
  }

  /**
   * This method is called when the subequipment's alivetag or the
   * subequipment's commfault tag (good value) is received. In both cases we
   * assume the equipment is running and we modify its state tag accordingly.
   *
   * @param pId
   *          Identifer of the subequipment for which the alivetag/commfaulttag
   *          was received
   * @param pTimestamp
   *          Timestamp indicating when it was received
   * @param pStateTagId
   *          The id of the state tag that indicates the subequipment state
   * @param pMessage
   *          Message explaining which is the cause for the subequipment to be
   *          considered as being up.
   */

  private void onSubEquipmentUp(final Long pId, final Timestamp pTimestamp, final String pMessage) {

    if (LOGGER.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("onSubEquipmentUp(");
      str.append(pId);
      str.append(", ");
      str.append(pTimestamp);
      str.append(", ");
      str.append(pMessage);
      LOGGER.debug(str.toString());
    }

    try {
      // Try to obtain a copy of the state tag with its current value
      subEquipmentFacade.resume(pId, pTimestamp, pMessage);
      SubEquipment subEquipmentCopy = subEquipmentCache.getCopy(pId);
      Long stateTagId = subEquipmentCopy.getStateTagId();
      Long commFaultId = subEquipmentCopy.getCommFaultTagId();
      controlTagCache.acquireWriteLockOnKey(stateTagId);
      try {
        ControlTag stateTag = controlTagCache.get(stateTagId);
        if (stateTag.getValue() == null  || !stateTag.getValue().equals(SupervisionStatus.RUNNING.toString()) || !stateTag.isValid()) {
          controlTagFacade.updateAndValidate(stateTagId, SupervisionStatus.RUNNING.toString(), pMessage, pTimestamp);
        }
      } catch (CacheElementNotFoundException controlCacheEx) {
        LOGGER.error("Unable to locate subequipment state tag in control tag cache (id is " + stateTagId + ")", controlCacheEx);
      } finally {
        controlTagCache.releaseWriteLockOnKey(stateTagId);
      }

      ControlTag commFaultTag = controlTagCache.getCopy(commFaultId);
      setCommFaultTag(commFaultId, true, commFaultTag.getValueDescription(), pTimestamp);
    } catch (CacheElementNotFoundException subEquipmentCacheEx) {
      LOGGER.error("Unable to locate subequipment in cache (id is " + pId + ")", subEquipmentCacheEx);
    }
  }

  @Override
  public boolean isAutoStartup() {
    return false;
  }

  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }

  /**
   * Always return false; nothing needs doing at shutd
   */
  @Override
  public boolean isRunning() {
    return false;
  }

  @Override
  public void stop() {
    //do nothing; alive timers must stay activated in clustered environment
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST;
  }

  /**
   * Synchronized on the Process cache object. Catches all exceptions.
   *
   * @param processConnectionRequest the PIK request object
   * @return the PIK XML as a String or null if there was an exception
   */
  @Override
  public String onProcessConnection(final ProcessConnectionRequest processConnectionRequest) {
    ProcessConnectionResponse processConnectionResponse = new ProcessConnectionResponse();

    // Protect the method against accidental null parameters
    if (processConnectionRequest == null) {
      LOGGER.error("onProcessConfiguration(null) called - rejecting the request.");
      processConnectionResponse.setprocessPIK(ProcessConnectionResponse.PIK_REJECTED);
      return this.xmlConverter.toXml(processConnectionResponse);
    }

    // Process name (NO_PROCESS by default)
    processConnectionResponse.setProcessName(processConnectionRequest.getProcessName());

    // Print some debug information
    if (LOGGER.isDebugEnabled()) {
      StringBuilder str = new StringBuilder("onProcessConnection([");
      str.append(processConnectionRequest.getProcessName());
      str.append(", ");
      str.append(processConnectionRequest.getProcessHostName());
      str.append(", ");
      str.append(processConnectionRequest.getProcessStartupTime());
      str.append("]) called.");
      LOGGER.debug(str.toString());
    }

    // Retrieve the cache object for the process as well
    try {
      Long processId = processCache.getProcessId(processConnectionRequest.getProcessName());
      processCache.acquireWriteLockOnKey(processId);
      try {
        Process process = processCache.get(processId);
        try {
          // If process is already currently running
          if (this.processFacade.isRunning(processId)) {
            // And TEST mode is on
            if (properties.isTestMode()) {
                LOGGER.info("onProcessConnection - TEST mode - Connection request for DAQ " + process.getName() + " authorized.");

                // Start Up the process
                this.controlTagFacade.updateAndValidate(process.getStateTagId(), SupervisionStatus.STARTUP.toString(), "ProcessConnection message received.",
                    processConnectionRequest.getProcessStartupTime());
                process = this.processFacade.start(processId, processConnectionRequest.getProcessHostName(), processConnectionRequest.getProcessStartupTime());

                // PIK
                processConnectionResponse.setprocessPIK(process.getProcessPIK());

                LOGGER.info("onProcessConnection - TEST Mode - Returning PIKResponse to DAQ " + process.getName()
                    + ", PIK " + process.getProcessPIK());

            // If process is already currently running and TEST mode is off no connection is permitted
            } else {
              // Reject Connection
              processConnectionResponse.setprocessPIK(ProcessConnectionResponse.PIK_REJECTED);
              LOGGER.warn("onProcessConnection - The DAQ process is already running, returning rejected connection : "
                  + processConnectionRequest.getProcessName());
            }
          // If process is not currently running the connection is permitted
          } else {
            LOGGER.info("onProcessConnection - Connection request for DAQ " + process.getName() + " authorized.");

            // Start Up the process
            this.controlTagFacade.updateAndValidate(process.getStateTagId(), SupervisionStatus.STARTUP.toString(), "ProcessConnection message received.",
                processConnectionRequest.getProcessStartupTime());
            process = this.processFacade.start(processId, processConnectionRequest.getProcessHostName(), processConnectionRequest.getProcessStartupTime());

            // PIK
            processConnectionResponse.setprocessPIK(process.getProcessPIK());

            LOGGER.info("onProcessConnection - Returning PIKResponse to DAQ " + process.getName());
          }

        } catch (CacheElementNotFoundException cacheEx) {
          LOGGER.error("State tag " + process.getStateTagId() + " or the alive tag for process " + process.getId() +
              "could not be found in the cache.");
        }
      } finally {
        processCache.releaseWriteLockOnKey(processId);
      }
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.warn("onProcessConnection - process not found in cache (name = "
          + processConnectionRequest.getProcessName() + ") - unable to accept connection request.", cacheEx);
      processConnectionResponse.setprocessPIK(ProcessConnectionResponse.PIK_REJECTED);
    } catch (Exception e) {
      LOGGER.error(new StringBuffer("onProcessConnection - An unexpected Exception occurred.").toString(), e);
      processConnectionResponse.setprocessPIK(ProcessConnectionResponse.PIK_REJECTED);
    }

    return this.xmlConverter.toXml(processConnectionResponse);
  }
}
