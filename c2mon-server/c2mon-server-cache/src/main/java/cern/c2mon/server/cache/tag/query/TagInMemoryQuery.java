package cern.c2mon.server.cache.tag.query;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TagInMemoryQuery<T> implements TagQuery<T> {

    private static final Logger LOG = LoggerFactory.getLogger(TagInMemoryQuery.class);

    private final InMemoryCache<Long, T> cache;

    public TagInMemoryQuery(final Ehcache cache){
        this.cache = (InMemoryCache) cache;
    }

    @Override
    public List<T> findTagsByName(String name, int maxResults) {
        List<T> resultList;

        Predicate<T> filter = tag -> ((Tag) tag).getName().equalsIgnoreCase(name);

        try(Stream<T> stream = cache.getCache().values().stream()){

            resultList = stream.filter(filter).map(t -> (T) t).limit(maxResults).collect(Collectors.toList());

            LOG.debug(String.format("findTagsByName() - Got %d results for name \"%s\"", resultList.size(), name));
        }

        return resultList;
    }

    @Override
    public List<T> findTagsByWildcard(String wildcard, int maxResults){
        List<T> resultList;

        Pattern pattern = Pattern.compile(replaceWildcardSymbols(wildcard), Pattern.CASE_INSENSITIVE);
        Predicate<T> filter = tag -> pattern.matcher(((Tag) tag).getName()).matches();

        try(Stream<T> stream = cache.getCache().values().stream()){

            resultList = stream.filter(filter).map(t -> (T) t).limit(maxResults).collect(Collectors.toList());

            LOG.debug(String.format("findByNameWildcard() - Got %d results for regex \"%s\"", resultList.size(), wildcard));
        }

        return resultList;
    }

    @Override
    public List<T> findTagsByProcessId(Long processId, int maxResults) {
        List<T> resultList;
        Predicate<T> filter = tag -> ((Tag) tag).getProcessIds().contains(processId);

        try(Stream<T> stream = cache.getCache().values().stream()){

            resultList = stream.filter(filter).map(t -> (T) t).limit(maxResults).collect(Collectors.toList());

            LOG.debug(String.format("findTagsByProcessId() - Got %d results for process id \"%s\"", resultList.size(), processId));
        }

        return resultList;
    }

    @Override
    public List<T> findTagsByEquipmentId(Long equipmentId, int maxResults) {
        List<T> resultList;

        Predicate<T> filter = tag -> ((Tag) tag).getEquipmentIds().contains(equipmentId);

        try(Stream<T> stream = cache.getCache().values().stream()){

            resultList = stream.filter(filter).map(t -> (T) t).limit(maxResults).collect(Collectors.toList());

            LOG.debug(String.format("findTagsByProcessId() - Got %d results for equipment id \"%s\"", resultList.size(), equipmentId));
        }

        return resultList;
    }

    @Override
    public List<T> findTagsBySubEquipmentId(Long subEquipmentId, int maxResults) {
        List<T> resultList;

        Predicate<T> filter = tag -> ((Tag) tag).getSubEquipmentIds().contains(subEquipmentId);

        try(Stream<T> stream = cache.getCache().values().stream()){

            resultList = stream.filter(filter).map(t -> (T) t).limit(maxResults).collect(Collectors.toList());

            LOG.debug(String.format("findTagsByProcessId() - Got %d results for sub equipment id \"%s\"", resultList.size(), subEquipmentId));
        }

        return resultList;
    }

    /**
     * Method to replace the character '*' by '.*' and '?' by '.?' to work with the Java Pattern
     * @param wildcard
     * @return
     */
    private String replaceWildcardSymbols(String wildcard){
        if(wildcard.contains("*") || wildcard.contains("?")) {
            String result = wildcard.replace("*", ".*").replace("?", ".?");
            LOG.debug("Replaced wildcard symbols on wildcard {}. Result: {}", wildcard, result);
            return result;
        }else{
            return wildcard;
        }
    }
}
