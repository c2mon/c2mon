package cern.c2mon.cache.actions.supervision;

import cern.c2mon.server.common.supervision.Supervised;

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
  default void start(long id, Timestamp timestamp) {
    getSupervisedService().start(id, timestamp);
  }

  @Override
  default void stop(long id, Timestamp timestamp) {
    getSupervisedService().stop(id, timestamp);
  }

  @Override
  default void resume(long id, Timestamp timestamp, String message) {
    getSupervisedService().resume(id, timestamp, message);
  }

  @Override
  default void suspend(long id, Timestamp timestamp, String message) {
    getSupervisedService().suspend(id, timestamp, message);
  }
}
