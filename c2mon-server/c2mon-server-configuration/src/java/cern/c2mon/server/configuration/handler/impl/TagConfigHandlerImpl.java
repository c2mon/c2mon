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

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import cern.tim.server.cache.CommonTagFacade;
import cern.tim.server.cache.DataTagFacade;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.cache.TimCache;
import cern.tim.server.cache.loading.ConfigurableDAO;
import cern.tim.server.common.tag.Tag;
import cern.tim.shared.common.ConfigurationException;

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
abstract class TagConfigHandlerImpl<T extends Tag> implements TagConfigHandler<T> {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(TagConfigHandlerImpl.class);
  
  protected TimCache<T> tagCache;
  
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
  
  public TagConfigHandlerImpl(ConfigurableDAO<T> configurableDAO, CommonTagFacade<T> configurableTagFacade, TimCache<T> tagCache, TagLocationService tagLocationService) {
    super();
    this.commonTagFacade = configurableTagFacade;   
    this.configurableDAO = configurableDAO;
    this.tagCache = tagCache;
    this.tagLocationService = tagLocationService;
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
   * @param tag the tag object in the cache
   * @param ruleId the rule id
   */
  @Override
  @Transactional("cacheTransactionManager")
  public void addRuleToTag(final Long tagId, final Long ruleId) {
    LOGGER.trace("Adding rule " + ruleId + " reference from Tag " + tagId);
    T tag = tagCache.get(tagId);
    try {
      tag.getWriteLock().lock();
      if (!tag.getRuleIds().contains(ruleId)) {
        commonTagFacade.addDependentRuleToTag(tag, ruleId);   
        configurableDAO.updateConfig(tag);      
      } 
    } finally {
      tag.getWriteLock().unlock();
    }  
  }
  
  @Override
  @Transactional("cacheTransactionManager")
  public void removeRuleFromTag(final Long tagId, final Long ruleId) {
    LOGGER.trace("Removing rule " + ruleId + " reference from Tag " + tagId);
    T tag = tagCache.get(tagId);
    try {
      tag.getWriteLock().lock();
      if (tag.getRuleIds().contains(ruleId)) {
        commonTagFacade.removeDependentRuleFromTag(tag, ruleId);
        configurableDAO.updateConfig(tag);      
      }
    } finally {    
      tag.getWriteLock().unlock();
    }
  }
  
  @Override
  @Transactional("cacheTransactionManager")
  public void addAlarmToTag(final Long tagId, final Long alarmId) {
    LOGGER.trace("Adding Alarm " + alarmId + " reference from Tag " + tagId);
    T tag = tagCache.get(tagId);
    try {
      tag.getWriteLock().lock();
      commonTagFacade.addAlarm(tag, alarmId);      
    } finally {
      tag.getWriteLock().unlock();
    }  
  }
  
  @Override
  @Transactional("cacheTransactionManager")
  public void removeAlarmFromTag(final Long tagId, final Long alarmId) {
    LOGGER.trace("Removing Alarm " + alarmId + " reference from Tag " + tagId);
    Tag tag = tagCache.get(tagId);
    try {
      tag.getWriteLock().lock();
      tag.getAlarmIds().remove(alarmId); 
    } finally {
      tag.getWriteLock().unlock();
    }
  }
  
}
