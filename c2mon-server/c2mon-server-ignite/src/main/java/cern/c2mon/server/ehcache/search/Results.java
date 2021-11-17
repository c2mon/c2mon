package cern.c2mon.server.ehcache.search;

import java.util.List;

public interface Results {

    void discard();

    int size();

    List<Result> all() throws SearchException;

    boolean hasKeys();
}
