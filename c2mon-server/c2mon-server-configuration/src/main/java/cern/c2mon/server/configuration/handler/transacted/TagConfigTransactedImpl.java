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

import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.CommonTagFacade;
import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.ConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

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
 * @author Mark Brightwell
 * 
 * @param <T> the type of Tag
 *
 */
@Slf4j
abstract class TagConfigTransactedImpl<T extends Tag> implements TagConfigTransacted {
  
  protected C2monCache<Long, T> tagCache;
  
  /**
   * The Facade bean for which this TagConfigHandler
   * provides the common functionality.
   * 
   * <p>(e.g. to provide helper functions for DataTag
   * configuration, this should be the {@link DataTagFacade}
   * bean).
   */
  protected CommonTagFacade<T> commonTagFacade;
  
  /**
   * The corresponding DAO.
   * 
   */
  protected ConfigurableDAO<T> configurableDAO;
  
  protected TagLocationService tagLocationService;

  protected final Collection<ConfigurationEventListener> configurationEventListeners;
  
  public TagConfigTransactedImpl(ConfigurableDAO<T> configurableDAO,
                                 CommonTagFacade<T> configurableTagFacade,
                                 C2monCache<Long, T> tagCache,
                                 TagLocationService tagLocationService,
                                 final GenericApplicationContext context) {
    super();
    this.commonTagFacade = configurableTagFacade;   
    this.configurableDAO = configurableDAO;
    this.tagCache = tagCache;
    this.tagLocationService = tagLocationService;
    this.configurationEventListeners = context.getBeansOfType(ConfigurationEventListener.class).values();
  }

  /**
   * Throw a {@link ConfigurationException} if the Tag id already exists in one of the Tag
   * caches.
   * @param id the id to check 
   */
  protected void checkId(final Long id) {
    if (tagLocationService.isInTagCache(id)) {      
        throw new ConfigurationException(ConfigurationException.ENTITY_EXISTS, 
            "Attempting to create a Tag with an already existing id: " + id);      
    }
  }
  
  /**
   * If necessary, updates the list of rules that need evaluating for this tag,
   * persisting the change to the database also.
   * 
   * @param tagId the tag object in the cache
   * @param ruleId the rule id
   */
  @Override
  @Transactional(value = "cacheTransactionManager")
  public void addRuleToTag(final Long tagId, final Long ruleId) {
    log.trace("Adding rule " + ruleId + " reference from Tag " + tagId);
    tagCache.acquireWriteLockOnKey(tagId);    
    try {
      T tag = tagCache.get(tagId);
      if (!tag.getRuleIds().contains(ruleId)) {
        commonTagFacade.addDependentRuleToTag(tag, ruleId);  
        configurableDAO.updateConfig(tag);
        tagCache.putQuiet(tag);
      }
    } finally {
      tagCache.releaseWriteLockOnKey(tagId);    
    }  
  }
  
  @Override
  @Transactional(value = "cacheTransactionManager")
  public void removeRuleFromTag(final Long tagId, final Long ruleId) {
    log.trace("Removing rule " + ruleId + " reference from Tag " + tagId);
    tagCache.acquireWriteLockOnKey(tagId);
    try {      
      T tag = tagCache.get(tagId);
      if (tag.getRuleIds().contains(ruleId)) {
        commonTagFacade.removeDependentRuleFromTag(tag, ruleId);
        configurableDAO.updateConfig(tag);
        tagCache.putQuiet(tag);
      }
    } finally {    
      tagCache.releaseWriteLockOnKey(tagId);
    }
  }
  
  @Override
  @Transactional(value = "cacheTransactionManager")
  public void addAlarmToTag(final Long tagId, final Long alarmId) {
    log.trace("Adding Alarm " + alarmId + " reference from Tag " + tagId);
    tagCache.acquireWriteLockOnKey(tagId);
    try {
      T tag = tagCache.get(tagId);
      tag.getAlarmIds().add(alarmId);
      tagCache.putQuiet(tag);
    } finally {
      tagCache.releaseWriteLockOnKey(tagId);
    }  
  }
  
  @Override
  @Transactional(value = "cacheTransactionManager")
  public void removeAlarmFromTag(final Long tagId, final Long alarmId) {
    log.trace("Removing Alarm " + alarmId + " reference from Tag " + tagId);
    tagCache.acquireWriteLockOnKey(tagId);
    try {      
      T tag = tagCache.get(tagId);
      tag.getAlarmIds().remove(alarmId);
      tagCache.putQuiet(tag);
    } finally {
      tagCache.releaseWriteLockOnKey(tagId);
    }
  }
  
}

