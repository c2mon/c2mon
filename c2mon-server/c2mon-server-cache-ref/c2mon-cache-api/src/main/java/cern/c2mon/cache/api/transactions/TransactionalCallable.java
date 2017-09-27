package cern.c2mon.cache.api.transactions;

/**
 * @author Szymon Halastra
 */
@FunctionalInterface
public interface TransactionalCallable<T> {

  T call();
}
