package cern.c2mon.server.cache.command.query;

public interface CommandTagQuery {

    Long findCommandTagIdByName(String name);
}
