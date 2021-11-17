package cern.c2mon.server.ehcache.search;

import cern.c2mon.server.ehcache.search.expression.Criteria;

public interface Query {

    Query includeKeys();

    Query addCriteria(Criteria criteria);

    Query maxResults(int maxResults);

    Results execute() throws SearchException;

    Query includeValues();
}
