/**
 * The cluster cache contains parameters that need sharing across a server cluster.
 * The keys can be used to synchronize cluster actions. Timestamps or other shared
 * values can also be stored.
 * 
 * <p>Make sure to follow the key naming convention, to allow re-use of this cache
 * by all modules (including optional ones). Keys should follow the pattern
 * 
 * <code> module-top-level-package-name.property-name </code>
 * 
 * This package contains the implementation of this cache.
 * 
 * @author Mark Brightwell
 *
 */
package cern.c2mon.server.cache.cluster;