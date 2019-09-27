package cern.c2mon.cache.api.spi;

import cern.c2mon.shared.common.Cacheable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

// Create an impl that allows people to customize further?
// Perhaps create a builder pattern and an internal query taking a BiFunction as well?
public interface CacheQuery<V extends Cacheable> extends Function<V, Boolean> {

}
