package cern.c2mon.server.cache.tag.query;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import javax.cache.Cache;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteClosure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagIgniteQuery<T> implements TagQuery<T> {

    private static final Logger LOG = LoggerFactory.getLogger(TagInMemoryQuery.class);

    private final IgniteCacheImpl cache;

    public TagIgniteQuery(final Ehcache cache){
        this.cache = (IgniteCacheImpl) cache;
    }


    @Override
    public List<T> findTagsByName(String name, int maxResults) {

        IgniteBiPredicate<Long, Tag> predicate = (id, tag) -> ((Tag) tag).getName().equalsIgnoreCase(name);

        List<Tag> tagList = cache.getCache().query(new ScanQuery<>(
                        predicate),
                (IgniteClosure<Cache.Entry<Long, Tag>, Tag>) Cache.Entry::getValue).getAll();

        List<T> resultList;

        try(Stream<Tag> stream = tagList.stream()){

            resultList = stream.map(t -> (T) t).limit(maxResults).collect(Collectors.toList());

            LOG.debug(String.format("findTagsByName() - Got %d results for name \"%s\"", resultList.size(), name));
        }

        return resultList;
    }

    @Override
    public List<T> findTagsByWildcard(String wildcard, int maxResults){

        Pattern pattern = Pattern.compile(replaceWildcardSymbols(wildcard), Pattern.CASE_INSENSITIVE);
        IgniteBiPredicate<Long, Tag> predicate = (id, tag) -> pattern.matcher(tag.getName()).matches();

        List<Tag> tagList = cache.getCache().query(new ScanQuery<>(
                predicate),
                (IgniteClosure<Cache.Entry<Long, Tag>, Tag>) Cache.Entry::getValue).getAll();

        List<T> resultList;

        try(Stream<Tag> stream = tagList.stream()){

            resultList = stream.map(t -> (T) t).limit(maxResults).collect(Collectors.toList());

            LOG.debug(String.format("findByNameWildcard() - Got %d results for regex \"%s\"", resultList.size(), wildcard));
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

