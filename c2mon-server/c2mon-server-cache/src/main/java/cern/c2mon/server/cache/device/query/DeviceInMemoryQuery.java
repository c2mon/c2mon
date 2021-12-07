package cern.c2mon.server.cache.device.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeviceInMemoryQuery implements DeviceQuery {

    private final InMemoryCache cache;

    public DeviceInMemoryQuery(final Ehcache cache){
        this.cache = (InMemoryCache) cache;
    }

    @Override
    public List<Device> findDevicesByDeviceClassId(Long deviceClassId)  throws CacheElementNotFoundException{

        if (deviceClassId == null) {
            throw new IllegalArgumentException("Attempting to retrieve a List of Devices from the cache with a NULL " +
                    "parameter.");
        }

        List<Device> deviceCacheObjects;

        Predicate<Device> filter = tag -> tag.getDeviceClassId() == deviceClassId;

        try(Stream<Device> stream = cache.getCache().values().stream()){

            deviceCacheObjects = stream.filter(filter).collect(Collectors.toList());

            if (deviceCacheObjects.isEmpty()) {
                throw new CacheElementNotFoundException("Failed to get device ids from cache");
            }
        }
        return deviceCacheObjects;
    }

}
