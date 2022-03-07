package cern.c2mon.server.cache.device.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;


public class DeviceClassIgniteQuery implements DeviceClassQuery {

    private final IgniteCacheImpl cache;

    public DeviceClassIgniteQuery(final Ehcache cache){
        this.cache = (IgniteCacheImpl) cache;
    }

    @Override
    public Long findDeviceByName(String deviceClassName) throws CacheElementNotFoundException {

        SqlFieldsQuery sql = new SqlFieldsQuery("select _key from DeviceClassCacheObject where NAME = ?").setArgs(deviceClassName);

        List<Long> deviceClassIds = new ArrayList<>();

        try (QueryCursor<List<?>> cursor = cache.sqlQueryCache(sql)) {
            for (List<?> row : cursor) {
                deviceClassIds.add((Long) row.get(0));
            }
        }

        if (deviceClassIds.isEmpty()) {
            throw new CacheElementNotFoundException("Failed to find a device class with name " + deviceClassName + " in the cache.");
        }

        return deviceClassIds.get(0);
    }
}

