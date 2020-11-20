package cern.c2mon.client.core.config.dynamic.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;

/**
 * A class of query keys that must be handled specifically.
 * @param <R> the class of the default value
 */
@AllArgsConstructor
public class QueryKey<R> {
    @Getter
    private String keyName;

    @Getter
    private R defaultValue;

    @Getter
    private boolean required;

    @Getter
    @Setter
    private Class<?> targetClass;

    @Setter
    private Function<String, Boolean> verifier;

    /**
     * create a new query key
     * @param keyName the name of the query key
     */
    public QueryKey(String keyName) {
        this(keyName, null);
    }

    /**
     * create a new query key
     * @param keyName      the name of the query key
     * @param defaultValue the value to default to should the query key not be set
     */
    public QueryKey(String keyName, R defaultValue) {
        this(keyName, defaultValue, false);
    }

    /**
     * create a new query key
     * @param keyName      the name of the query key
     * @param defaultValue the value to default to should the query key not be set
     * @param required     is query key mandatory to be set in the the query
     */
    public QueryKey(String keyName, R defaultValue, boolean required) {
        this(keyName, defaultValue, required, Object.class, s -> true);
    }

    /**
     * some queries may match both protocol-specific and tag-related builder classes. This function verifies that a key
     * is in fact intended for a certain class.
     * @param target the builder or implementation class that the key is intended for.
     * @return whether the key is relevant for the class.
     */
    public boolean appliesTo(Class<?> target) {
        return targetClass.isAssignableFrom(target);
    }

    /**
     * Check whether the string s has a valid value for the key. This mostly targets keys that must have values of type
     * String, but follow certain patterns. For example, the REST-"mode" parameter must have the value "GET" or "POST".
     * @param s the string to check for validity
     * @return whether the string s has a valid value for the key.
     */
    public boolean isValid(String s) {
        return verifier.apply(s);
    }
}