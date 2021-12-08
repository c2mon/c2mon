package cern.c2mon.server.cache.command.query;

import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;
import cern.c2mon.shared.common.command.CommandTag;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandTagInMemoryQuery implements CommandTagQuery{

    private static final Logger LOG = LoggerFactory.getLogger(CommandTagIgniteQuery.class);

    private final InMemoryCache cache;

    public CommandTagInMemoryQuery(final Ehcache cache){
        this.cache = (InMemoryCache) cache;
    }

    @Override
    public Long findCommandTagIdByName(String name){
        Optional<Long> commandTagKey;

        Predicate<CommandTag> filter = tag -> tag.getName().equalsIgnoreCase(name);

        Stream<CommandTag> test = cache.getCache().values().stream();
        test.forEach(t -> System.out.println(name + " " + t.getName()));

        try(Stream<CommandTag> stream = cache.getCache().values().stream()){

            commandTagKey = stream.filter(filter).map(c -> c.getId()).findFirst();

        }

        if (!commandTagKey.isPresent()) {
            LOG.info("Failed to find a command tag with name " + name + " in the cache.");
            return null;
        }

        return commandTagKey.get();
    }
}
