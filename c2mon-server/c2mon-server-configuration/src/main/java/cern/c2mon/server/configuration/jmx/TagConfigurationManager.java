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
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.cache.loading.ControlTagLoaderDAO;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import cern.c2mon.server.cache.loading.RuleTagLoaderDAO;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Franz Ritter
 */
@Component
@ManagedResource(objectName = "cern.c2mon:name=tagConfigurationManager",
  description = "Persist the configuration of rule-, control- and data-tags into the db.")
@Slf4j
public class TagConfigurationManager {

  private DataTagLoaderDAO dataTagLoaderDAO;

  private ControlTagLoaderDAO controlTagLoaderDAO;

  private RuleTagLoaderDAO ruleTagLoaderDAO;

  private C2monCache<DataTag> dataTagCache;

  private C2monCache<RuleTag> ruleTagCache;

  @Autowired
  public TagConfigurationManager(final DataTagLoaderDAO dataTagLoaderDAO, final C2monCache<DataTag> dataTagCache,
                                 ControlTagLoaderDAO controlTagLoaderDAO,
                                 RuleTagLoaderDAO ruleTagLoaderDAO, C2monCache<RuleTag> ruleTagCache) {
    this.dataTagLoaderDAO = dataTagLoaderDAO;
    this.dataTagCache = dataTagCache;
    this.controlTagLoaderDAO = controlTagLoaderDAO;
    this.ruleTagLoaderDAO = ruleTagLoaderDAO;
    this.ruleTagCache = ruleTagCache;
  }

  @ManagedOperation(description = "Persists the current cache configurations to the DB (cache persistence). Ensures cache object runtime values & DB are synchronized.")
  public void persistAllCacheConfigurationToDatabase() {
    try {
      Set<Long> dataTagIdList = dataTagCache.getKeys();
//      List<Long> controlTagIdList = controlTagCache.getKeys();
      Set<Long> ruleTagIdList = ruleTagCache.getKeys();

      int numberOfAllData = dataTagIdList.size() + /*controlTagIdList.size()*/ + ruleTagIdList.size();

      log.debug("Persisting " + numberOfAllData + " configuration of cache object(s) to the database.");

      AtomicInteger counter = new AtomicInteger(0);
      AtomicInteger overall = new AtomicInteger(0);

      for (Long id : dataTagIdList) {
        DataTag tag = dataTagCache.get(id);
        log.trace("Write Tag [id: {} - minvalue: {} - maxvalue: {}]", tag.getId(), tag.getMaxValue(), tag.getMinValue());
        saveConfigToDatabase(tag, dataTagLoaderDAO, counter, overall, numberOfAllData);
      }

//      for (Long id : controlTagIdList) { TODO (Alex) Also, parallelizable?
//        ControlTag tag = controlTagCache.getCopy(id);
//        log.trace("Write Tag [id: {} - minvalue: {} - maxvalue: {}]", tag.getId(), tag.getMaxValue(), tag.getMinValue());
//        saveConfigToDatabase(tag, controlTagLoaderDAO, counter, overall, numberOfAllData);
//      }

      for (Long id : ruleTagIdList) {
        RuleTag tag = ruleTagCache.get(id);
        log.trace("Write Tag [id: {} ]", tag.getId());
        saveConfigToDatabase(tag, ruleTagLoaderDAO, counter, overall, numberOfAllData);
      }
    } catch (Exception e) {
      log.warn("Error occurred whilst persisting all data tag configurations.", e);
    }
  }

  private <T extends Tag> void saveConfigToDatabase(T tag, ConfigurableDAO<T> dao, AtomicInteger counter,
                                                    AtomicInteger overall, int numberOfAllData) {
    dao.updateConfig(tag);
    counter.incrementAndGet();
    overall.incrementAndGet();
    if (counter.get() >= numberOfAllData * 0.1) {
      counter.set(0);
      log.debug("JMX update progress: " + (int) (((overall.get() * 1.0) / numberOfAllData) * 100) + "%");
    }
  }

  @ManagedOperation(description = "Persists the current cache configurations to the DB (cache persistence). Ensures cache object runtime values & DB are synchronized.")
  public void persistAllCacheConfigurationToDatabaseParallel() {
    try {
      Set<Long> dataTagIdList = dataTagCache.getKeys();
//      List<Long> controlTagIdList = controlTagCache.getKeys();
      Set<Long> ruleTagIdList = ruleTagCache.getKeys();

      log.debug("Persisting of {} dataTags, controlTags and {} ruleTags configuration to the database", dataTagIdList.size(), /*controlTagIdList.size(),*/ ruleTagIdList.size());

//      controlTagCache.getKeys().parallelStream().forEach((key) -> controlTagLoaderDAO.updateConfig(controlTagCache.getCopy(key)));
      log.debug("Persisting controlTags configuration done");

      ruleTagCache.getKeys().parallelStream().forEach((key) -> ruleTagLoaderDAO.updateConfig(ruleTagCache.get(key)));
      log.debug("Persisting of {} ruleTags configuration done", ruleTagIdList.size());

      dataTagCache.getKeys().parallelStream().forEach((key) -> dataTagLoaderDAO.updateConfig(dataTagCache.get(key)));
      log.debug("Persisting of {} dataTags configuration done", dataTagIdList.size());


    } catch (Exception e) {
      log.warn("Error occurred whilst persisting all data tag configurations.", e);
    }
  }


}
