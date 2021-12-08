package cern.c2mon.server.cache.command.query;

import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;
import cern.c2mon.shared.common.command.CommandTag;

import javax.cache.Cache;
import java.util.List;

import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteClosure;
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
        IgniteBiPredicate<Long, CommandTag> predicate = (id, tag) -> tag.getName().equalsIgnoreCase(name);

        List<Long> result = cache.getCache().query(new ScanQuery<>(predicate),
                (IgniteClosure<Cache.Entry<Long, CommandTag>, Long>) Cache.Entry::getKey).getAll();

        if(result.isEmpty()){
            LOG.info("Failed to find a command tag with name " + name + " in the cache.");
            return null;
        }

        return result.get(0);
    }
}
