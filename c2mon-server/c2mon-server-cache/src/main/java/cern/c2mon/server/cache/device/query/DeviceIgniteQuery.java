package cern.c2mon.server.cache.device.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import javax.cache.Cache;
import java.util.List;

import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteClosure;

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

        IgniteBiPredicate<Long, Device> predicate = (id, device) -> device.getDeviceClassId() == deviceClassId;

        List<Device> deviceCacheObjects = cache.getCache().query(new ScanQuery<>(
                predicate),
                (IgniteClosure<Cache.Entry<Long, Device>, Device>) Cache.Entry::getValue).getAll();

        if (deviceCacheObjects.isEmpty()) {
            throw new CacheElementNotFoundException("Failed to get device ids from cache");
        }

        return deviceCacheObjects;
    }
}

