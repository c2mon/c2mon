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

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.loading.CommandTagDAO;
import cern.c2mon.shared.common.command.CommandTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Franz Ritter
 */
@Component
@ManagedResource(objectName = "cern.c2mon:name=commandTagConfigurationManager",
    description = "Persist the configuration of command tags into the db.")
@Slf4j
public class CommandTagConfigurationManager {

  private CommandTagDAO commandTagDAO;

  private C2monCache<CommandTag> commandTagCache;


  @Autowired
  public CommandTagConfigurationManager(final CommandTagDAO commandTagDAO, final C2monCache<CommandTag> commandTagCache) {
    this.commandTagDAO = commandTagDAO;
    this.commandTagCache = commandTagCache;
  }

  @ManagedOperation(description = "Persists the current cache configurations to the DB (cache persistence). Ensures cache object runtime values & DB are synchronized.")
  public void persistAllCacheConfigurationToDatabase() {
    try {
      Set<Long> tagIdList = commandTagCache.getKeys();

      log.debug("Persisting " + tagIdList.size() + " configuration of cache object(s) to the database (CommandTag)");
      int counter = 0, overall = 0;
      for (Long id : tagIdList) {
        commandTagDAO.updateConfig(commandTagCache.get(id));
        counter++;
        overall++;
        if (counter >= tagIdList.size() * 0.1) {
          counter = 0;
          log.debug("JMX update progress: " + (int) (((overall * 1.0) / tagIdList.size()) * 100) + "%");
        }
      }
    } catch (Exception e){
      log.warn("Error occurred whilst persisting all command tag configurations.", e);
    }
  }

  @ManagedOperation(description = "Persists the current cache configurations to the DB (cache persistence). Ensures cache object runtime values & DB are synchronized.")
  public void persistAllCacheConfigurationToDatabaseParallel() {
    try {
      Set<Long> tagIdList = commandTagCache.getKeys();
      log.debug("Persisting " + tagIdList + " configuration of cache object(s) to the database (CommandTag)");
      tagIdList.parallelStream().forEach((key) -> commandTagDAO.updateConfig(commandTagCache.get(key)));
      log.debug("Persisting commandTags configuration done");

    }catch (Exception e){
      log.warn("Error occurred whilst persisting all command tag configurations.", e);
    }
  }


}
