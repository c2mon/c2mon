package cern.c2mon.server.cache.cluster;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.common.DefaultCacheImpl;

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
    try {
      // empty this cache in single server mode!
      if (!skipCachePreloading && cacheMode.equalsIgnoreCase("single")) {
        cache.removeAll();
      }

      if (cache.get(clusterInitializedKey) == null) {
        this.put(clusterInitializedKey, Boolean.TRUE);
      }
    } finally {
      cache.releaseWriteLockOnKey(clusterInitializedKey);
    }
  }
}
