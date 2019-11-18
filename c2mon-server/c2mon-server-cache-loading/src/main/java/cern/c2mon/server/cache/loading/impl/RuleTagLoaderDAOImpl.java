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
package cern.c2mon.server.cache.loading.impl;

import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.cache.loading.CacheLoaderName;
import cern.c2mon.server.cache.loading.RuleTagLoaderDAO;
import cern.c2mon.server.cache.loading.common.AbstractBatchLoaderDAO;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * RuleTag loader DAO.
 *
 * @author Mark Brightwell
 */
@Service(CacheLoaderName.Names.RULETAG)
public class RuleTagLoaderDAOImpl extends AbstractBatchLoaderDAO<Long, RuleTag> implements RuleTagLoaderDAO {

  private RuleTagMapper ruleTagMapper;

  /**
   * We add the unused dataTagMapper to ensure it will have been instantiated first - it is a
   * dependency of {@link RuleTagMapper}! This is due to Batis 'extends' relationship in the
   * resultMap. Check out RuleTagMapper.xml for more
   */
  @Inject
  public RuleTagLoaderDAOImpl(DataTagMapper dataTagMapper, RuleTagMapper ruleTagMapper) {
    super(ruleTagMapper); //initial buffer size
    this.ruleTagMapper = ruleTagMapper;
  }

  @Override
  public void insert(RuleTag ruleTag) {
    ruleTagMapper.insertRuleTag((RuleTagCacheObject) ruleTag);
  }

  @Override
  public void deleteItem(Long id) {
    ruleTagMapper.deleteRuleTag(id);
  }

  @Override
  public void updateConfig(RuleTag ruleTag) {
    ruleTagMapper.updateConfig(ruleTag);
  }

  @Override
  protected RuleTag doPostDbLoading(RuleTag item) {
    return item;
  }
}
