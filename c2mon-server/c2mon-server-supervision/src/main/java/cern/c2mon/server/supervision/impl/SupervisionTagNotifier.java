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

import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.SupervisionAppender;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.config.tag.UnifiedTagCacheFacade;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.supervision.SupervisionListener;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * On supervision status changes, calls listeners of all C2monCacheWithSupervision
 * that are registered for Tag update notifications on supervision changes.
 *
 * <p>Only passes on DOWN/STOPPED and RUNNING notifications (start-up ignored).
 *
 * <p>Only a tag lock is held during the notification procedure. The process and
 * equipment locks are accessed when getting copies of the Equipment/Tag lists
 * and when checking the Process/Equipment status'. Tag lock is held while performing
 * the invalidation AND notifying the listeners: this is REQUIRED to prevent successive
 * supervision changes from overtaking each other (the process state is appended while
 * this lock is held). Notice that as a result, there is no guarantee the listener
 * will be notified of all status changes if there are successive change close together
 * (in this case the listener may receive 2 notifications with the latest status only).
 *
 * <p>Timestamps are not changed when supervision
 * status is added to Tag object: as a result, listeners can filter out supervision
 * callbacks if they are overtaken by a newer incoming value (may happen since
 * many callbacks are made and this could last some time).
 *
 * <p>Notice that if a cache element is reconfigured during one of these supervision
 * notifications, the corresponding callback may fail for the given element and any
 * dependent elements (eg. Rules dependent on a Tag).
 *
 * @author Mark Brightwell
 *
 */
@Service
public class SupervisionTagNotifier implements SupervisionListener, SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SupervisionTagNotifier.class);

  /**
   * Register for notifications from the SupervisionNotifier bean.
   */
  private SupervisionNotifier supervisionNotifier;

  /**
   * For adding supervision info.
   */
  private SupervisionAppender supervisionAppender;

  /**
   * Caches used for accessing supervision states.
   */
  private C2monCache<Process> processCache;
  private C2monCache<Equipment> equipmentCache;
  private C2monCache<SubEquipment> subEquipmentCache;
  private DataTagService dataTagService;
  private ProcessService processService;
  private EquipmentService equipmentService;
  private SubEquipmentService subEquipmentService;

  /**
   * Used for locating a Tag in the appropriate Tag cache.
   */
  private UnifiedTagCacheFacade unifiedTagCacheFacade;

  /**
   * Caches with listeners notified of supervision information (Tag caches).
   */
  private C2monCache<DataTag> dataTagCache;
  private C2monCache<RuleTag> ruleTagCache;

  /**
   * Caches used to filter out older supervision events, to prevent overtaking of DOWN and
   * UP events *for a given tag*. If a single Tag has already been notified of a more recent
   * event, no more Tags will be notified of older events (avoid using a time for each tag
   * individually!, resulting in large maps).
   *
   * <p>Lock is used for both maps.
   *
   * <p>All elements are shared through the cluster
   */

  /** Cluster cache key lock */
  protected static final String EVENT_LOCK = "c2mon.supervision.SupervisionTagNotifier.eventLock";

  /**
   * For lifecycle callback to stop listener threads.
   */
  private Lifecycle listenerContainer;

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  /**
   * @param supervisionAppender
   *        Reference to helper bean for adding the current supervision status of Processes
   *        and Equipments to Tags
   */
  @Autowired
  public SupervisionTagNotifier(final SupervisionNotifier supervisionNotifier,
                                final UnifiedTagCacheFacade unifiedTagCacheFacade,
                                final SupervisionAppender supervisionAppender,
                                DataTagService dataTagService, final ProcessService processService,
                                final EquipmentService equipmentService,
                                final SubEquipmentService subEquipmentService,
                                final C2monCache<RuleTag> ruleTagCache) {
    super();
    this.supervisionNotifier = supervisionNotifier;
    this.unifiedTagCacheFacade = unifiedTagCacheFacade;
    this.supervisionAppender = supervisionAppender;
    this.dataTagService = dataTagService;
    this.processService = processService;
    this.equipmentService = equipmentService;
    this.subEquipmentService = subEquipmentService;
    this.dataTagCache = dataTagService.getCache();
    this.ruleTagCache = ruleTagCache;

    this.processCache = processService.getCache();
    this.equipmentCache = equipmentService.getCache();
    this.subEquipmentCache = subEquipmentService.getCache();
  }

  /**
   * Run on bean creation. Registers with SupervisionNotifier.
   */
  @PostConstruct
  public void init() {
    listenerContainer = supervisionNotifier.registerAsListener(this);
  }

  @Override
  public void notifySupervisionEvent(final SupervisionEvent event) {
    SupervisionStatus status = event.getStatus();
    Long entityId = event.getEntityId();
    if (status.equals(SupervisionStatus.RUNNING) || status.equals(SupervisionStatus.RUNNING_LOCAL)
      || status.equals(SupervisionStatus.STOPPED) || status.equals(SupervisionStatus.DOWN)) {


      switch (event.getEntity()) {
        case PROCESS:
          notifyProcessTags(entityId);
          break;
        case EQUIPMENT:
          notifyEquipmentTags(entityId);
          break;
        case SUBEQUIPMENT:
          notifySubEquipmentTags(entityId);
          break;
        default:
          break;
      }
    }
  }

  /**
   * Notifies all equipments under this process. Will use event in local map.
   * @param processId process id
   */
  private void notifyProcessTags(final Long processId) {
    Process process = processCache.get(processId);
    for (Long equipmentId : process.getEquipmentIds()) { //no lock required as get copy
      notifyEquipmentTags(equipmentId);
    }
  }

  /**
   * Calls notification method for all tags associated to the Equipment (DataTags only).
   * @param equipementId the equipment id
   */
  private void notifyEquipmentTags(final Long equipementId) {
    try {
      //local map so as not to notify rules twice; lock on map when modifying
      Map<Long, Boolean> notifiedRules = new HashMap<>();
      Collection<Long> tagIds = dataTagService.getDataTagIdsByEquipmentId(equipementId);
      for (Long id : tagIds) {
       try {
         callCacheNotification(id, notifiedRules); //recursively notifies all dependent rules also, once only
       } catch (CacheElementNotFoundException cacheEx) {
         LOGGER.warn("Unable to locate Tag/Rule cache element during Tag supervision " //TODO ask DAQ refresh
             + "change callback (some Tags/Rules may have been omitted)", cacheEx);
       }
      }
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.warn("Unable to locate Equipment element during Tag supervision "
          + "change callback (so no invalidation callbacks performed for associated Tags)", cacheEx);
    }
  }

  /**
   * Calls notification method for all tags associated to a SubEquipment.
   *
   * @param subEquipmentId the sub equipment id
   */
  private void notifySubEquipmentTags(final Long subEquipmentId) {
    try {
      //local map so as not to notify rules twice; lock on map when modifying
      Map<Long, Boolean> notifiedRules = new HashMap<>();
      Collection<Long> tagIds = subEquipmentService.getDataTagIds(subEquipmentId);
      for (Long id : tagIds) {
       try {
         callCacheNotification(id, notifiedRules); //recursively notifies all dependent rules also, once only
       } catch (CacheElementNotFoundException cacheEx) {
         LOGGER.warn("Unable to locate Tag/Rule cache element during Tag supervision " //TODO ask DAQ refresh
             + "change callback (some Tags/Rules may have been omitted)", cacheEx);
       }
      }
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.warn("Unable to locate SubEquipment element during Tag supervision "
          + "change callback (so no invalidation callbacks performed for associated Tags)", cacheEx);
    }
  }

  /**
   * Private recursive method for calling all listeners; recursive calls for
   * calling the notification for all dependent rules also.
   * @param id tag id
   * @param notifiedRules map for preventing multiple notifications for rules
   */
  private void callCacheNotification(final Long id, final Map<Long, Boolean> notifiedRules) {
    synchronized (notifiedRules) {
      Tag tagCopy = unifiedTagCacheFacade.get(id);
      if (!notifiedRules.containsKey(tagCopy.getId())) {
        LOGGER.trace("Performing supervision notification for tag " + id);
        boolean dirtyTagContext = false;

        for (Long procId : tagCopy.getProcessIds()) {
          if (processCache.containsKey(procId)) { //null never override a value, so if statement ok out of lock
            supervisionAppender.addSupervisionQuality(tagCopy, processCache.get(procId).getSupervisionEvent());
            dirtyTagContext = true;
          }
        }
        for (Long eqId : tagCopy.getEquipmentIds()) {
          if (equipmentCache.containsKey(eqId)) {
            supervisionAppender.addSupervisionQuality(tagCopy, equipmentCache.get(eqId).getSupervisionEvent());
            dirtyTagContext = true;
          }
        }
        for (Long subEqId : tagCopy.getSubEquipmentIds()) {
          if (subEquipmentCache.containsKey(subEqId)) {
            supervisionAppender.addSupervisionQuality(tagCopy, subEquipmentCache.get(subEqId).getSupervisionEvent());
            dirtyTagContext = true;
          }
        }

        if (dirtyTagContext) {
          if (tagCopy instanceof DataTag) {
            dataTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE,(DataTag) tagCopy);
          } else if (tagCopy instanceof RuleTag) {
            ruleTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE,(RuleTag) tagCopy);
          } else {
            throw new IllegalArgumentException("Unexpected call with Tag parameter that is neither DataTag or RuleTag; "
                + "type is " + tagCopy.getClass().getSimpleName());
          }
        }
      }

      Collection<Long> ruleIds;
      ruleIds = new ArrayList<>(tagCopy.getRuleIds());
      for (Long ruleId : ruleIds) {
        callCacheNotification(ruleId, notifiedRules);
        notifiedRules.put(ruleId, true);
      }
    }
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable runnable) {
    stop();
    runnable.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    LOGGER.debug("Starting SupervisionTagNotifier");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    LOGGER.debug("Stopping SupervisionTagNotifier");
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST + 1;
  }

}
