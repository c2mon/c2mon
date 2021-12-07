package cern.c2mon.server.cache.tag.query;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

import java.util.List;
import java.util.function.Predicate;
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
    public List<T> findTagsByWildcard(String wildcard, int maxResults){
        List<T> resultList;

        Predicate<T> filter = tag -> ((Tag) tag).getName().matches(wildcard);

        try(Stream<T> stream = cache.getCache().values().stream()){

            resultList = stream.filter(filter).map(t -> (T) t).limit(maxResults).collect(Collectors.toList());

            LOG.debug(String.format("findByNameWildcard() - Got %d results for regex \"%s\"", resultList.size(), wildcard));
        }

        return resultList;
    }
}
