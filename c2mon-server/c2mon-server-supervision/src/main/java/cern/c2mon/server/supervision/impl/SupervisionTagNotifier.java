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
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.SupervisionAppender;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.supervision.SupervisionListener;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

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
 */
@Slf4j
@Service
public class SupervisionTagNotifier implements SupervisionListener, SmartLifecycle {

  /**
   * Register for notifications from the SupervisionNotifier bean.
   */
  private final SupervisionNotifier supervisionNotifier;

  /**
   * For adding supervision info.
   */
  private final SupervisionAppender supervisionAppender;

  /**
   * Caches used for accessing supervision states.
   */
  private final C2monCache<Process> processCache;
  private final C2monCache<Equipment> equipmentCache;
  private final C2monCache<SubEquipment> subEquipmentCache;
  private final SupervisionStateTagService stateTagService;
  private final DataTagService dataTagService;

  /**
   * Used for locating a Tag in the appropriate Tag cache.
   */
  private final TagCacheCollection unifiedTagCacheFacade;

  /**
   * Caches with listeners notified of supervision information (Tag caches).
   */
  private final C2monCache<DataTag> dataTagCache;
  private final C2monCache<RuleTag> ruleTagCache;

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

  /**
   * For lifecycle callback to stop listener threads.
   */
  private Lifecycle listenerContainer;

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  /**
   * @param supervisionAppender Reference to helper bean for adding the current supervision status
   *                            of Processes and Equipments to Tags
   */
  @Autowired
  public SupervisionTagNotifier(final SupervisionNotifier supervisionNotifier,
                                final TagCacheCollection unifiedTagCacheFacade,
                                final SupervisionAppender supervisionAppender,
                                final DataTagService dataTagService,
                                final ProcessService processService,
                                final EquipmentService equipmentService,
                                final SubEquipmentService subEquipmentService,
                                final C2monCache<RuleTag> ruleTagCache,
                                final SupervisionStateTagService stateTagService) {
    this.supervisionNotifier = supervisionNotifier;
    this.unifiedTagCacheFacade = unifiedTagCacheFacade;
    this.supervisionAppender = supervisionAppender;
    this.dataTagService = dataTagService;
    this.dataTagCache = dataTagService.getCache();
    this.processCache = processService.getCache();
    this.equipmentCache = equipmentService.getCache();
    this.subEquipmentCache = subEquipmentService.getCache();
    this.ruleTagCache = ruleTagCache;
    this.stateTagService = stateTagService;
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
    if (status == SupervisionStatus.RUNNING || status == SupervisionStatus.RUNNING_LOCAL
      || status == SupervisionStatus.STOPPED || status == SupervisionStatus.DOWN) {
      Long entityId = event.getEntityId();
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
   * @param equipmentId the equipment id
   */
  private void notifyEquipmentTags(final Long equipmentId) {
    try {
      notifyTags(dataTagService.getDataTagIdsByEquipmentId(equipmentId));
    } catch (CacheElementNotFoundException cacheEx) {
      log.warn("Unable to locate Equipment element during Tag supervision "
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
      notifyTags(dataTagService.getDataTagIdsBySubEquipmentId(subEquipmentId));
    } catch (CacheElementNotFoundException cacheEx) {
      log.warn("Unable to locate SubEquipment element during Tag supervision "
          + "change callback (so no invalidation callbacks performed for associated Tags)", cacheEx);
    }
  }

  private void notifyTags(final Collection<Long> tagIds) {
    // local set so as not to notify rules twice; lock on set when modifying
    Set<Long> notifiedRules = new HashSet<>();

    for (Long id : tagIds) {
      try {
        // recursively notifies all dependent rules also, once only
        callCacheNotification(id, notifiedRules);
      } catch (CacheElementNotFoundException cacheEx) {
        log.warn("Unable to locate Tag/Rule cache element during Tag supervision " // TODO ask DAQ refresh
          + "change callback (some Tags/Rules may have been omitted)", cacheEx);
      }
    }
  }

  /**
   * Private recursive method for calling all listeners; recursive calls for
   * calling the notification for all dependent rules also.
   * @param id tag id
   * @param notifiedRules set for preventing multiple notifications for rules
   */
  private void callCacheNotification(final Long id, final Set<Long> notifiedRules) {
    synchronized (notifiedRules) {
      Tag tagCopy = unifiedTagCacheFacade.get(id);

      if (!notifiedRules.contains(tagCopy.getId())) {
        log.trace("Performing supervision notification for tag " + id);

        List<Long> stateTagIds = Stream.of(
          tagCopy.getProcessIds().stream().filter(processCache::containsKey).map(processCache::get),
          tagCopy.getEquipmentIds().stream().filter(equipmentCache::containsKey).map(equipmentCache::get),
          tagCopy.getSubEquipmentIds().stream().filter(subEquipmentCache::containsKey).map(subEquipmentCache::get)
        )
        .flatMap(identity())
        .map(Supervised::getStateTagId)
        .collect(toList());

        stateTagIds.forEach(stateTagId ->
          supervisionAppender.addSupervisionQuality(tagCopy, stateTagService.getSupervisionEvent(stateTagId))
        );

        if (!stateTagIds.isEmpty()) {
          if (tagCopy instanceof DataTag) {
            dataTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, (DataTag) tagCopy);
          } else if (tagCopy instanceof RuleTag) {
            ruleTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, (RuleTag) tagCopy);
          } else {
            throw new IllegalArgumentException("Unexpected call with Tag parameter that is neither DataTag or RuleTag; "
                + "type is " + tagCopy.getClass().getSimpleName());
          }
        }
      }

      for (Long ruleId : tagCopy.getRuleIds()) {
        callCacheNotification(ruleId, notifiedRules);
        notifiedRules.add(ruleId);
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
    log.debug("Starting SupervisionTagNotifier");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    log.debug("Stopping SupervisionTagNotifier");
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST + 1;
  }
}
