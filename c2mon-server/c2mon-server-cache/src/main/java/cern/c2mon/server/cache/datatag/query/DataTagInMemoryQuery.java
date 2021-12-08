package cern.c2mon.server.cache.datatag.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataTagInMemoryQuery implements DataTagQuery {

    private final InMemoryCache<Long, DataTag>  cache;

    public DataTagInMemoryQuery(final Ehcache<Long, DataTag>  cache){
        this.cache = (InMemoryCache) cache;
    }

    @Override
    public List<Long> findDataTagIdsByEquipmentId(Long equipmentId) throws CacheElementNotFoundException{

        if (equipmentId == null) {
            throw new IllegalArgumentException("Attempting to retrieve a List of DataTag ids from the cache with a NULL " +
                    "parameter.");
        }

        List<Long> tagIds;

        Predicate<DataTag> filter = dataTag -> Objects.equals(dataTag.getEquipmentId(), equipmentId);


        try(Stream<DataTag> stream = cache.getCache().values().stream()){

            tagIds = stream.filter(filter).map(a -> a.getId()).collect(Collectors.toList());

            /*TODO query can't fail ?
                throw new CacheElementNotFoundException("Failed to execute query with EquipmentId " + equipmentId + " : " +
                        "Result is null.");
            */
        }
        return tagIds;
    }

    @Override
    public List<Long> findDataTagIdsBySubEquipmentId(Long subEquipmentId) throws CacheElementNotFoundException{

        if (subEquipmentId == null) {
            throw new IllegalArgumentException("Attempting to retrieve a List of DataTag ids from the cache with a NULL " +
                    "parameter.");
        }

        List<Long> tagIds;

        Predicate<DataTag> filter = dataTag -> Objects.equals(dataTag.getSubEquipmentId(), subEquipmentId);

        try(Stream<DataTag> stream = cache.getCache().values().stream()){

            tagIds = stream.filter(filter).map(a -> a.getId()).collect(Collectors.toList());

           /*TODO query can't fail ?
                throw new CacheElementNotFoundException("Failed to execute query with (sub)EquipmentId " + subEquipmentId + " : " +
                        "Result is null.");
            */
        }
        return tagIds;
    }
}
