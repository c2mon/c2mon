/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
package cern.c2mon.server.cache.loading.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.cache.loading.RuleTagLoaderDAO;
import cern.c2mon.server.cache.loading.common.AbstractBatchLoaderDAO;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;

/**
 * RuleTag loader DAO.
 * 
 * @author Mark Brightwell
 *
 */
@Service("ruleTagLoaderDAO")
public class RuleTagLoaderDAOImpl extends AbstractBatchLoaderDAO<RuleTag> implements RuleTagLoaderDAO {
  
  /**
   * The iBatis mapper.
   */
  private RuleTagMapper ruleTagMapper;
  
  @Value("${c2mon.jms.tag.publication.topic}") 
  private String publicationTrunk;

  @Autowired
  public RuleTagLoaderDAOImpl(RuleTagMapper ruleTagMapper) {
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
    item.setTopic(publicationTrunk + "." + item.getLowestProcessId());
    return item;
  }


}
