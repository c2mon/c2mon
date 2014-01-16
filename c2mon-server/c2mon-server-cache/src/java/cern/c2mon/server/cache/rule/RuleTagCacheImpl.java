package cern.c2mon.server.cache.rule;

import java.util.HashSet;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.cache.tag.AbstractTagCache;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;

/**
 * Implementation of the Rule cache.
 * 
 * @author Mark Brightwell
 *
 */
@Service("ruleTagCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=ruleTagCache")
public class RuleTagCacheImpl extends AbstractTagCache<RuleTag> implements RuleTagCache {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(RuleTagCacheImpl.class);
  
  /**
   * DataTagCache for rule parent id loading.
   */
  private final DataTagCache dataTagCache;

  @Autowired
  public RuleTagCacheImpl(@Qualifier("clusterCache") final ClusterCache clusterCache, 
                          @Qualifier("ruleTagEhcache") final Ehcache ehcache,
                          @Qualifier("ruleTagEhcacheLoader") final CacheLoader cacheLoader, 
                          @Qualifier("ruleTagCacheLoader") final C2monCacheLoader c2monCacheLoader,
                          @Qualifier("ruleTagLoaderDAO") final SimpleCacheLoaderDAO<RuleTag> cacheLoaderDAO,
                          @Qualifier("dataTagCache") final DataTagCache dataTagCache) {    
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);
    this.dataTagCache = dataTagCache;
  }

  @PostConstruct
  public void init() {    
    LOGGER.info("Initializing RuleTag cache...");
    commonInit(); 
    LOGGER.info("... RuleTag cache initialization complete.");
  }

  @Override
  protected void doPostDbLoading(RuleTag ruleTag) {
    LOGGER.trace("doPostDbLoading() - Post processing RuleTag " + ruleTag.getId() + " ...");
    // sets for this ruleTag
    HashSet<Long> processIds = new HashSet<Long>();
    HashSet<Long> equipmentIds = new HashSet<Long>();
    for (Long tagKey : ruleTag.getRuleInputTagIds()) {
      if (dataTagCache.hasKey(tagKey)) {
        DataTag dataTag = dataTagCache.getCopy(tagKey);
        processIds.add(dataTag.getProcessId());
        equipmentIds.add(dataTag.getEquipmentId());
      } else if (hasKey(tagKey)) {
        acquireWriteLockOnKey(tagKey);        
        try {
          RuleTag childRuleTag = getCopy(tagKey);        
          //if not empty, already processed; if empty, needs processing
          if (childRuleTag.getProcessIds().isEmpty()) {
            doPostDbLoading(childRuleTag);
            // commit changes to the cache
            putQuiet(childRuleTag);
          }
          processIds.addAll(childRuleTag.getProcessIds());
          equipmentIds.addAll(childRuleTag.getEquipmentIds());
        } finally {
          releaseWriteLockOnKey(tagKey);
        }          
      } else {
        throw new CacheElementNotFoundException("Unable to set rule parent process & equipment ids for rule " + ruleTag.getId()
                  + ": unable to locate tag " + tagKey + " in either RuleTag or DataTag cache (Control tags not supported in rules)");
        }       
    }
    LOGGER.trace("doPostDbLoading() - Setting parent ids for rule " + ruleTag.getId() + "; process ids: " + processIds + "; equipment ids: " + equipmentIds);
    ruleTag.setProcessIds(processIds);
    ruleTag.setEquipmentIds(equipmentIds);
    LOGGER.trace("doPostDbLoading() - ... RuleTag " + ruleTag.getId() + " done!");
  }

  @Override
  protected C2monCacheName getCacheName() {
    return C2monCacheName.RULETAG;
  }
  
  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }

  

}
