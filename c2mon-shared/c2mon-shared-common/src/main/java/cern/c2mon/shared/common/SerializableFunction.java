package cern.c2mon.shared.common;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Custom implementation of Java functions with an extra casting of Serializable
 * which allows the deserialization of lambdas used by Ignite in a client server infrastructure
 * @param <V>
 * @param <R>
 */
public interface SerializableFunction<V, R> extends Function<V, R>, Serializable {
}
