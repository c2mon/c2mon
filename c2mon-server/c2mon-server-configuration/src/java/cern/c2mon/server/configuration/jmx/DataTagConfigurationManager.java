/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.server.configuration.jmx;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Franz Ritter
 */
@Component
@ManagedResource(objectName = "cern.c2mon:name=dataTagConfigurationManager")
@Slf4j
public class DataTagConfigurationManager {

  private DataTagLoaderDAO dataTagLoaderDAO;

  private DataTagCache dataTagCache;

  private ClusterCache clusterCache;

  private String cachePersistenceLock = "c2mon.cachepersistence.cachePersistenceLock";


  @Autowired
  public DataTagConfigurationManager(final DataTagLoaderDAO dataTagLoaderDAO, final DataTagCache dataTagCache, ClusterCache clusterCache) {
    this.dataTagLoaderDAO = dataTagLoaderDAO;
    this.dataTagCache = dataTagCache;
    this.clusterCache = clusterCache;
  }

  @ManagedOperation(description = "Persists the current cache configurations to the DB (cache persistence). Ensures cache object runtime values & DB are synchronized.")
  public void persistAllCacheConfigurationToDatabase() {
    clusterCache.acquireWriteLockOnKey(cachePersistenceLock);
    try {
      List<Long> tagIdList = dataTagCache.getKeys();

      log.debug("Persisting " + tagIdList.size() + " configuration of cache object(s) to the database (DataTag)");

      int counter=0, overall=0;
      for(Long id : tagIdList){
        dataTagLoaderDAO.updateConfig(dataTagCache.get(id));
        counter++;
        overall++;
        if(tagIdList.size()/10 >= counter){
          counter =0;
          log.debug("JMX update progress:"+tagIdList.size()/overall);
        }
      }

//      TODO: use the stream
//      tagIdList.stream().forEach((key) -> dataTagLoaderDAO.updateConfig(dataTagCache.get(key)));
      log.debug("Done with Persisting the DataTag Configuration");

    } finally {
      clusterCache.releaseWriteLockOnKey(cachePersistenceLock);
    }

  }


}
