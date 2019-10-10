package cern.c2mon.server.cache.supervision;

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
public interface SupervisedServiceDelegator<T extends Supervised> extends SupervisedService<T> {

  SupervisedService<T> getSupervisedService();

  @Override
  default SupervisionEvent getSupervisionStatus(Long id) {
    return getSupervisedService().getSupervisionStatus(id);
  }

  @Override
  default void refreshAndNotifyCurrentSupervisionStatus(Long id) {
    getSupervisedService().refreshAndNotifyCurrentSupervisionStatus(id);
  }

  @Override
  default void start(Long id) {
    getSupervisedService().start(id);
  }

  @Override
  @Deprecated
  default void start(Long id, Timestamp timestamp) {
    getSupervisedService().start(id, timestamp);
  }

  @Override
  default void start(T supervised) {
    getSupervisedService().start(supervised);
  }

  @Override
  @Deprecated
  default void start(T supervised, Timestamp timestamp) {
    getSupervisedService().start(supervised, timestamp);
  }

  @Override
  default void stop(T supervised, Timestamp timestamp) {
    getSupervisedService().stop(supervised, timestamp);
  }

  @Override
  default void stop(Long id, Timestamp timestamp) {
    getSupervisedService().stop(id, timestamp);
  }

  @Override
  default void resume(Long id, Timestamp timestamp, String message) {
    getSupervisedService().resume(id, timestamp, message);
  }

  @Override
  default void suspend(Long id, Timestamp timestamp, String message) {
    getSupervisedService().suspend(id, timestamp, message);
  }

  @Override
  default boolean isRunning(T supervised) {
    return getSupervisedService().isRunning(supervised);
  }

  @Override
  default boolean isRunning(Long id) {
    return getSupervisedService().isRunning(id);
  }

  @Override
  default boolean isUncertain(T supervised) {
    return getSupervisedService().isUncertain(supervised);
  }

  @Override
  default void removeAliveTimer(Long id) {
    getSupervisedService().removeAliveTimer(id);
  }

  @Override
  default void loadAndStartAliveTag(Long supervisedId) {
    getSupervisedService().loadAndStartAliveTag(supervisedId);
  }

  @Override
  default void removeAliveDirectly(Long aliveId) {
    getSupervisedService().removeAliveDirectly(aliveId);
  }

  @Override
  default SupervisionConstants.SupervisionEntity getSupervisionEntity() {
    return getSupervisedService().getSupervisionEntity();
  }

  @Override
  default void setSupervisionEntity(SupervisionConstants.SupervisionEntity entity) {
    getSupervisedService().setSupervisionEntity(entity);
  }
}
