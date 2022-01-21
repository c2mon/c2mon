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

    private final IgniteCacheImpl cache;

    public DeviceIgniteQuery(final Ehcache cache){
        this.cache = (IgniteCacheImpl) cache;
    }

    @Override
    public List<Device> findDevicesByDeviceClassId(Long deviceClassId) throws CacheElementNotFoundException{

        if (deviceClassId == null) {
            throw new IllegalArgumentException("Attempting to retrieve a List of Devices from the cache with a NULL " +
                    "parameter.");
        }

        SqlFieldsQuery sql = new SqlFieldsQuery("select _val from DeviceCacheObject where DEVICECLASSID = ?").setArgs(deviceClassId);

        List<Device> deviceCacheObjects = new ArrayList<>();

        try (QueryCursor<List<?>> cursor = cache.getCache().query(sql)) {
            for (List<?> row : cursor) {
                deviceCacheObjects.add((DeviceCacheObject) row.get(0));
            }
        }

        if (deviceCacheObjects.isEmpty()) {
            throw new CacheElementNotFoundException("Failed to get device ids from cache");
        }

        return deviceCacheObjects;
    }
}

