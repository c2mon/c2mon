package cern.c2mon.server.cache.device.query;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

public class DeviceInMemoryQuery implements DeviceQuery {

    private final InMemoryCache<Long, Device> cache;

    public DeviceInMemoryQuery(final Ehcache<Long, Device> cache){
        this.cache = (InMemoryCache<Long, Device>) cache;
    }

    @Override
    public List<Device> findDevicesByDeviceClassId(Long deviceClassId)  throws CacheElementNotFoundException{

        if (deviceClassId == null) {
            throw new IllegalArgumentException("Attempting to retrieve a List of Devices from the cache with a NULL " +
                    "parameter.");
        }

        List<Device> deviceCacheObjects;

        Predicate<Device> filter = tag -> deviceClassId.equals(tag.getDeviceClassId());

        try(Stream<Device> stream = cache.getCache().values().stream()){

            deviceCacheObjects = stream.filter(filter).collect(Collectors.toList());

            if (deviceCacheObjects.isEmpty()) {
                throw new CacheElementNotFoundException("Failed to get device ids from cache");
            }
        }
        return deviceCacheObjects;
    }

    @Override
    public Long findDeviceIdByName(String deviceName) throws CacheElementNotFoundException {
        Optional<Long> deviceId;
        if (deviceName == null || deviceName.equalsIgnoreCase("")) {
            throw new IllegalArgumentException(
                    "Attempting to retrieve a Device from the cache with a NULL or empty name parameter.");
        }
        if (deviceName.contains("*") || deviceName.contains("?")) {
            throw new IllegalArgumentException(
                    "Attempting to retrieve a single Device from the cache with wildcard '*' or '?', which is not supported.");
        }

        Predicate<Device> filter = tag -> tag.getName().equals(deviceName);

        try(Stream<Device> stream = cache.getCache().values().stream()){

            deviceId = stream.filter(filter).map(Device::getId).findFirst();

            if (!deviceId.isPresent()) {
                throw new CacheElementNotFoundException("Failed to find a device with name " + deviceName + " in the cache.");
            }
        }

        return deviceId.get();
    }

}
