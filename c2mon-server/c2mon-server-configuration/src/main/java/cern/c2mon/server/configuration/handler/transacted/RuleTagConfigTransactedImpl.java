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
package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.actions.rule.RuleTagCacheObjectFactory;
import cern.c2mon.cache.actions.rule.RuleTagService;
import cern.c2mon.server.cache.loading.RuleTagLoaderDAO;
import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Properties;

/**
 * Implementation of transacted configuration methods.
 * @author Mark Brightwell
 *
 */
@Service
@Slf4j
public class RuleTagConfigTransactedImpl extends TagConfigTransactedImpl<RuleTag>  {

  private AlarmConfigTransactedImpl alarmConfigHandler;

  @Autowired
  public RuleTagConfigTransactedImpl(RuleTagService ruleTagService,
                                     RuleTagCacheObjectFactory ruleTagCacheObjectFactory,
                                     RuleTagLoaderDAO ruleTagLoaderDAO,
                                     GenericApplicationContext context, AlarmConfigTransactedImpl alarmConfigTransacted) {
    super(ruleTagService.getCache(), ruleTagLoaderDAO, ruleTagCacheObjectFactory, ruleTagService, context);
    this.alarmConfigHandler = alarmConfigTransacted;
  }

  @Override
  protected void doPostCreate(RuleTag ruleTag) {
    for (Long tagId : ruleTag.getRuleInputTagIds()) {
      addRuleToTag(tagId, ruleTag.getId());
    }
    super.doPostCreate(ruleTag);
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange update(Long id, Properties properties) {
    Collection<Long> oldTagIds = null;

    //first record the old tag Ids before reconfiguring
    if (properties.containsKey("ruleText")) {
      oldTagIds = cache.get(id).getRuleInputTagIds();
    }

    ProcessChange processChange = super.update(id, properties);


    if (oldTagIds != null) {
      Collection<Long> newTagIds = cache.get(id).getRuleInputTagIds();

      for (Long oldTagId : oldTagIds) {
        removeRuleFromTag(oldTagId, id);
      }
      for (Long newTagId : newTagIds) {
        addRuleToTag(newTagId, id);
      }
    }

    return processChange;
  }

  @Override
  protected void doPostUpdate(RuleTag cacheable) {
    super.doPostUpdate(cacheable);
    //reset all parent DAQ/Equipment ids of rules higher up the pile - if fails, no rolling back possible, so rule cache may be left inconsistent
    try {
      log.trace("Resetting all relevant Rule parent Process/Equipment ids");
      for (Long parentRuleId : cacheable.getRuleIds()) {
        ruleTagFacade.setParentSupervisionIds(parentRuleId);
      }
    } catch (Exception e) {
      String msg = "Exception while reloading rule parent ids: cache may be left in inconsistent state! - need to remove this rule to try and recover consistency";
      log.error(msg, e);
      throw new UnexpectedRollbackException(msg, e);
    }
  }

  @Override
  protected void doPreRemove(RuleTag ruleTag, ConfigurationElementReport elementReport) {
    super.doPreRemove(ruleTag, elementReport);

    createConfigRemovalReportsFor(Entity.ALARM, ruleTag.getAlarmIds(), alarmConfigHandler.cache)
      .forEach(elementReport::addSubReport);

    createConfigRemovalReportsFor(Entity.RULETAG, ruleTag.getRuleIds(), cache)
      .forEach(elementReport::addSubReport);

    for (Long inputTagId : ruleTag.getCopyRuleInputTagIds()) {
      removeRuleFromTag(inputTagId, ruleTag.getId());
    }
  }

  @Override
  public ProcessChange remove(Long id, ConfigurationElementReport report) {
    ProcessChange result = super.remove(id, report);

    for (ConfigurationEventListener listener : configurationEventListeners) {
      listener.onConfigurationEvent(ruleTag, Action.REMOVE);
    }
  }
}
