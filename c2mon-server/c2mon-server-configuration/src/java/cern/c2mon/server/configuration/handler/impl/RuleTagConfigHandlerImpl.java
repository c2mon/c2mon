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
package cern.c2mon.server.configuration.handler.impl;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import cern.c2mon.server.configuration.handler.RuleTagConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.RuleTagConfigTransacted;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.rule.RuleEvaluator;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * See interface documentation.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class RuleTagConfigHandlerImpl implements RuleTagConfigHandler {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(RuleTagConfigHandlerImpl.class);  
  
  /**
   * Transacted bean.
   */
  @Autowired
  private RuleTagConfigTransacted ruleTagConfigTransacted;
  
  private RuleTagCache ruleTagCache;
  
  private RuleEvaluator ruleEvaluator;
  
  @Autowired
  public RuleTagConfigHandlerImpl(final RuleTagCache ruleTagCache, final RuleEvaluator ruleEvaluator) {    
    this.ruleTagCache = ruleTagCache;
    this.ruleEvaluator = ruleEvaluator;
  }

  @Override
  public void removeRuleTag(final Long id, final ConfigurationElementReport elementReport) {    
    ruleTagConfigTransacted.doRemoveRuleTag(id, elementReport);    
    ruleTagCache.remove(id); //will be skipped if rollback exception thrown in do method    
  }

  @Override
  public void createRuleTag(ConfigurationElement element) throws IllegalAccessException {
    ruleTagConfigTransacted.doCreateRuleTag(element);
    ruleEvaluator.evaluateRule(element.getEntityId());
    ruleTagCache.lockAndNotifyListeners(element.getEntityId());    
  }

  @Override
  public void updateRuleTag(Long id, Properties elementProperties) throws IllegalAccessException {
    try {
      ruleTagConfigTransacted.doUpdateRuleTag(id, elementProperties);
      ruleEvaluator.evaluateRule(id);
      ruleTagCache.lockAndNotifyListeners(id);
    } catch (UnexpectedRollbackException e) {
      LOGGER.error("Rolling back Rule update in cache");
      ruleTagCache.remove(id);
      ruleTagCache.loadFromDb(id);
      throw e;
    }
  }
  
  @Override
  public void addAlarmToTag(Long tagId, Long alarmId) {
    ruleTagConfigTransacted.addAlarmToTag(tagId, alarmId);
  }

  @Override
  public void addRuleToTag(Long tagId, Long ruleId) {
    ruleTagConfigTransacted.addRuleToTag(tagId, ruleId);
  }

  @Override
  public void removeAlarmFromTag(Long tagId, Long alarmId) {
    ruleTagConfigTransacted.removeAlarmFromTag(tagId, alarmId);
  }

  @Override
  public void removeRuleFromTag(Long tagId, Long ruleId) {
    ruleTagConfigTransacted.removeRuleFromTag(tagId, ruleId);
  }
  
}
