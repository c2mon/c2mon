package cern.c2mon.server.cache.tag;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.config.AbstractSimpleCacheConfig;
import cern.c2mon.server.cache.loader.CacheLoaderDAO;
import cern.c2mon.server.common.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Alexandros Papageorgiou Koufidis
 *
 * This is declared here only to maintain convention with all other cache declarations
 *
 * It does nothing
 * @see TagCacheFacade
 */
public class TagCacheConfig {

}
