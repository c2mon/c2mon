package cern.c2mon.server.cache.device.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;

public class DeviceIgniteQuery implements DeviceQuery {

    private final IgniteCacheImpl<Long, Device> cache;

    public DeviceIgniteQuery(final Ehcache<Long, Device> cache){
        this.cache = (IgniteCacheImpl<Long, Device>) cache;
    }

    @Override
    public List<Device> findDevicesByDeviceClassId(Long deviceClassId) throws CacheElementNotFoundException{

        if (deviceClassId == null) {
            throw new IllegalArgumentException("Attempting to retrieve a List of Devices from the cache with a NULL " +
                    "parameter.");
        }

        SqlFieldsQuery sql = new SqlFieldsQuery("select _val from DeviceCacheObject where DEVICECLASSID = ?").setArgs(deviceClassId);

        List<Device> deviceCacheObjects = new ArrayList<>();

        try (QueryCursor<List<?>> cursor = cache.sqlQueryCache(sql)) {
            for (List<?> row : cursor) {
                deviceCacheObjects.add((DeviceCacheObject) row.get(0));
            }
        }

        if (deviceCacheObjects.isEmpty()) {
            throw new CacheElementNotFoundException("Failed to get device ids from cache");
        }

        return deviceCacheObjects;
    }

    @Override
    public Long findDeviceIdByName(String deviceName) throws CacheElementNotFoundException {
        if (deviceName == null || deviceName.equalsIgnoreCase("")) {
            throw new IllegalArgumentException(
                    "Attempting to retrieve a Device from the cache with a NULL or empty name parameter.");
        }
        if (deviceName.contains("*") || deviceName.contains("?")) {
            throw new IllegalArgumentException(
                    "Attempting to retrieve a single Device from the cache with wildcard '*' or '?', which is not supported.");
        }
        
        SqlFieldsQuery sql = new SqlFieldsQuery("select _key from DeviceCacheObject where NAME = ?").setArgs(deviceName);

        List<Long> deviceIds = new ArrayList<>();

        try (QueryCursor<List<?>> cursor = cache.sqlQueryCache(sql)) {
            for (List<?> row : cursor) {
                deviceIds.add((Long) row.get(0));
            }
        }

        if (deviceIds.isEmpty()) {
            throw new CacheElementNotFoundException("Failed to find a device class with name " + deviceName + " in the cache.");
        }

        return deviceIds.get(0);
    }
}

