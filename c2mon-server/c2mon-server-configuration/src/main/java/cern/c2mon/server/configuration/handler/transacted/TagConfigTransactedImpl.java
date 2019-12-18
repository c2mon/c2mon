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

import cern.c2mon.cache.actions.rule.RuleTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Public methods in this class should perform the complete
 * configuration process for the given tag (i.e. cache update
 * and database persistence).
 * 
 * <p>The methods contain the common reconfiguration logic for
 * all Tag objects (Control, Data and Rule tags).
 * 
 * <p>The appropriate Facade and DAO objects must be passed
 * to the constructor to provide the common configuration
 * functionality. 
 * 
 * <p>Notice that these methods will always be called within
 * a transaction initiated at the ConfigurationLoader level
 * and passed through the handler via a "create", "update"
 * or "remove" method, with rollback of DB changes if a 
 * RuntimeException is thrown.
 * 
 * @author Alexandros Papageorgiou, Mark Brightwell
 * 
 * @param <TAG> the type of Tag
 *
 */
@Slf4j
abstract class TagConfigTransactedImpl<TAG extends Tag> extends BaseConfigHandlerImpl<TAG, ProcessChange> {

  protected ConfigurableDAO<TAG> configurableDAO;

  private final RuleTagService ruleTagService;
  protected final Collection<ConfigurationEventListener> configurationEventListeners;

  public TagConfigTransactedImpl(final C2monCache<TAG> tagCache,
                                 final ConfigurableDAO<TAG> configurableDAO,
                                 final AbstractCacheObjectFactory<TAG> tagCacheObjectFactory,
                                 final RuleTagService ruleTagService,
                                 final GenericApplicationContext context) {
    super(tagCache, configurableDAO, tagCacheObjectFactory, ProcessChange::new);
    this.ruleTagService = ruleTagService;
    this.configurationEventListeners = context.getBeansOfType(ConfigurationEventListener.class).values();
  }

  @Override
  protected void doPostCreate(TAG cacheable) {
    super.doPostCreate(cacheable);
    for (ConfigurationEventListener listener : configurationEventListeners) {
      listener.onConfigurationEvent(cacheable, ConfigConstants.Action.CREATE);
    }
  }

  @Override
  protected void doPostUpdate(TAG cacheable) {
    super.doPostUpdate(cacheable);
    for (ConfigurationEventListener listener : configurationEventListeners) {
      listener.onConfigurationEvent(cacheable, ConfigConstants.Action.UPDATE);
    }
  }

  /**
   * Adds this Rule to the list of Rules that
   * need evaluating when this tag changes.
   *
   * If necessary, updates the list of rules that need evaluating for this tag,
   * persisting the change to the database also.
   * 
   * @param tagId the tag object in the cache
   * @param ruleId the rule id
   */
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public void addRuleToTag(final Long tagId, final Long ruleId) {
    log.trace("Adding rule " + ruleId + " reference from Tag " + tagId);
    ifConditionEditTag(tagId,
      tag -> !tag.getRuleIds().contains(ruleId),
      tag -> ruleTagService.addDependentRuleToTag(tag, ruleId));
  }

  /**
   * Removes this Rule from the list of Rules
   * that need evaluating when this Tag changes.
   *
   * @param tagId the tag pointing to the rule
   * @param ruleId the rule that no longer needs evaluating
   */
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public void removeRuleFromTag(final Long tagId, final Long ruleId) {
    log.trace("Removing rule " + ruleId + " reference from Tag " + tagId);
    ifConditionEditTag(tagId,
      tag -> tag.getRuleIds().contains(ruleId),
      tag -> ruleTagService.removeDependentRuleFromTag(tag, ruleId));
  }

  /**
   * Adds the alarm to the list of alarms associated to this
   * tag.
   *
   * @param tagId the id of the tag
   * @param alarmId the id of the alarm
   */
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public void addAlarmToTag(final Long tagId, final Long alarmId) {
    log.trace("Adding Alarm " + alarmId + " reference from Tag " + tagId);
    editTag(tagId, tag -> tag.getAlarmIds().add(alarmId));
  }

  /**
   * Removes the Alarm from the list of alarms
   * attached to the Tag.
   *
   * @param tagId the Tag id
   * @param alarmId the id of the alarm to remove
   */
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public void removeAlarmFromTag(final Long tagId, final Long alarmId) {
    log.trace("Removing Alarm " + alarmId + " reference from Tag " + tagId);
    editTag(tagId, tag -> tag.getAlarmIds().remove(alarmId));
  }

  Collection<ConfigurationElementReport> createConfigRemovalReportsFor(ConfigConstants.Entity entity, Collection<Long> ids, C2monCache<?> targetCache) {
    return ids.stream()
      .filter(targetCache::containsKey)
      .map(id -> new ConfigurationElementReport(ConfigConstants.Action.REMOVE, entity, id))
      .collect(Collectors.toList());
  }

  private void ifConditionEditTag(final Long tagId, Predicate<TAG> condition, Consumer<TAG> mutator) {
    cache.computeQuiet(tagId, tag -> {
      if (condition.test(tag)) {
        configurableDAO.updateConfig(tag);
        mutator.accept(tag);
      }
    });
  }

  private void editTag(final Long tagId, Consumer<TAG> mutator) {
    ifConditionEditTag(tagId, tag -> true, mutator);
  }
  
}

