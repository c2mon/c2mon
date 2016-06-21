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
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.loading.ControlTagLoaderDAO;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import cern.c2mon.server.cache.loading.RuleTagLoaderDAO;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
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

  private ControlTagLoaderDAO controlTagLoaderDAO;

  private RuleTagLoaderDAO ruleTagLoaderDAO;

  private DataTagCache dataTagCache;

  private ControlTagCache controlTagCache;

  private RuleTagCache ruleTagCache;

  @Autowired
  public DataTagConfigurationManager(final DataTagLoaderDAO dataTagLoaderDAO, final DataTagCache dataTagCache,
                                     ControlTagLoaderDAO controlTagLoaderDAO, ControlTagCache controlTagCache,
                                     RuleTagLoaderDAO ruleTagLoaderDAO, RuleTagCache ruleTagCache) {
    this.dataTagLoaderDAO = dataTagLoaderDAO;
    this.dataTagCache = dataTagCache;
    this.controlTagLoaderDAO = controlTagLoaderDAO;
    this.controlTagCache = controlTagCache;
    this.ruleTagLoaderDAO = ruleTagLoaderDAO;
    this.ruleTagCache = ruleTagCache;
  }

  @ManagedOperation(description = "Persists the current cache configurations to the DB (cache persistence). Ensures cache object runtime values & DB are synchronized.")
  public void persistAllCacheConfigurationToDatabase() {
    try {
      List<Long> dataTagIdList = dataTagCache.getKeys();
      List<Long> controlTagIdList = controlTagCache.getKeys();
      List<Long> ruleTagIdList = ruleTagCache.getKeys();

      int numberOfAllData = dataTagIdList.size() + controlTagIdList.size() + ruleTagIdList.size();

      log.debug("Persisting " + numberOfAllData + " configuration of cache object(s) to the database.");

      int counter = 0, overall = 0;
      for (Long id : dataTagIdList) {
        DataTag tag = dataTagCache.getCopy(id);
        log.trace("Write Tag [id: {} - minvalue: {} - maxvalue: {}]", tag.getId(), tag.getMaxValue(), tag.getMinValue());
        dataTagLoaderDAO.updateConfig(dataTagCache.getCopy(id));
        counter++;
        overall++;
        if (counter >= numberOfAllData  * 0.1) {
          counter = 0;
          log.debug("JMX update progress: " + (int) (((overall * 1.0) / numberOfAllData) * 100) + "%");
        }
      }

      for (Long id : dataTagIdList) {
        ControlTag tag = controlTagCache.getCopy(id);
        log.trace("Write Tag [id: {} - minvalue: {} - maxvalue: {}]", tag.getId(), tag.getMaxValue(), tag.getMinValue());
        controlTagLoaderDAO.updateConfig(controlTagCache.getCopy(id));
        counter++;
        overall++;
        if (counter >= numberOfAllData  * 0.1) {
          counter = 0;
          log.debug("JMX update progress: " + (int) (((overall * 1.0) / numberOfAllData) * 100) + "%");
        }
      }

      for (Long id : dataTagIdList) {
        RuleTag tag = ruleTagCache.getCopy(id);
        log.trace("Write Tag [id: {} ]", tag.getId());
        ruleTagLoaderDAO.updateConfig(ruleTagCache.getCopy(id));
        counter++;
        overall++;
        if (counter >= numberOfAllData  * 0.1) {
          counter = 0;
          log.debug("JMX update progress: " + (int) (((overall * 1.0) / numberOfAllData) * 100) + "%");
        }
      }
    } catch (Exception e) {
      log.warn("Error occurred whilst persisting all data tag configurations.", e);
    }
  }

  @ManagedOperation(description = "Persists the current cache configurations to the DB (cache persistence). Ensures cache object runtime values & DB are synchronized.")
  public void persistAllCacheConfigurationToDatabaseParallel() {
    try {
      List<Long> dataTagIdList = dataTagCache.getKeys();
      List<Long> controlTagIdList = controlTagCache.getKeys();
      List<Long> ruleTagIdList = ruleTagCache.getKeys();

      log.debug("Persisting {} dataTag, {} controlTag and {} ruleTag configuration to the database", dataTagIdList.size(), controlTagIdList.size(), ruleTagIdList.size());

      controlTagCache.getKeys().parallelStream().forEach((key) -> controlTagLoaderDAO.updateConfig(controlTagCache.getCopy(key)));
      log.debug("Persisting controlTags done");

      ruleTagCache.getKeys().parallelStream().forEach((key) -> ruleTagLoaderDAO.updateConfig(ruleTagCache.getCopy(key)));
      log.debug("Persisting ruleTags done");

      dataTagCache.getKeys().parallelStream().forEach((key) -> dataTagLoaderDAO.updateConfig(dataTagCache.getCopy(key)));
      log.debug("Persisting dataTags done");


    } catch (Exception e) {
      log.warn("Error occurred whilst persisting all data tag configurations.", e);
    }
  }


}
