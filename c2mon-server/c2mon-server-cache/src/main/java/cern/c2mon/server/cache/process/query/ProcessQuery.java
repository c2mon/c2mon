package cern.c2mon.server.cache.process.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;

public interface ProcessQuery {

    Long findProcessIdByName(String processName) throws CacheElementNotFoundException;

}
