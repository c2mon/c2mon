package cern.c2mon.client.core.config.dynamic.query;

import cern.c2mon.client.core.config.dynamic.DynConfigException;

import java.util.List;

/**
 * The IQueryObj holds the information contained in a query uri in a structured form and contains query-specific logic
 */
public interface IQueryObj {

    /**
     * Get the original URI as a string without any additional query parameters. Only protocol, host, port and path are returned
     * @return the original URI as a string without additional query parameters
     * @throws DynConfigException if the original URI did not contain host or authority information
     */
    String getUriWithoutParams() throws DynConfigException;

    /**
     * Checks whether a regular expression matches the IQueryObj. All fields, meaning the whole of the original query URI is considered
     * @param regex the regular expression to match
     * @return whether the regex matches the IQueryObj
     */
    boolean matches(String regex);

    /**
     * If query parameters keys correspond to method names of the applyToObj's class, attempt to invoke the method with the given and transformed query value as argument.
     * @param applyToObj the object whose methods to invoke
     * @throws DynConfigException if the method matching a required key cannot be invoked
     */
    void applyQueryPropertiesTo(Object applyToObj) throws DynConfigException;

    /**
     * Get the original string values of a query property whose key matches the given one. If the key is not contained in the query, return a default value.
     * @param key the key whose value to get
     * @return the value of the key, or a default value
     */
    List<String> get(QueryKey<String> key);

    /**
     * Transform the string values of a query property whose key matches the given one to the c, and return them. If the key is not contained in the query, return a default value.
     * @param key the key whose value to get
     * @param c the class into which the values shall be transformed
     * @param <T> the generic type of the key.
     * @return A List of values of class c corresponding to the transformed query property values.
     */
    <T> List<T> get(QueryKey<? extends T> key, Class<? extends T> c);

    /**
     * Check whether there is an explicitly set query parameter corresponding to the key
     * @param key the key to match
     * @return whether there is an explicitly set query parameter corresponding to the key
     */
    boolean contains(QueryKey<?> key);
}
