package cern.c2mon.server.ehcache.search;

import cern.c2mon.server.ehcache.search.expression.Criteria;

public class Attribute<T> {

    public Attribute(String attributeName) {}

    public Criteria eq(T value) {
        return null;
    }

    public Criteria ilike(String regex) {
        return null;
    }
}
