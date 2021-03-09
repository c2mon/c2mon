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

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.cache.config.rule.RuleTagCacheObjectFactory;
import cern.c2mon.server.cache.loading.RuleTagLoaderDAO;
import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.rule.RuleEvaluator;
import cern.c2mon.server.rule.RuleTagService;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of transacted configuration methods.
 *
 * @author Mark Brightwell
 */
@Named
@Slf4j
@EnableTransactionManagement(proxyTargetClass = true)
public class RuleTagConfigHandler extends AbstractTagConfigHandler<RuleTag> {

  private final RuleTagService ruleTagService;
  private final RuleEvaluator ruleEvaluator;
  private final TagCacheCollection tagCacheCollection;

  @Inject
  public RuleTagConfigHandler(final C2monCache<RuleTag> ruleTagCache,
                              final RuleTagService ruleTagService,
                              final RuleTagCacheObjectFactory ruleTagCacheObjectFactory,
                              final RuleTagLoaderDAO ruleTagLoaderDAO,
                              final GenericApplicationContext context,
                              final AlarmConfigHandler alarmConfigHandler,
                              final TagCacheCollection tagCacheCollection,
                              RuleEvaluator ruleEvaluator) {
    super(ruleTagCache, ruleTagLoaderDAO, ruleTagCacheObjectFactory, tagCacheCollection, context, alarmConfigHandler);
    this.ruleTagService = ruleTagService;
    this.tagCacheCollection = tagCacheCollection;
    this.ruleEvaluator = ruleEvaluator;
  }

  @Override
  protected void doPostCreate(RuleTag ruleTag) {
    for (Long tagId : ruleTag.getRuleInputTagIds()) {
      tagCacheCollection.addRuleToTag(tagId, ruleTag.getId());
    }
    ruleEvaluator.evaluateRule(ruleTag.getId());
    super.doPostCreate(ruleTag);
  }

  @Override
  public List<ProcessChange> update(Long id, Properties properties) {
    Collection<Long> oldTagIds = null;

    //first record the old tag Ids before reconfiguring
    if (properties.containsKey("ruleText")) {
      oldTagIds = cache.get(id).getRuleInputTagIds();
    }

    List<ProcessChange> processChanges = super.update(id, properties);


    if (oldTagIds != null) {
      Collection<Long> newTagIds = cache.get(id).getRuleInputTagIds();

      for (Long oldTagId : oldTagIds) {
        tagCacheCollection.removeRuleFromTag(oldTagId, id);
      }
      for (Long newTagId : newTagIds) {
        tagCacheCollection.addRuleToTag(newTagId, id);
      }
    }

    return processChanges;
  }

  @Override
  protected void doPostUpdate(RuleTag cacheable) {
    super.doPostUpdate(cacheable);
    //reset all parent DAQ/Equipment ids of rules higher up the pile - if fails, no rolling back possible, so rule cache may be left inconsistent
    try {
      log.trace("Resetting all relevant Rule parent Process/Equipment ids");
      for (Long parentRuleId : cacheable.getRuleIds()) {
        ruleTagService.setParentSupervisionIds(parentRuleId);
      }
    } catch (Exception e) {
      String msg = "Exception while reloading rule parent ids: cache may be left in inconsistent state! - need to remove this rule to try and recover consistency";
      log.error(msg, e);
      throw new UnexpectedRollbackException(msg, e);
    }
  }

  @Override
  protected void doPreRemove(RuleTag ruleTag, ConfigurationElementReport elementReport) {
    createConfigRemovalReportsFor(Entity.ALARM, ruleTag.getAlarmIds(), alarmConfigHandler.getCache())
      .forEach(report -> {
        alarmConfigHandler.remove(report.getId(), elementReport);
        elementReport.addSubReport(report);
      });

    // TODO (Alex) Do we want to be removing dependent rules?
//    createConfigRemovalReportsFor(Entity.RULETAG, ruleTag.getRuleIds(), cache)
//      .forEach(report -> {
//        elementReport.addSubReport(report);
//      });

    Set<Long> existingRuleInputTagIds = ruleTag.getRuleInputTagIds()
      .stream()
      .filter(tagCacheCollection::containsKey)
      .collect(Collectors.toSet());

    for (Long inputTagId : existingRuleInputTagIds) {
      tagCacheCollection.removeRuleFromTag(inputTagId, ruleTag.getId());
    }
  }

  @Override
  protected List<ProcessChange> removeReturnValue(RuleTag ruleTag, ConfigurationElementReport report) {
    for (ConfigurationEventListener listener : configurationEventListeners) {
      listener.onConfigurationEvent(ruleTag, Action.REMOVE);
    }
    return super.removeReturnValue(ruleTag, report);
  }

}
