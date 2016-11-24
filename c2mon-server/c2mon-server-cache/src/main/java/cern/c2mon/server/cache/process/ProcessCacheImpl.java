/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.cache.process;

import javax.annotation.PostConstruct;

import cern.c2mon.server.cache.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
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
@Slf4j
@Service("processCache")
@ManagedResource(objectName = "cern.c2mon:type=cache,name=processTagCache")
public class ProcessCacheImpl extends AbstractCache<Long, Process>implements ProcessCache {

  /**
   * Reference to the {@link ProcessDAO} bean.
   */
  private final ProcessDAO processDAO;

  /** Used to post configure the associated control tags */
  private final ControlTagCache controlCache;

  @Autowired
  public ProcessCacheImpl(final ClusterCache clusterCache,
                          @Qualifier("processEhcache") final Ehcache ehcache,
                          @Qualifier("processEhcacheLoader") final CacheLoader cacheLoader,
                          @Qualifier("processCacheLoader") final C2monCacheLoader c2monCacheLoader,
                          @Qualifier("processDAO") final SimpleCacheLoaderDAO<Process> cacheLoaderDAO,
                          @Qualifier("controlTagCache") final ControlTagCache controlCache,
                          final CacheProperties properties) {

    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO, properties);
    this.processDAO = (ProcessDAO) cacheLoaderDAO;
    this.controlCache = controlCache;
  }

  /**
   * Run on bean initialization. Sets the Ehcache field to the appropriate cache
   * and calls the preload routine if necessary.
   */
  @PostConstruct
  public void init() {
    log.debug("Initializing Process cache...");
    commonInit();
    doPostConfigurationOfProcessControlTags();
    log.info("Process cache initialization complete");
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
    log.trace(logMsg);
    copy.setProcessId(processId);
    controlCache.putQuiet(copy);
  }
}
