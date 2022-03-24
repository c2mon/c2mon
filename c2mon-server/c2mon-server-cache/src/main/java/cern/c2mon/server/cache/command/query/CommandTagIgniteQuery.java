package cern.c2mon.server.cache.command.query;

import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandTagIgniteQuery implements CommandTagQuery {

    private static final Logger LOG = LoggerFactory.getLogger(CommandTagIgniteQuery.class);

    private final IgniteCacheImpl cache;

    public CommandTagIgniteQuery(final Ehcache cache){
        this.cache = (IgniteCacheImpl) cache;
    }

    @Override
    public Long findCommandTagIdByName(String name) {

        SqlFieldsQuery sql = new SqlFieldsQuery("select _key from CommandTagCacheObject where UPPER(NAME) = UPPER(?)").setArgs(name);

        List<Long> result = new ArrayList<>();

        try (QueryCursor<List<?>> cursor = cache.sqlQueryCache(sql)) {
            for (List<?> row : cursor) {
                result.add((Long) row.get(0));
            }
        }

        if(result.isEmpty()){
            LOG.info("Failed to find a command tag with name " + name + " in the cache.");
            return null;
        }

        return result.get(0);
    }
}
