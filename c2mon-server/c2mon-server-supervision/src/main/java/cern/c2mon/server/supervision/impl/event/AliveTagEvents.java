package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class AliveTagEvents {
  private static final Logger LOG = LoggerFactory.getLogger(AliveTagEvents.class);

  private final AliveTagService aliveTimerService;

  @Inject
  public AliveTagEvents(AliveTagService aliveTimerService) {
    this.aliveTimerService = aliveTimerService;
  }

  /**
   * Calls the ProcessDown, EquipmentDown or SubEquipmentDown methods depending
   * on the type of alive that has expired.
   */
  public void onAliveTimerExpiration(final Long aliveTimerId) {
    if (aliveTimerId == null) {
      LOG.warn("onAliveTimerExpiration(null) called - ignoring the call.");
      return;
    }

    if (!aliveTimerService.isRegisteredAliveTimer(aliveTimerId)) {
      LOG.error("AliveTimer received does not exist in cache - unable to take any action on alive reception.");
      return;
    }

    AliveTag aliveTimer = aliveTimerService.getCache().get(aliveTimerId);

    LOG.debug("Alive of {} {} (alive tag: {}) has expired.",
      aliveTimer.getSupervisedEntity(),  aliveTimer.getSupervisedName(), aliveTimer.getId());

    SupervisionEntity supervisedEntity = aliveTimer.getSupervisedEntity();

    // TODO (Alex) Consider try-catching this?
    SupervisionEventHandler.getEventHandlers().get(supervisedEntity).onAliveTimerDown(aliveTimer.getSupervisedId());
  }
}
