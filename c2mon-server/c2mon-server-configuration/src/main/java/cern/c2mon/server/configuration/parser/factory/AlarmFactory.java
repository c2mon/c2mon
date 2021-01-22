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

package cern.c2mon.server.configuration.parser.factory;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Franz Ritter
 */
@Named
@Singleton
class AlarmFactory extends EntityFactory<Alarm> {

  private SequenceDAO sequenceDAO;
  private TagCacheCollection tagFacadeGateway;
  private C2monCache<DataTag> dataTagCache;

  @Inject
  public AlarmFactory(SequenceDAO sequenceDAO, C2monCache<cern.c2mon.server.common.alarm.Alarm> alarmCache, C2monCache<DataTag> dataTagCache, TagCacheCollection tagFacadeGateway) {
    super(alarmCache);
    this.sequenceDAO = sequenceDAO;
    this.dataTagCache = dataTagCache;
    this.tagFacadeGateway = tagFacadeGateway;
  }

  @Override
  public List<ConfigurationElement> createInstance(Alarm alarm) {
    long tagId = alarm.getDataTagId() != null
        ? alarm.getDataTagId() : findTagIdByName(alarm.getDataTagName());

    // Check if the parent id exists
    if (tagFacadeGateway.containsKey(tagId)) {

      alarm.setDataTagId(tagId);
      return Collections.singletonList(doCreateInstance(alarm));

    } else {
      throw new ConfigurationParseException("Error creating alarm #" + alarm.getId() + ": " +
          "Specified parent datatag #" + tagId + " does not exist!");
    }
  }

  private Long findTagIdByName(String alarmName) {
    Collection<Tag> results = tagFacadeGateway.findByNameRegex(alarmName);

    for (Tag tag : results) {
      return tag.getId();
    }
    return null;
  }

  @Override
  Long createId(Alarm configurationEntity) {
    return configurationEntity.getId() != null ? configurationEntity.getId() : sequenceDAO.getNextAlarmId();
  }

  @Override
  Long getId(Alarm configurationEntity) {
    return configurationEntity.getId();
  }

  @Override
  public ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.ALARM;
  }
}
