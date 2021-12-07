package cern.c2mon.server.ehcache.search;

/**
 * Represents a single cache entry that has been selected by a cache query.
 */
public interface Result {

    Object getKey() throws SearchException;

    Object getValue() throws SearchException;
}
