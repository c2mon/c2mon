package cern.c2mon.server.cache.tag.query;

import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagIgniteQuery<T> implements TagQuery<T> {

    private static final Logger LOG = LoggerFactory.getLogger(TagIgniteQuery.class);

    private final IgniteCacheImpl cache;

    public TagIgniteQuery(final Ehcache cache){
        this.cache = (IgniteCacheImpl) cache;
    }


    @Override
    public List<T> findTagsByName(String name, int maxResults) {

        List<T> tagList = new ArrayList<>();

        switch(cache.getName()) {
            case "controlTagCache":
                SqlFieldsQuery controlTagSql = new SqlFieldsQuery("select _val from ControlTagCacheObject  where UPPER(NAME) = UPPER(?) LIMIT ?").setArgs(name, maxResults);

                try (QueryCursor<List<?>> cursor = cache.sqlQueryCache(controlTagSql)) {
                    for (List<?> row : cursor) {
                        tagList.add((T) row.get(0));
                    }
                }
            break;

            case "dataTagCache":
            SqlFieldsQuery dataTagSql = new SqlFieldsQuery("select _val from DataTagCacheObject  where UPPER(NAME) = UPPER(?) LIMIT ?").setArgs(name, maxResults);

            try (QueryCursor<List<?>> cursor = cache.sqlQueryCache(dataTagSql)) {
                for (List<?> row : cursor) {
                    tagList.add((T) row.get(0));
                }
            }
            break;

            case "ruleTagCache":
            SqlFieldsQuery ruleTagSql = new SqlFieldsQuery("select _val from RuleTagCacheObject  where UPPER(NAME) = UPPER(?) LIMIT ?").setArgs(name, maxResults);

            try (QueryCursor<List<?>> cursor = cache.sqlQueryCache(ruleTagSql)) {
                for (List<?> row : cursor) {
                    tagList.add((T) row.get(0));
                }
            }
                break;
        }
        LOG.debug(String.format("findTagsByName() - Got %d results for name \"%s\"", tagList.size(), name));

        return tagList;
    }

    @Override
    public List<T> findTagsByWildcard(String wildcard, int maxResults){

        String sqlWildcard = replaceWildcardSymbols(wildcard);

        List<T> tagList = new ArrayList<>();

        switch(cache.getName()){
            case "dataTagCache":
                SqlFieldsQuery dataTagSql = new SqlFieldsQuery("select _val from DataTagCacheObject  where UPPER(NAME) LIKE UPPER(?) LIMIT ?").setArgs(sqlWildcard, maxResults);

                try (QueryCursor<List<?>> cursor = cache.sqlQueryCache(dataTagSql)) {
                    for (List<?> row : cursor) {
                        tagList.add((T) row.get(0));
                    }
                }
            break;

            case "ruleTagCache":
                SqlFieldsQuery ruleTagSql = new SqlFieldsQuery("select _val from RuleTagCacheObject  where UPPER(NAME) LIKE UPPER(?) LIMIT ?").setArgs(sqlWildcard, maxResults);

                try (QueryCursor<List<?>> cursor = cache.sqlQueryCache(ruleTagSql)) {
                    for (List<?> row : cursor) {
                        tagList.add((T) row.get(0));
                    }
                }
            break;

            case "controlTagCache":
                SqlFieldsQuery controlTagSql = new SqlFieldsQuery("select _val from ControlTagCacheObject  where UPPER(NAME) LIKE UPPER(?) LIMIT ?").setArgs(sqlWildcard, maxResults);

                try (QueryCursor<List<?>> cursor = cache.sqlQueryCache(controlTagSql)) {
                    for (List<?> row : cursor) {
                        tagList.add((T) row.get(0));
                    }
                }
            break;
        }

        LOG.debug(String.format("findByNameWildcard() - Got %d results for regex \"%s\"", tagList.size(), wildcard));

        return tagList;
    }


    /**
     * Method to replace the character '*' by '%' and '?' by '_' to work with the Java Pattern
     * @param wildcard
     * @return
     */
    private String replaceWildcardSymbols(String wildcard){
        if(wildcard.contains("*") || wildcard.contains("?")) {
            String result = wildcard.replace("*", "%").replace("?", "_");
            LOG.debug("Replaced wildcard symbols on wildcard {}. Result: {}", wildcard, result);
            return result;
        }else{
            return wildcard;
        }
    }
}

