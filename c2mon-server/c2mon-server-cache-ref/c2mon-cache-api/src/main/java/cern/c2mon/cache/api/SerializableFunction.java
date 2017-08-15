package cern.c2mon.cache.api;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author Szymon Halastra
 */

@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}
