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

import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.handler.RuleTagConfigHandler;
import cern.tim.server.cache.RuleTagCache;
import cern.tim.server.cache.RuleTagFacade;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.cache.loading.RuleTagLoaderDAO;
import cern.tim.server.common.rule.RuleTag;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.client.configuration.ConfigConstants.Action;
import cern.tim.shared.client.configuration.ConfigConstants.Entity;

/**
 * See interface documentation.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class RuleTagConfigHandlerImpl extends TagConfigHandlerImpl<RuleTag> implements RuleTagConfigHandler {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(RuleTagConfigHandlerImpl.class); 
  
  /**
   * Circular dependency between RuleTagConfigHandler
   * and TagConfigGateway, so autowire field.
   */
  @Autowired
  private TagConfigGateway tagConfigGateway;

  @Autowired
  public RuleTagConfigHandlerImpl(RuleTagCache ruleTagCache,
      RuleTagFacade ruleTagFacade, RuleTagLoaderDAO ruleTagLoaderDAO, TagLocationService tagLocationService) {
    super(ruleTagLoaderDAO, ruleTagFacade, ruleTagCache, tagLocationService);              
  }
  
  /**
   * Creates a rule on existing tags. 
   * 
   * <p>The DAQ does not need informing of this change (so no return
   * type as for DataTags.
   * 
   * @param element the details of the new object
   * @throws IllegalAccessException
   * @throws {@link UnexpectedRollbackException} if RuntimeException caught; DB transaction is rolled back and Rule & associated
   *                                                Tags are removed from cache 
   */
  @Transactional("cacheTransactionManager")
  @Override
  public void createRuleTag(ConfigurationElement element) throws IllegalAccessException {
    checkId(element.getEntityId());
    RuleTag ruleTag = commonTagFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
    Collection<Long> tagIds = ruleTag.getRuleInputTagIds();
    try {
      for (Long tagId : tagIds) {      
        tagConfigGateway.addRuleToTag(tagId, ruleTag.getId()); 
      }
      configurableDAO.insert(ruleTag);
      tagCache.putQuiet(ruleTag); 
    } catch (RuntimeException e) {
      String errMessage = "Exception caught while adding a RuleTag - rolling back DB transaction.";
      LOGGER.error(errMessage, e);
      tagCache.remove(ruleTag.getId());
      for (Long tagId : tagIds) {      
        tagCache.remove(tagId); 
      }
      throw new UnexpectedRollbackException(errMessage, e);
    }
           
  }
  
  /**
   * Takes all the necessary actions when updating
   * the configuration of a rule tag (updating the cache
   * object and the database).
   * 
   * @param id the id of the rule that is being reconfigured
   * @param properties the properties of fields that have changed
   * @throws IllegalAccessException
   * @throw {@link UnexpectedRollbackException} if failure; 
   */
  @Override
  @Transactional("cacheTransactionManager")
  public void updateRuleTag(Long id, Properties properties) throws IllegalAccessException {  
    RuleTag ruleTag = tagCache.get(id);
    ruleTag.getWriteLock().lock();
    try {
      Collection<Long> oldTagIds = null;
      //first record the old tag Ids before reconfiguring
      if (properties.containsKey("ruleText")) {
         oldTagIds = ruleTag.getRuleInputTagIds();
      }
      configurableDAO.updateConfig(ruleTag);      
      try {
        commonTagFacade.updateConfig(ruleTag, properties);      
        //if successful so far, adjust associated Tag (remove all old, add all new)
        if (oldTagIds != null) {
          for (Long oldTagId : oldTagIds) {
            tagConfigGateway.removeRuleFromTag(oldTagId, ruleTag.getId());
          }
          for (Long newTagId : ruleTag.getRuleInputTagIds()) {
            tagConfigGateway.addRuleToTag(newTagId, ruleTag.getId());    
          }
        }
      } catch (RuntimeException rEx) {
        String errMessage = "Exception caught while updating a RuleTag in cache - "
          + "rolling back DB transaction and removing from cache."; 
        LOGGER.error(errMessage, rEx);
        tagCache.remove(id);
        if (oldTagIds != null) {
          for (Long oldTagId : oldTagIds) {
            tagCache.remove(oldTagId);
          }
          for (Long newTagId : ruleTag.getRuleInputTagIds()) {
            tagCache.remove(newTagId);   
          }
        }        
        throw new UnexpectedRollbackException(errMessage, rEx);
      }      
    } finally {
      ruleTag.getWriteLock().unlock();      
    }    
  }
  
  /**
   * Note DB delete is rolled back if cache remove fails.
   */
  @Override
  @Transactional("cacheTransactionManager")
  public void removeRuleTag(final Long id, final ConfigurationElementReport elementReport) {
    RuleTag ruleTag = tagCache.get(id);
    ruleTag.getWriteLock().lock();
    try {     
      if (!ruleTag.getRuleIds().isEmpty()) {
        for (Long ruleId : ruleTag.getRuleIds()) {
          ConfigurationElementReport newReport = new ConfigurationElementReport(Action.REMOVE, Entity.RULETAG, ruleId);
          elementReport.addSubReport(newReport);
          removeRuleTag(ruleId, newReport);
        }                
      } else if (!ruleTag.getAlarmIds().isEmpty()) {
        String errMessage = "Unable to remove Rule with id " + id + " until the following alarms have been removed " + ruleTag.getAlarmIds().toString();
        elementReport.setFailure(errMessage);
        throw new RuntimeException(errMessage);
      } else {        
        configurableDAO.deleteItem(ruleTag.getId());
        tagCache.remove(ruleTag.getId());        
      }
    }
    catch (RuntimeException rEx) {
      String errMessage = "Exception caught when removing rule tag with id " + id;
      LOGGER.error(errMessage, rEx);
      throw new UnexpectedRollbackException(errMessage, rEx);   
    } finally {
      ruleTag.getWriteLock().unlock();
    }   
  }
     
}
