package cern.c2mon.server.cache.datatag.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;

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

        SqlFieldsQuery sql = new SqlFieldsQuery("select _key from DataTagCacheObject where EQUIPMENTID = ?").setArgs(equipmentId);

        List<Long> tagIds = new ArrayList<>();

        try (QueryCursor<List<?>> cursor = cache.sqlQueryCache(sql)) {
            for (List<?> row : cursor) {
               tagIds.add((Long) row.get(0));
            }
        }
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

        SqlFieldsQuery sql = new SqlFieldsQuery("select _key from DataTagCacheObject where SUBEQUIPMENTID = ?").setArgs(subEquipmentId);

        List<Long> tagIds = new ArrayList<>();

        try (QueryCursor<List<?>> cursor = cache.sqlQueryCache(sql)) {
            for (List<?> row : cursor) {
                tagIds.add((Long) row.get(0));
            }
        }

        /*TODO query can't fail ?
            throw new CacheElementNotFoundException("Failed to execute query with (sub)EquipmentId " + subEquipmentId + " : " +
                    "Result is null.");
                    */

        return tagIds;
    }
}

