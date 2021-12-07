package cern.c2mon.server.cache.device.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import javax.cache.Cache;
import java.util.List;
import java.util.Optional;

import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteClosure;


public class DeviceClassIgniteQuery implements DeviceClassQuery {

    private final IgniteCacheImpl cache;

    public DeviceClassIgniteQuery(final Ehcache cache){
        this.cache = (IgniteCacheImpl) cache;
    }

    @Override
    public Long findDeviceByName(String deviceClassName) throws CacheElementNotFoundException {
        Optional<Long> deviceClassId;

        IgniteBiPredicate<Long, DeviceClass> predicate = (id, deviceClass) -> deviceClass.getName().equals(deviceClassName);

        List<Long> deviceClassIds = cache.getCache().query(new ScanQuery<>(
                        predicate),
                (IgniteClosure<Cache.Entry<Long, DeviceClass>, Long>) Cache.Entry::getKey).getAll();

        if (deviceClassIds.isEmpty()) {
            throw new CacheElementNotFoundException("Failed to find a device class with name " + deviceClassName + " in the cache.");
        }

        return deviceClassIds.get(0);
    }
}

