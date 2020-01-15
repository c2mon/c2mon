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

import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.config.ServerProperties;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.supervision.SupervisionManager;
import cern.c2mon.server.supervision.impl.event.AliveTagEvents;
import cern.c2mon.server.supervision.impl.event.EquipmentEvents;
import cern.c2mon.server.supervision.impl.event.ProcessEvents;
import cern.c2mon.server.supervision.impl.event.SubEquipmentEvents;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import cern.c2mon.shared.daq.process.ProcessConfigurationRequest;
import cern.c2mon.shared.daq.process.ProcessConnectionRequest;
import cern.c2mon.shared.daq.process.ProcessDisconnectionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.sql.Timestamp;


/**
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
 */
@Service("supervisionManager")
@Slf4j
public class SupervisionManagerImpl implements SupervisionManager, SmartLifecycle {

  @Inject
  private ProcessEvents processEvents;

  @Inject
  private EquipmentEvents equipmentEvents;

  @Inject
  private SubEquipmentEvents subEquipmentEvents;

  @Inject
  private AliveTagEvents aliveTimerEvents;

//  ===========================

  @Resource
  private C2monCache<Equipment> equipmentCache;

  @Resource
  private C2monCache<SubEquipment> subEquipmentCache;

  @Resource
  private C2monCache<CommFaultTag> commFaultTagCache;

  @Resource
  private AliveTagService aliveTimerService;

  @Resource
  private C2monCache<AliveTag> aliveTimerCache;

  @Resource
  private ProcessService processFacade;

  @Resource
  private ServerProperties properties;;

  /**
   * Starts the alive timer mechanisms at server start up.
   */
  @Override
  public void start() {
    if (log.isDebugEnabled()) {
      log.debug("starting all alive timers");
    }

//    TODO ================================================================================
//    TODO = (Alex) We could achieve this delayed startup by emitting a CACHE_READY event =
//    TODO ================================================================================

    //the purpose of this synch block is to wait for all caches to be loaded (from DB only, not "refreshing to node" which can be simultaneous!)
    //before starting the alive mechanism: the alive mechanism needs several of
    //these caches and we don't want any server instance up and running before
    //the entire cache has been loaded (another server might still be loading
    //the datatag cache when this instance reaches this point; all cache loads
    //hold a distributed read lock on this lock)
    //Once the lock is write-available, only one server wills start the alive
    //timers, although they all start the timer mechanism on their JVM).
//    clusterCache.acquireWriteLockOnKey(C2monCacheLoader.aliveStatusInitialized);
//    try {
//      if (!clusterCache.hasKey(C2monCacheLoader.aliveStatusInitialized) || !(Boolean) clusterCache.getCopy(C2monCacheLoader.aliveStatusInitialized)) {
//        Long aliveTagId;
//
//        //TODO the three sections below unnecessary?
//        Process process;
//        for (Long id : processCache.getKeys()) {
//          processCache.acquireReadLockOnKey(id);
//          try {
//            process = processCache.get(id); //TODO should never be fake, unless config loader removes processes during runtime
//            //TODO add here a check to see if the process is disused - need to add field to process,
//            //     equipment (and use mode in datatag)
//            //     comment: may not need this after all, just remove from cache when disused
//            aliveTagId = process.getAliveTagId();
//            if (aliveTagId != null) {
//              if (controlTagCache.hasKey(process.getStateTagId())) {
//                if (processFacade.isRunning(process)) {
//                  aliveTimerFacade.start(aliveTagId);
//                }
//              } else {
//                log.warn("Unable to locate state tag in cache (id = " + process.getStateTagId() + ") " +
//                  "cannot start alive timer for this process.");
//              }
//            }
//          } finally {
//            processCache.releaseReadLockOnKey(id);
//          }
//        }
//
//        //start equipment alive timers
//        Equipment equipment;
//        for (Long id : equipmentCache.getKeys()) {
//          equipmentCache.acquireReadLockOnKey(id);
//          try {
//            equipment = equipmentCache.get(id); //TODO should never be fake, unless config loader removes processes during runtime
//            //TODO add here a check to see if the equipement is disused - need to add field to process, equipment (and use mode in datatag)
//            aliveTagId = equipment.getAliveTagId();
//            if (aliveTagId != null) {
//              if (controlTagCache.hasKey(equipment.getStateTagId())) {
//                if (equipmentFacade.isRunning(equipment)) {
//                  aliveTimerFacade.start(aliveTagId);
//                }
//              } else {
//                log.warn("Unable to locate state tag in cache (id = " + equipment.getStateTagId() + ") " +
//                  "cannot start alive timer for this equipment.");
//              }
//            }
//          } finally {
//            equipmentCache.releaseReadLockOnKey(id);
//          }
//        }
//
//        //start subequipment alive timers
//        SubEquipment subEquipment;
//        for (Long id : subEquipmentCache.getKeys()) {
//          subEquipmentCache.acquireReadLockOnKey(id);
//          try {
//            subEquipment = subEquipmentCache.get(id); //TODO should never be fake, unless config loader removes processes during runtime
//
//            // TODO add here a check to see if the equipement is disused - need to add field to process, equipment (and use mode in datatag)
//            aliveTagId = subEquipment.getAliveTagId();
//            if (aliveTagId != null) {
//              if (controlTagCache.hasKey(subEquipment.getStateTagId())) {
//                if (subEquipmentFacade.isRunning(subEquipment)) {
//                  aliveTimerFacade.start(aliveTagId);
//                }
//              } else {
//                log.warn("Unable to locate state tag in cache (id = " + subEquipment.getStateTagId() + ") cannot " +
//                  "start alive timer for this subequipment.");
//              }
//            }
//          } finally {
//            subEquipmentCache.releaseReadLockOnKey(id);
//          }
//        }
//
//        aliveTimerFacade.startAllTimers();
//        clusterCache.put(C2monCacheLoader.aliveStatusInitialized, Boolean.TRUE);
//      }
//
//    } catch (Exception e) {
//      log.error("initialise() : Error starting alive timer mechanism.", e);
//    } finally {
//      clusterCache.releaseWriteLockOnKey(C2monCacheLoader.aliveStatusInitialized);
//    }
//
//    //start timer on all servers
//    //aliveTimerManager.start();
//
//    //HeartbeatManager.getInstance().start(); //TODO start heartbeatmanager here
//    log.info("Finished initializing all alive timers.");
  }

  /**
   * Synchronized on the Process cache object. Catches all exceptions.
   *
   * @param processConnectionRequest the PIK request object
   * @return the PIK XML as a String or null if there was an exception
   */
  @Override
  public String onProcessConnection(final ProcessConnectionRequest processConnectionRequest) {
    return processEvents.onConnection(processConnectionRequest);
  }

  @Override
  public void onProcessDisconnection(final ProcessDisconnectionRequest processDisconnectionRequest) {
    processEvents.onDisconnection(processDisconnectionRequest);
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
    return processEvents.onConfiguration(processConfigurationRequest);
  }

  /**
   * Calls the ProcessDown, EquipmentDown or SubEquipmentDown methods depending
   * on the type of alive that has expired.
   */
  @Override
  public void onAliveTimerExpiration(final Long aliveTimerId) {
    aliveTimerEvents.onAliveTimerExpiration(aliveTimerId);
  }

  @Override
  public void processControlTag(final SourceDataTagValue sourceDataTagValue) {
    // Theoretically it is possible that commfault events might be treated in the wrong order,
    // if sent very shortly after each other. In reality this is not really happening, except if there
    // is a huge backlog on the JMS queue. However, this is not a real problem as the incoming alive
    // always revalidates even if 2 commfault tags overtook each other (see TIMS-281). To keep the
    // code simple we accept this and do not take any further precautions.

    if (log.isDebugEnabled()) {
      log.debug("Incoming update for ControlTag " + sourceDataTagValue.getId() + " (value " + sourceDataTagValue.getValue() + ")");
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

      if (aliveTimerService.isRegisteredAliveTimer(tagId)) {

        //reject old alive tag
        AliveTag timerCopy = aliveTimerCache.get(tagId);
        //TODO tmp check until all DAQ updates have DAQ t.s. set
        Timestamp useTimestamp;
        if (sourceDataTagValue.getDaqTimestamp() == null) {
          useTimestamp = sourceDataTagValue.getTimestamp();
        } else {
          useTimestamp = sourceDataTagValue.getDaqTimestamp();
        }
        Timestamp aliveTimerTimestamp = new Timestamp(System.currentTimeMillis());
        if (aliveTimerTimestamp.getTime() - useTimestamp.getTime() > 2 * timerCopy.getAliveInterval()) {
          log.debug("Rejecting alive #{} of {} as delayed arrival at server.", tagId, timerCopy.getSupervisedName());
        } else {
          // The tag is an alive tag -> we rewind the corresponding alive timer
          //TODO sychronization on alive timers... needed? use id here, so not possible around update
          aliveTimerService.startOrUpdateTimestamp(tagId, useTimestamp.getTime());

          Timestamp supervisionTimestamp = new Timestamp(System.currentTimeMillis());
          // TODO (Alex) Is this the timer we want to use?
          if (timerCopy.getSupervisedEntity() == SupervisionEntity.PROCESS) {
            Long processId = processFacade.getProcessIdFromAlive(tagId);
            processEvents.onUp(processId, supervisionTimestamp, "Process Alive tag received.");
          } else {
            if (timerCopy.getSupervisedEntity() == SupervisionEntity.EQUIPMENT) {
              equipmentEvents.onUp(timerCopy.getSupervisedId(), supervisionTimestamp, "Equipment Alive tag received.");
            } else {
              // It is a subequipment
              subEquipmentEvents.onUp(timerCopy.getSupervisedId(), supervisionTimestamp, "Subequipment Alive tag received.");
            }
          }
        }
      } else {
        // The tag is NOT an alive tag -> we check if it is a communication fault tag

        CommFaultTag commFaultTagCopy = commFaultTagCache.get(tagId);
        if (log.isDebugEnabled()) {
          StringBuilder str = new StringBuilder("processControlTag() : tag ");
          str.append(tagId);
          str.append(" is a commfault tag.");
          log.debug(str.toString());
        }

        if (equipmentCache.containsKey(commFaultTagCopy.getSupervisedId())) { //check if equipment

          boolean updateAliveTimer = false; //must be done outside of the process lock as locks the alivetimer!
          Long equipmentId = commFaultTagCopy.getSupervisedId();
          Timestamp supervisionTimestamp = new Timestamp(System.currentTimeMillis());
          if (tagValue.equals(commFaultTagCopy.getFaultValue())) {
            StringBuffer str = new StringBuffer("Communication fault tag indicates that equipment ");
            str.append(commFaultTagCopy.getEquipmentName());
            str.append(" is down.");
            if (!valueDescription.equalsIgnoreCase("")) {
              str.append(" Reason: ").append(valueDescription);
            }
            equipmentEvents.onDown(equipmentId, supervisionTimestamp, str.toString());
          } else {
            updateAliveTimer = true;
            String str = "Communication fault tag indicates that equipment "
              + commFaultTagCopy.getEquipmentName() + " is up.";
            equipmentEvents.onUp(equipmentId, supervisionTimestamp, str);
          }
          if (updateAliveTimer) {
            Timestamp useTimestamp;
            if (sourceDataTagValue.getDaqTimestamp() == null) {
              useTimestamp = sourceDataTagValue.getTimestamp();
            } else {
              useTimestamp = sourceDataTagValue.getDaqTimestamp();
            }
            if (commFaultTagCopy.getAliveTagId() != null) {
              aliveTimerService.startOrUpdateTimestamp(commFaultTagCopy.getAliveTagId(), useTimestamp.getTime());
            }
          }

        } else if (subEquipmentCache.containsKey(commFaultTagCopy.getSupervisedId())) {     //check if subequipment

          boolean updateAliveTimer = false;
          Long subEquipmentId = commFaultTagCopy.getSupervisedId();
          Timestamp supervisionTimestamp = new Timestamp(System.currentTimeMillis());
          if (tagValue.equals(commFaultTagCopy.getFaultValue())) {
            StringBuffer str = new StringBuffer("Communication fault tag indicates that subequipment ");
            str.append(commFaultTagCopy.getEquipmentName());
            str.append(" is down.");

            if (!valueDescription.equalsIgnoreCase("")) {
              str.append(" Reason: ").append(valueDescription);
            }
            subEquipmentEvents.onDown(subEquipmentId, supervisionTimestamp, str.toString());
          } else {
            updateAliveTimer = true;
            StringBuffer str = new StringBuffer("Communication fault tag indicates that subequipment ");
            str.append(commFaultTagCopy.getEquipmentName());
            str.append(" is up.");
            subEquipmentEvents.onUp(commFaultTagCopy.getSupervisedId(), supervisionTimestamp, str.toString());
          }
          if (updateAliveTimer) {
            Timestamp useTimestamp;
            if (sourceDataTagValue.getDaqTimestamp() == null) {
              useTimestamp = sourceDataTagValue.getTimestamp();
            } else {
              useTimestamp = sourceDataTagValue.getDaqTimestamp();
            }
            if (commFaultTagCopy.getAliveTagId() != null) {
              aliveTimerService.startOrUpdateTimestamp(commFaultTagCopy.getAliveTagId(), useTimestamp.getTime());
            }
          }
        } else {
          log.error("Unable to locate equipment/subequipment in cache (id = " + commFaultTagCopy.getSupervisedId() + ") - key could not be located.");
        }
      }
    } catch (CacheElementNotFoundException cacheEx) {
      log.error("Unable to locate a required element within the cache while processing control tag " + tagId + ".", cacheEx);
    } catch (Exception ex) {
      log.error("Unexpected exception caught on Alive Timer expiration for tag " + tagId + ".", ex);
    }
  }

  @Override
  public boolean isAutoStartup() {
    return true;
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

}
