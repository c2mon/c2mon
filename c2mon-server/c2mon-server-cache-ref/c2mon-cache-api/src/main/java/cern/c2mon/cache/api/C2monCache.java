package cern.c2mon.cache.api;

import cern.c2mon.shared.common.Cacheable;

/**
 * This interface is a partially static convenience reference to {@link AbstractCache}&lt;Long,*&gt;
 * <p>
 * Since any {@link Cacheable} has a {@code Long} key by default it is only for rare and exceptional
 * use cases that you will want to implement {@code C2monCache} instead of this
 * <p>
 * Do NOT add any methods to this interface. It is just a static type reference and the code
 * (e.g unchecked casts) operates on this assumption
 *
 * @param <V> the type of {@code Cacheable} in the cache
 */
public interface C2monCache<V extends Cacheable> extends AbstractCache<Long, V> {
}
