package cern.c2mon.server.cache.process.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;

public class ProcessIgniteQuery implements ProcessQuery {

    private final IgniteCacheImpl cache;

    public ProcessIgniteQuery(final Ehcache cache){
        this.cache = (IgniteCacheImpl) cache;
    }

    @Override
    public Long findProcessIdByName(String processName) throws CacheElementNotFoundException{

        if (processName == null) {
            throw new IllegalArgumentException("Attempting to retrieve a List of Devices from the cache with a NULL " +
                    "parameter.");
        }

        SqlFieldsQuery sql = new SqlFieldsQuery("select _key from ProcessCacheObject where NAME = ?").setArgs(processName);

        List<Long> processIds = new ArrayList<>();

        try (QueryCursor<List<?>> cursor = cache.getCache().query(sql)) {
            for (List<?> row : cursor) {
                processIds.add((Long) row.get(0));
            }
        }

        if (processIds.isEmpty()) {
            throw new CacheElementNotFoundException("Failed to find a process with name " + processName + " in the cache.");
        }

        return processIds.get(0);
    }
}

