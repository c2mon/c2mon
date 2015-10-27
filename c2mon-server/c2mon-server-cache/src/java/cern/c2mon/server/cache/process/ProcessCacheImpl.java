/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.cache.process;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.shared.common.ConfigurationException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Results;

/**
 * Implementation of the Process cache.
 *
 * @author Mark Brightwell
 */
@Service("processCache")
@ManagedResource(objectName = "cern.c2mon:type=cache,name=processTagCache")
public class ProcessCacheImpl extends AbstractCache<Long, Process>implements ProcessCache {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessCacheImpl.class);

  /**
   * Reference to the {@link ProcessDAO} bean.
   */
  private final ProcessDAO processDAO;

  /** Used to post configure the associated control tags */
  private final ControlTagCache controlCache;

  @Autowired
  public ProcessCacheImpl(final ClusterCache clusterCache, @Qualifier("processEhcache") final Ehcache ehcache,
      @Qualifier("processEhcacheLoader") final CacheLoader cacheLoader, @Qualifier("processCacheLoader") final C2monCacheLoader c2monCacheLoader,
      @Qualifier("processDAO") final SimpleCacheLoaderDAO<Process> cacheLoaderDAO, @Qualifier("controlTagCache") final ControlTagCache controlCache) {

    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);
    this.processDAO = (ProcessDAO) cacheLoaderDAO;
    this.controlCache = controlCache;
  }

  /**
   * Run on bean initialization. Sets the Ehcache field to the appropriate cache
   * and calls the preload routine if necessary.
   */
  @PostConstruct
  public void init() {
    LOGGER.info("Initializing Process cache...");
    commonInit();
    doPostConfigurationOfProcessControlTags();
    LOGGER.info("... Process cache initialization complete.");
  }

  /**
   * Ensures that the Alive-, Status- and CommFault Tags have the
   * Process id set.
   */
  private void doPostConfigurationOfProcessControlTags() {
    for (Long key : getKeys()) {
      doPostDbLoading(get(key));
    }
  }

  @Override
  public Process getCopy(final String name) {
    Long key = getProcessId(name);
    return getCopy(key);
  }

  /**
   * Ensures that the Alive-, Status- and CommFault Tags have appropriately the
   * Process id set.
   * 
   * @param process The equipment to which the control tags are assigned
   */
  @Override
  protected void doPostDbLoading(final Process process) {
    Long processId = process.getId();

    Long aliveTagId = process.getAliveTagId();
    if (aliveTagId != null) {
      
      ControlTag aliveTagCopy = controlCache.getCopy(aliveTagId);
      if (aliveTagCopy != null) {
        setProcessId((ControlTagCacheObject) aliveTagCopy, processId);
      }
      else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            String.format("No Alive tag (%d) found for Process %s (#%d).", aliveTagId, process.getName(), process.getId()));
      }
      
    }
    else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
          String.format("No Alive tag for Process %s (#%d) defined.", process.getName(), process.getId()));
    }

    Long statusTagId = process.getStateTagId();
    if (statusTagId != null) {
      
      ControlTag statusTagCopy = controlCache.getCopy(statusTagId);
      if (statusTagCopy != null) {
        setProcessId((ControlTagCacheObject) statusTagCopy, processId);
      }
      else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            String.format("No Status tag (%d) found for Process %s (#%d).", statusTagId, process.getName(), process.getId()));
      }
      
    }
    else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
          String.format("No Status tag for Process %s (#%d) defined.", process.getName(), process.getId()));
    }
  }

  @Override
  protected C2monCacheName getCacheName() {
    return C2monCacheName.PROCESS;
  }

  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }

  @Override
  public Long getProcessId(final String name) {
    Long processKey = null;
    Results results = null;

    if (name == null || name.equalsIgnoreCase("")) {
      throw new IllegalArgumentException("Attempting to retrieve a Process from the cache with a NULL or empty name parameter.");
    }

    try {
      Attribute<String> processName = getCache().getSearchAttribute("processName");
      // By limiting the query result list to 1 it is up to the administrator to
      // make
      // sure that the process name is unique. Otherwise this will result in an
      // unpredictable behaviour.
      Query query = getCache().createQuery();
      results = query.includeKeys().addCriteria(processName.eq(name)).maxResults(1).execute();

      // Find the number of results -- the number of hits.
      int size = results.size();
      if (size == 0) {
        throw new CacheElementNotFoundException("Failed to find a process with name " + name + " in the cache.");
      }

      processKey = (Long) results.all().get(0).getKey();
    }
    finally {
      if (results != null) {
        // Discard the results when done to free up cache resources.
        results.discard();
      }
    }

    return processKey;
  }

  @Override
  public Integer getNumTags(Long processId) {
    return processDAO.getNumTags(processId);
  }

  @Override
  public Integer getNumInvalidTags(Long processId) {
    return processDAO.getNumInvalidTags(processId);
  }

  private void setProcessId(ControlTagCacheObject copy, Long processId) {
    String logMsg = String.format("Adding process id #%s to control tag #%s", processId, copy.getId());
    LOGGER.trace(logMsg);
    copy.setProcessId(processId);
    controlCache.putQuiet(copy);
  }
}
