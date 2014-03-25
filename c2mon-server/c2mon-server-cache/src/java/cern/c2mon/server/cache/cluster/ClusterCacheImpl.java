package cern.c2mon.server.cache.cluster;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.CommandTagCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.common.DefaultCacheImpl;
import cern.c2mon.server.cache.rule.RuleTagPostLoaderProcessor;

@Service("clusterCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=clusterCache")
public class ClusterCacheImpl extends DefaultCacheImpl<String, Serializable> implements ClusterCache {
  
  private final String clusterInitializedKey = "c2mon.cache.cluster.initialized";
  
  
  @Autowired
  public ClusterCacheImpl(@Qualifier("clusterEhcache") final Ehcache ehcache) {
    super(ehcache);
  }
  
  /**
   * Initializes C2MON server core distributed parameters.
   */
  @PostConstruct
  public void init() {
    //lock cluster 
    cache.acquireWriteLockOnKey(clusterInitializedKey);
    //empty this cache in single server mode!
    if (!skipCachePreloading && cacheMode.equalsIgnoreCase("single")) {
      cache.removeAll();
    }
    try {
      //if cluster cache is not initialized (i.e. distributed cache is not running), then set initial values
      if (cache.get(clusterInitializedKey) == null) {
        //only run this once on clean cluster startup
        this.put(clusterInitializedKey, Boolean.TRUE);
        
        //initialize cache loading flags
        this.put(AlarmCache.cacheInitializedKey, Boolean.FALSE);
        this.put(DataTagCache.cacheInitializedKey, Boolean.FALSE);
        this.put(CommandTagCache.cacheInitializedKey, Boolean.FALSE);
        this.put(RuleTagCache.cacheInitializedKey, Boolean.FALSE);
        this.put(RuleTagPostLoaderProcessor.ruleCachePostProcessedKey, Boolean.FALSE);
        this.put(ControlTagCache.cacheInitializedKey, Boolean.FALSE);
        this.put(ProcessCache.cacheInitializedKey, Boolean.FALSE);
        this.put(EquipmentCache.cacheInitializedKey, Boolean.FALSE);
        this.put(SubEquipmentCache.cacheInitializedKey, Boolean.FALSE);
        this.put(CommFaultTagCache.cacheInitializedKey, Boolean.FALSE);
        this.put(AliveTimerCache.cacheInitializedKey, Boolean.FALSE);
        
        
      }
    } finally {
      cache.releaseWriteLockOnKey(clusterInitializedKey);
    } 
  }
}
