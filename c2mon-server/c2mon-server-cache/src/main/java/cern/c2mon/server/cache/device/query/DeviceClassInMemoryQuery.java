package cern.c2mon.server.cache.device.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;


public class DeviceClassInMemoryQuery implements DeviceClassQuery {

    private final InMemoryCache cache;

    public DeviceClassInMemoryQuery(final Ehcache cache){
        this.cache = (InMemoryCache) cache;
    }

    @Override
    public Long findDeviceByName(String deviceClassName) throws CacheElementNotFoundException {
        Optional<Long> deviceClassId;

        Predicate<DeviceClass> filter = tag -> tag.getName().equals(deviceClassName);

        try(Stream<DeviceClass> stream = cache.getCache().values().stream()){

            deviceClassId = stream.filter(filter).map(a -> a.getId()).findFirst();

            if (!deviceClassId.isPresent()) {
                throw new CacheElementNotFoundException("Failed to find a device class with name " + deviceClassName + " in the cache.");
            }
        }

        return deviceClassId.get();
    }
}
