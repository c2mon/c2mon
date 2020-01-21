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
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.supervision.SupervisionManager;
import cern.c2mon.server.supervision.impl.event.*;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.sql.Timestamp;

import static cern.c2mon.server.common.util.KotlinAPIs.orElse;
import static cern.c2mon.server.supervision.log.SupervisionLogMessages.eqCommFault;
import static cern.c2mon.shared.common.supervision.SupervisionEntity.EQUIPMENT;
import static cern.c2mon.shared.common.supervision.SupervisionEntity.SUBEQUIPMENT;


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
  private ServerProperties properties;

  // https://stackoverflow.com/questions/4195027/when-will-the-java-date-collapse
  private static final Timestamp ALMOST_MAX_TIMESTAMP = new Timestamp(Long.MAX_VALUE - 100000);

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
   * Calls the ProcessDown, EquipmentDown or SubEquipmentDown methods depending
   * on the type of alive that has expired.
   */
  @Override
  public void onAliveTimerExpiration(final Long aliveTimerId) {
    aliveTimerEvents.onAliveTimerExpiration(aliveTimerId);
  }

  @Override
  public void processControlTag(final SourceDataTagValue sourceDataTagValue) {
    log.debug("Incoming update for ControlTag {} (value {})",sourceDataTagValue.getId(),sourceDataTagValue.getValue());
    Long tagId = sourceDataTagValue.getId();

    try {
      if (aliveTimerService.isRegisteredAliveTimer(tagId)) {
        // The tag is an alive tag -> we update the corresponding alive timer
        handleAliveTimer(sourceDataTagValue, tagId);
      } else {
        // The tag is NOT an alive tag -> is it a commFault?
        handleCommFault(sourceDataTagValue, tagId);
      }
    } catch (CacheElementNotFoundException cacheEx) {
      log.error("Unable to locate a required element within the cache while processing control tag " + tagId + ".", cacheEx);
    } catch (Exception ex) {
      log.error("Unexpected exception caught on Alive Timer expiration for tag " + tagId + ".", ex);
    }
  }

  private void handleCommFault(SourceDataTagValue sourceDataTagValue, Long tagId) {
    CommFaultTag commFault = commFaultTagCache.get(tagId);
    log.debug("processControlTag() : tag {} is a commfault tag.", commFault.getName());

    // TODO (Alex) Can we also check SupervisedEntity?
    if (equipmentCache.containsKey(commFault.getSupervisedId())) {
      handleCommFault(sourceDataTagValue, commFault, EQUIPMENT);
    } else if (subEquipmentCache.containsKey(commFault.getSupervisedId())) {
      handleCommFault(sourceDataTagValue, commFault, SUBEQUIPMENT);
    } else {
      log.error("Unable to locate equipment/subequipment in cache (id = {}) - key could not be located.", commFault.getSupervisedId());
    }
  }

  private <T extends AbstractEquipment> void handleCommFault(SourceDataTagValue sourceDataTagValue, CommFaultTag commFault, SupervisionEntity type) {
    Long equipmentId = commFault.getSupervisedId();
    long supervisionTimestamp = chooseEarliestNonNullTime(sourceDataTagValue);
    SupervisionEventHandler eventHandler = (type == EQUIPMENT ? equipmentEvents : subEquipmentEvents);

    if (sourceDataTagValue.getValue().equals(commFault.getFaultValue())) {
      eventHandler.onDown(equipmentId, supervisionTimestamp, eqCommFault(sourceDataTagValue, type, commFault, false));
    } else {
      eventHandler.onUp(equipmentId, supervisionTimestamp, eqCommFault(sourceDataTagValue, type, commFault, true));
    }
  }


  private long chooseEarliestNonNullTime(SourceDataTagValue sourceDataTagValue) {
    return Math.min(
      orElse(sourceDataTagValue.getDaqTimestamp(), ALMOST_MAX_TIMESTAMP).getTime(),
      orElse(sourceDataTagValue.getTimestamp(), ALMOST_MAX_TIMESTAMP).getTime()
    );
  }

  private void handleAliveTimer(SourceDataTagValue sourceDataTagValue, Long tagId) {
    AliveTag timerCopy = aliveTimerCache.get(tagId);
    long useTimestamp = chooseEarliestNonNullTime(sourceDataTagValue);

    // Reject expired alive tags
    if (System.currentTimeMillis() - useTimestamp > 2 * timerCopy.getAliveInterval()) {
      log.debug("Rejecting alive #{} of {} as delayed arrival at server.", tagId, timerCopy.getSupervisedName());
      return;
    }

    aliveTimerService.startOrUpdateTimestamp(tagId, useTimestamp);

    // TODO (Alex) Is this the timer we want to use?
    if (timerCopy.getSupervisedEntity() == SupervisionEntity.PROCESS) {
      Long processId = processFacade.getProcessIdFromAlive(tagId);
      processEvents.onUp(processId, useTimestamp, "Process Alive tag received.");
    } else {
      if (timerCopy.getSupervisedEntity() == EQUIPMENT) {
        equipmentEvents.onUp(timerCopy.getSupervisedId(), useTimestamp, "Equipment Alive tag received.");
      } else {
        // It is a subequipment
        subEquipmentEvents.onUp(timerCopy.getSupervisedId(), useTimestamp, "Subequipment Alive tag received.");
      }
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
