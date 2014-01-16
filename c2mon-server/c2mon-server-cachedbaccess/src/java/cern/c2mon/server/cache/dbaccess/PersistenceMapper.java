package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.shared.common.Cacheable;

/**
 * Interface that must be implemented by mappers that will be used for
 * automatically persisting cache updates to the database (corresponds 
 * to all tag caches in TIM).
 * 
 * !not used at the moment as use udpateCacheable(cacheable, mapper); may be able to remove this
 *  if never need this generic method on the simple mapper
 * 
 * @author mbrightw
 *
 */
public interface PersistenceMapper<T extends Cacheable> {

  void updateCacheable(T cacheable);
  
}
