package cern.c2mon.server.cache.datatag.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import javax.cache.Cache;
import java.util.List;
import java.util.Objects;

import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteClosure;

public class DataTagIgniteQuery implements DataTagQuery {

    private final IgniteCacheImpl<Long, DataTag> cache;

    public DataTagIgniteQuery(final Ehcache cache){
        this.cache = (IgniteCacheImpl) cache;
    }

    @Override
    public List<Long> findDataTagIdsByEquipmentId(Long equipmentId) throws CacheElementNotFoundException{

        if (equipmentId == null) {
            throw new IllegalArgumentException("Attempting to retrieve a List of DataTag ids from the cache with a NULL " +
                    "parameter.");
        }

        IgniteBiPredicate<Long, DataTag> predicate = (id, dataTag) -> Objects.equals(dataTag.getEquipmentId(), equipmentId);

        List<Long> tagIds = cache.getCache().query(new ScanQuery<>(
                predicate),
                (IgniteClosure<Cache.Entry<Long, DataTag>, Long>) Cache.Entry::getKey).getAll();

         /*TODO query can't fail ?
            throw new CacheElementNotFoundException("Failed to execute query with EquipmentId " + equipmentId + " : " +
                    "Result is null.");*/


        return tagIds;
    }

    @Override
    public List<Long> findDataTagIdsBySubEquipmentId(Long subEquipmentId) throws CacheElementNotFoundException{

        if (subEquipmentId == null) {
            throw new IllegalArgumentException("Attempting to retrieve a List of DataTag ids from the cache with a NULL " +
                    "parameter.");
        }

        IgniteBiPredicate<Long, DataTag> predicate = (id, dataTag) -> Objects.equals(dataTag.getSubEquipmentId(), subEquipmentId);

        List<Long> tagIds = cache.getCache().query(new ScanQuery<>(
                        predicate),
                (IgniteClosure<Cache.Entry<Long, DataTag>, Long>) Cache.Entry::getKey).getAll();

        /*TODO query can't fail ?
            throw new CacheElementNotFoundException("Failed to execute query with (sub)EquipmentId " + subEquipmentId + " : " +
                    "Result is null.");
                    */


        return tagIds;
    }
}

