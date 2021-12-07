package cern.c2mon.server.cache.process.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import javax.cache.Cache;
import java.util.List;

import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteClosure;

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

        IgniteBiPredicate<Long, Process> predicate = (id, process) -> process.getName().equals(processName);

        List<Long> deviceCacheObjects = cache.getCache().query(new ScanQuery<>(
                predicate),
                (IgniteClosure<Cache.Entry<Long, Process>, Long>) Cache.Entry::getKey).getAll();

        if (deviceCacheObjects.isEmpty()) {
            throw new CacheElementNotFoundException("Failed to find a process with name " + processName + " in the cache.");
        }

        return deviceCacheObjects.get(0);
    }
}

