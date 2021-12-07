package cern.c2mon.server.cache.process.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ProcessInMemoryQuery implements ProcessQuery {

    private final InMemoryCache cache;

    public ProcessInMemoryQuery(final Ehcache cache){
        this.cache = (InMemoryCache) cache;
    }

    @Override
    public Long findProcessIdByName(String processName) throws CacheElementNotFoundException{
        Optional<Long> processKey;

        if (processName == null || processName.equalsIgnoreCase("")) {
            throw new IllegalArgumentException("Attempting to retrieve a Process from the cache with a NULL or empty name parameter.");
        }

        Predicate<Process> filter = process -> process.getName().equals(processName);

        try(Stream<Process> stream = cache.getCache().values().stream()){

            processKey = stream.filter(filter).map(p -> p.getId()).findFirst();

            // Find the number of results -- the number of hits.
            if (!processKey.isPresent()) {
                throw new CacheElementNotFoundException("Failed to find a process with name " + processName + " in the cache.");
            }
        }

        return processKey.get();
    }
}
