package cern.c2mon.cache.actions;

import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.actions.tag.TagController;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.CacheUpdateFlow;
import cern.c2mon.server.common.control.ControlTag;
import lombok.extern.slf4j.Slf4j;

/**
 * Hosts the shared functionality for {@link ControlTag} Services whose
 * values are {@code Boolean}
 *
 * @param <T> the type of control tag - one of {@link cern.c2mon.server.common.alive.AliveTag}
 *           or {@link cern.c2mon.server.common.commfault.CommFaultTag}
 */
@Slf4j
public abstract class AbstractBooleanControlTagService<T extends ControlTag> extends AbstractCacheServiceImpl<T>
  implements SupervisedCacheService<T> {


  /**
   * Constructor for internal use by implementations
   */
  protected AbstractBooleanControlTagService(C2monCache<T> cache, CacheUpdateFlow<T> c2monCacheFlow) {
    super(cache, c2monCacheFlow);
  }

  /**
   * Find the {@code ControlTag} object with {@code id} in the cache
   * and if it is stopped (not active), then update its value and
   * timestamp
   *
   * The timestamp will not be updated, unless there is a change.
   * The cache object will not be reinserted, unless there is a change.
   *
   * @param id the alive timer id for the object to be force started
   * @throws NullPointerException when {@code id} is null
   */
  @Override
  public void start(long id, long timestamp) {
    setTagAsActive(id, true, timestamp);
  }

  /**
   * Find the {@code AliveTimer} object with {@code id} in the cache
   * and if it is started (active), then update its value and
   * timestamp
   *
   * The timestamp will not be updated, unless there is a change.
   * The cache object will not be reinserted, unless there is a change.
   *
   * @param id the alive timer id for the object to be force started
   * @throws NullPointerException when {@code id} is null
   */
  @Override
  public void stop(long id, long timestamp) {
    setTagAsActive(id, false, timestamp);
  }

  @Override
  public void resume(long aliveTimerId, long timestamp, String message) {
    start(aliveTimerId, timestamp);
  }

  @Override
  public void suspend(long aliveTimerId, long timestamp, String message) {
    stop(aliveTimerId, timestamp);
  }

  protected void setTagAsActive(long controlTagId, boolean active, long timestamp) {
    log.debug("Attempting to set control tag " + controlTagId + " to " + active);

    if (!cache.containsKey(controlTagId)) {
      log.error("Cannot locate the Control tag in the cache (Id is #{}) - unable to stop it.", controlTagId);
      return;
    }

    try {
      cache.compute(controlTagId, controlTag -> compareAndSetNewValues(controlTag, active, timestamp));
    } catch (Exception e) {
      log.error("Unable to stop the alive timer " + controlTagId, e);
    }
  }

  protected void compareAndSetNewValues(T controlTag, boolean active, long timestamp) {
    if (controlTag.getValue() != active || timestamp >= controlTag.getTimestamp().getTime()) {
      controlTag.setValue(active);
      TagController.validate(controlTag);
    }
  }
}
