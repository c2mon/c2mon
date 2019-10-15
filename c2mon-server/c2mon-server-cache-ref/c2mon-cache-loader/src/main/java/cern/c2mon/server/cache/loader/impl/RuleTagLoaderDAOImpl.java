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
package cern.c2mon.server.cache.loader.impl;

import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.cache.loader.CacheLoaderName;
import cern.c2mon.server.cache.loader.RuleTagLoaderDAO;
import cern.c2mon.server.cache.loader.common.AbstractBatchLoaderDAO;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
  @Autowired
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
