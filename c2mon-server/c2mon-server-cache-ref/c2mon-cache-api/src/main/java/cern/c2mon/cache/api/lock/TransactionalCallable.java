package cern.c2mon.cache.api.lock;

/**
 * @author Szymon Halastra
 */
@FunctionalInterface
public interface TransactionalCallable {

  void call();
}
