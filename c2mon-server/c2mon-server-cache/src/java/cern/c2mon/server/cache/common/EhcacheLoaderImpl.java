/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cache.common;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.shared.common.Cacheable;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.loader.CacheLoader;

/**
 * Implementation of Ehcache CacheLoader interface. Each cache registers
 * one of these, although it is only used when cache eviction is enabled,
 * to load cache objects into the cache from the DB.
 * 
 * @author Mark Brightwell
 *
 * @param <T> the cache object type
 */
public class EhcacheLoaderImpl<T extends Cacheable> implements CacheLoader {
  
  /**
   * Private logger.
   */
  private static final Logger LOGGER = Logger.getLogger(EhcacheLoaderImpl.class);
    
  /**
   * Reference to the cache that needs loading
   * (set in the constructor).
   */
  private Ehcache cache;
  
  /**
   * Reference to the loader DAO for this cache
   * (set in constructor).
   */
  private SimpleCacheLoaderDAO<T> cacheLoaderDAO;
    
  /**
   * Constructor.
   * @param cache the Ehcache
   * @param cacheLoaderDAO the Ehcache loader mechanism
   */
  public EhcacheLoaderImpl(final Ehcache cache, final SimpleCacheLoaderDAO<T> cacheLoaderDAO) {
    super();
    this.cache = cache;
    this.cacheLoaderDAO = cacheLoaderDAO;
  }


  /**
   * Look for the object in the DB with the given Id.
   * @param id the Id of the object
   * @return the cache object retrieved from the DB
   */
  private Cacheable fetchFromDB(final Object id) {
    return cacheLoaderDAO.getItem(id);
  }
  
  
  public CacheLoader clone(Ehcache arg0) throws CloneNotSupportedException {
    // TODO Auto-generated method stub
    return null;
  }

  public void dispose() throws CacheException {
    // TODO Auto-generated method stub
  }


  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }


  public Status getStatus() {
    // TODO Auto-generated method stub
    return null;
  }


  public void init() {
    // TODO Auto-generated method stub
  }
  
  /**
   * Loads the object with the provided key into the cache (called for instance
   * by getWithLoader if element not found in the cache.
   * 
   * <p>If this loader is in preloading mode (as indicated by the <code>preload</code>
   * field, then this method looks for the object in the preload Map. If not
   * in preload mode, it will look for the object in the database directly. If
   * it is found in either of these, it will be loaded into the cache and 
   * returned. If not found in either, nothing is added to the cache and null
   * is returned.
   * 
   * <p>Notice that the returned null is never returned by a direct query to a
   * {@link C2monCache}, in which case a {@link CacheElementNotFoundException} is
   * thrown.
   */
  public Object load(Object key) throws CacheException {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Fetching cache object with Id " + key + " from database.");
    }
    Object result = fetchFromDB(key);      
           
    //put in cache if found something
    if (result != null) {
      cache.putQuiet(new Element(key, result));
    } 
    
    return result;
  }

  public Object load(Object arg0, Object arg1) {
    System.out.println("load(Object,Object): should not be here as not implemented!");
    return null;
  }

  public Map loadAll(Collection arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public Map loadAll(Collection arg0, Object arg1) {
    // TODO Auto-generated method stub
    return null;
  }

}
