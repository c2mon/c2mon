package cern.c2mon.server.cache.command.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;

public interface CommandTagQuery {

    Long findCommandTagIdByName(String name) throws CacheElementNotFoundException;
}
