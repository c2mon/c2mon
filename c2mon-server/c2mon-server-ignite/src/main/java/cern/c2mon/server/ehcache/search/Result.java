package cern.c2mon.server.ehcache.search;

public interface Result {

    Object getKey() throws SearchException;

    Object getValue() throws SearchException;
}
