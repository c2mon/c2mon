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

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;

/**
 * @author Franz Ritter
 */
@Service
public class RuleTagFactory extends EntityFactory<RuleTag> {
  private final RuleTagCache ruleTagCache;
  private final TagFacadeGateway tagFacadeGateway;
  private final SequenceDAO sequenceDAO;

  @Autowired
  public RuleTagFactory(RuleTagCache ruleTagCache, TagFacadeGateway tagFacadeGateway, SequenceDAO sequenceDAO) {
    super(ruleTagCache);
    this.ruleTagCache = ruleTagCache;
    this.tagFacadeGateway = tagFacadeGateway;
    this.sequenceDAO = sequenceDAO;
  }

  @Override
  public List<ConfigurationElement> createInstance(RuleTag configurationEntity) {
    return Collections.singletonList(doCreateInstance(configurationEntity));
  }

  @Override
  Long createId(RuleTag configurationEntity) {
    return configurationEntity.getId() != null ? configurationEntity.getId() : sequenceDAO.getNextTagId();
  }

  @Override
  Long getId(RuleTag configurationEntity) {
    return configurationEntity.getId() != null ? configurationEntity.getId() : ruleTagCache.get(configurationEntity.getName()).getId();
  }

  @Override
  boolean hasEntity(Long id) {
    return id != null && tagFacadeGateway.isInTagCache(id);
  }

  @Override
  ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.RULETAG;
  }
}
