package cern.c2mon.server.cache;

import java.io.Serializable;


/**
 * This cache is used for custom synchronization actions across the cluster, and is available
 * for use by any module.
 * 
 * <p>For instance, it can be used to share a single timestamp between server nodes, or for sharing
 * a distributed lock. 
 * 
 * <p>In order to allow this cache to be used across modules, the following naming convention should
 * be followed: cache keys for a given module must follow the pattern:
 * 
 * <p><code> module-top-level-package-name.property-name </code>
 * 
 * <p>Note that for generating a shared lock, it is sufficient to enter a custom key and use the cache locking
 * methods. 
 * 
 * @author Mark Brightwell
 *
 */
public interface ClusterCache extends C2monCache<String, Serializable> {


}
