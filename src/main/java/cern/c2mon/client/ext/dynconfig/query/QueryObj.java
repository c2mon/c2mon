package cern.c2mon.client.ext.dynconfig.query;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.ObjectConverter;
import cern.c2mon.client.ext.dynconfig.URIParser;
import cern.c2mon.client.ext.dynconfig.strategy.TagConfigStrategy;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static cern.c2mon.client.ext.dynconfig.DynConfigException.Context.INVALID_URI_PROPERTY;
import static cern.c2mon.client.ext.dynconfig.DynConfigException.Context.URI_MISSING_REQUIRED_PROPERTIES;

/**
 * The QueryObj holds the information contained in a query uri in a structured form and contains query-specific logic
 */
@Slf4j
public class QueryObj implements IQueryObj {
    private final Map<String, List<String>> properties;
    private final Collection<QueryKey<?>> keys;
    private final URI uri;

    /**
     * Creates a new instance of the QueryObj
     *
     * @param uri  the uri to parse into a query object
     * @param keys represent certain query parameters that a query must contain, or that must be handled in a non-standard way
     * @throws DynConfigException if the query does not contain certain mandatory keys
     */
    public QueryObj(URI uri, Collection<QueryKey<?>> keys) throws DynConfigException {
        this.uri = uri;
        this.keys = keys;
        properties = URIParser.splitQuery(uri);
        checkForMissingKeys();

        // Hack for a non-static default value
        this.properties.putIfAbsent(TagConfigStrategy.TAG_NAME.getKeyName(), Collections.singletonList(uri.toASCIIString()));
    }

    /**
     * Get the original URI as a string without any additional query parameters. Only protocol, host, port and path are returned
     * @return the original URI as a string without additional query parameters
     * @throws DynConfigException if the original URI did not contain host or authority information
     */
    @Override
    public String getUriWithoutParams() throws DynConfigException {
        if (uri.getScheme() == null || uri.getAuthority() == null) {
            throw new DynConfigException(URI_MISSING_REQUIRED_PROPERTIES, "Uri must contain a at least protocol and authority");
        }

        return uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
    }

    /**
     * Checks whether a regular expression matches the IQueryObj. All fields, meaning the whole of the original query URI is considered
     * @param regex the regular expression to match
     * @return whether the regex matches the IQueryObj
     */
    public boolean matches(String regex) {
        return uri.toString().matches(regex);
    }

    /**
     * Get the original string values of a query property whose key matches the given one. If the key is not contained in the query, return a default value.
     * @param key the key whose value to get
     * @return the value of the key, or a default value
     */
    public List<String> get(QueryKey<String> key) {
        return get(key, String.class);
    }

    /**
     * Transform the string values of a query property whose key matches the given one to the c, and return them. If the key is not contained in the query, return a default value.
     * @param key the key whose value to get
     * @param c the class into which the values shall be transformed
     * @param <T> the generic type of the key.
     * @return A List of values of class c corresponding to the transformed query property values.
     */
    public <T> List<T> get(QueryKey<? extends T> key, Class<? extends T> c) {
        if (!contains(key) || !properties.get(key.getKeyName()).stream().allMatch(key::isValid)) {
            return Collections.singletonList(key.getDefaultValue());
        }
        return properties.get(key.getKeyName()).stream().map(k -> ObjectConverter.convert(c, k)).collect(Collectors.toList());
    }

    /**
     * Check whether there is an explicitly set query parameter corresponding to the key
     * @param key the key to match
     * @return whether there is an explicitly set query parameter corresponding to the key
     */
    @Override
    public boolean contains(QueryKey<?> key) {
        return this.properties.containsKey(key.getKeyName());
    }

    /**
     * If query parameters keys correspond to method names of the applyToObj's class, attempt to invoke the method with the given and transformed query value as argument.
     * @param applyToObj the object whose methods to invoke
     * @throws DynConfigException if the method matching a required key cannot be invoked
     */
    @Override
    public void applyQueryPropertiesTo(Object applyToObj) throws DynConfigException {
        final Method[] declaredMethods = applyToObj.getClass().getDeclaredMethods();
        for (Map.Entry<String, List<String>> targetMap : properties.entrySet()) {
            Optional<Method> matchingMethod = Arrays.stream(declaredMethods)
                    .filter(m -> m.getName().equalsIgnoreCase(targetMap.getKey()) && m.getParameterTypes().length == 1)
                    .findFirst();
            if (matchingMethod.isPresent()) {
                for (String s : targetMap.getValue()) {
                    transformAndInvoke(matchingMethod.get(), s, applyToObj);
                }
            }
        }
    }

    /**
     * QueryObjs are considered equal if they are of the same type and their fields hold the same values.
     *
     * @param o the object to compare to
     * @return true if the objects are of the same type and their fields hold the same values.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryObj that = (QueryObj) o;
        return Objects.equals(uri.toString(), uri.toString()) &&
                properties.keySet().equals(that.properties.keySet()) &&
                properties.entrySet()
                        .stream()
                        .allMatch(e -> e.getValue().equals(that.properties.get(e.getKey())));
    }

    /**
     * Override the hashCode method to fit the custom equals implementation
     *
     * @return the hashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(uri.toString(), properties);
    }

    private void checkForMissingKeys() throws DynConfigException {
        Optional<QueryKey<?>> missingKey = keys.stream()
                .filter(k-> k.isRequired() && (!contains(k) || !properties.get(k.getKeyName()).stream().allMatch(k::isValid)))
                .findFirst();
        if (missingKey.isPresent()) {
            throw new DynConfigException(URI_MISSING_REQUIRED_PROPERTIES, missingKey.get().getKeyName());
        }
    }

    private void transformAndInvoke(Method m, String s, Object applyToObj) throws DynConfigException {
        try {
            Object o = ObjectConverter.convert(m.getParameterTypes()[0], s);
            m.invoke(applyToObj, o);
        } catch (IllegalAccessException | InvocationTargetException | UnsupportedOperationException e) {
           handle(m, s, applyToObj, e);
        }
    }

    private void handle(Method m, String s, Object applyToObj, Throwable e) throws DynConfigException {
        Optional<QueryKey<?>> key = keys.stream()
                .filter(k -> k.getKeyName().equalsIgnoreCase(m.getName()))
                .findFirst();
        Class<?> c = applyToObj.getClass();
        if (key.isPresent() && key.get().appliesTo(c) && key.get().isRequired()) {
            throw new DynConfigException(INVALID_URI_PROPERTY,
                    "Parameter " + c + "." + m.getName() + " with value " + s + " is mandatory. Aborting. ",
                    e.getCause());
        } else if (!key.isPresent() || key.get().appliesTo(c)) {
            log.error("An error occurred processing parameter value {}. Resorting to default.", s);
        }
    }
}