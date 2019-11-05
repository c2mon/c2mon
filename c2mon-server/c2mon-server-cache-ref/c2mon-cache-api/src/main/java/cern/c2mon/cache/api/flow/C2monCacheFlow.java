package cern.c2mon.cache.api.flow;

import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;
import lombok.NonNull;
import org.springframework.lang.Nullable;

import java.util.Set;

/**
 * Collects business logic methods related to putting an object in the cache
 *
 * @param <T> the type of {@link Cacheable}s this cache is handling
 */
public interface C2monCacheFlow<T extends Cacheable> {

  /**
   * Validates that {@code this} object should be inserted.
   * <p>
   * Typical uses of this method would be to verify that the object is complete and
   * correct, while also able to test against the previous object, e.g for a later
   * timestamp. Be wary that the previous object will be {@code null} during the
   * first value insertion.
   * <p>
   * Parallelism / Concurrency:
   * <ul>
   *   <li> This method can have side effects. If {@code this} mutates the changes
   *        will stay with it as it is put in the cache.
   *   <li> While {@code previous} is a frozen clone, be wary of {@code newer}
   *        being modified from another thread.
   * </ul>
   *
   * @param older potentially null, the previous object if one existed
   * @return boolean true if this object should be put in the cache - false otherwise
   */
  boolean preInsertValidate(@Nullable T older, @NonNull T newer);

  /**
   * Executes any post insertion logic and creates all post insertion events
   * <p>
   * Any mutations to this object will not affect the cache in any way. The object
   * has already been inserted to the cache and changing this reference will do
   * nothing to change that reference, until you explicitly do {@code Cache.put}
   * <p>
   * When implementing this, make sure to call to super, in order to avoid
   * missing any events!
   *
   * @param older potentially null, the previous object if one existed
   * @return a set of all {@link CacheEvent}s that should be fired based on this change
   */
  Set<CacheEvent> postInsertEvents(@Nullable T older, @NonNull T newer);
}
