package cern.c2mon.cache.actions.supervision;

import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

import java.sql.Timestamp;

/**
 * This interface simply delegates all the {@code SupervisedService} methods to the
 * {@code SupervisedService#getSupervisedService()} method.
 * <p>
 * None of the operations do anything other than delegate. There are no side effects. This class serves so
 * that you can override only the methods you actually need in an implementation.
 * <p>
 * Do NOT add any custom logic here. Add it to implementation of this class. If you would like to override a lot of
 * functionality, make a child class.
 *
 * @param <T> the type of {@link Supervised} objects the cache contains
 */
public interface SupervisedCacheServiceDelegator<T extends Supervised> extends SupervisedCacheService<T> {

  SupervisedCacheService<T> getSupervisedService();

  @Override
  default SupervisionEvent getSupervisionStatus(long id) {
    return getSupervisedService().getSupervisionStatus(id);
  }

  @Override
  default void refresh(long id) {
    getSupervisedService().refresh(id);
  }

  @Override
  @Deprecated
  default T start(long id, Timestamp timestamp) {
    return getSupervisedService().start(id, timestamp);
  }

  @Override
  default T stop(long id, Timestamp timestamp) {
    return getSupervisedService().stop(id, timestamp);
  }

  @Override
  default T resume(long id, Timestamp timestamp, String message) {
    return getSupervisedService().resume(id, timestamp, message);
  }

  @Override
  default T suspend(long id, Timestamp timestamp, String message) {
    return getSupervisedService().suspend(id, timestamp, message);
  }

  @Override
  default boolean isRunning(long id) {
    return getSupervisedService().isRunning(id);
  }

  @Override
  default boolean isUncertain(long id) {
    return getSupervisedService().isUncertain(id);
  }

  @Override
  default void removeAliveTimerBySupervisedId(long id) {
    getSupervisedService().removeAliveTimerBySupervisedId(id);
  }

  @Override
  default void loadAndStartAliveTag(long supervisedId) {
    getSupervisedService().loadAndStartAliveTag(supervisedId);
  }

  @Override
  default SupervisionConstants.SupervisionEntity getSupervisionEntity() {
    return getSupervisedService().getSupervisionEntity();
  }
}
