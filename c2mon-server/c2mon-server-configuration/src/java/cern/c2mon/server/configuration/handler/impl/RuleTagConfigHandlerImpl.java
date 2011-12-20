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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.c2mon.server.configuration.handler.RuleTagConfigHandler;
import cern.tim.server.cache.RuleTagCache;
import cern.tim.server.cache.RuleTagFacade;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.cache.exception.CacheElementNotFoundException;
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
  private AlarmConfigHandler alarmConfigHandler;

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
    LOGGER.trace("Creating RuleTag with id " + element.getEntityId());
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
        tagCache.remove(tagId); //for removing all references to the rule; tags will be reloaded from DB
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
    LOGGER.trace("Updating RuleTag " + id);
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
            tagCache.remove(oldTagId); //TODO: wrong cache here ?!
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
   * TODO revisit locking here; either remove as config single threaded or fix order of rule locks...
   */
  @Override
  @Transactional("cacheTransactionManager")
  public void removeRuleTag(final Long id, final ConfigurationElementReport elementReport) {
    LOGGER.trace("Removing RuleTag " + id);
    try {
      RuleTag ruleTag = tagCache.get(id);
      ruleTag.getWriteLock().lock();
      Collection<Long> ruleInputTagIds = Collections.EMPTY_LIST;
      try {
        ruleInputTagIds = ruleTag.getCopyRuleInputTagIds();        
        Collection<Long> ruleIds = ruleTag.getCopyRuleIds();  
        Collection<Long> alarmIds = ruleTag.getCopyAlarmIds();          
        configurableDAO.deleteItem(ruleTag.getId());
        tagCache.remove(ruleTag.getId());
        if (!alarmIds.isEmpty()) {
          LOGGER.debug("Removing Alarms dependent on RuleTag " + id);
          for (Long alarmId : alarmIds) { //need copy as modified concurrently by remove alarm
            ConfigurationElementReport alarmReport = new ConfigurationElementReport(Action.REMOVE, Entity.ALARM, alarmId);
            elementReport.addSubReport(alarmReport);
            alarmConfigHandler.removeAlarm(alarmId, alarmReport);
          }        
        }
        for (Long inputTagId : ruleInputTagIds) {
          tagConfigGateway.removeRuleFromTag(inputTagId, id); //allowed to lock tag below the rule...
        }
        ruleTag.getWriteLock().unlock(); //.. but not rules "above"!
        //unlock before removing rules
        if (!ruleIds.isEmpty()) {
          LOGGER.debug("Removing rules dependent on RuleTag " + id);
          for (Long ruleId : ruleIds) { //concurrent modifcation as a rule is removed from the list during the remove call!
            if (tagLocationService.isInTagCache(ruleId)) { //may already have been removed if a previous rule in the list was used in this rule!
              ConfigurationElementReport newReport = new ConfigurationElementReport(Action.REMOVE, Entity.RULETAG, ruleId);
              elementReport.addSubReport(newReport);
              removeRuleTag(ruleId, newReport);
            }         
          }                
        }                               
      }
      catch (RuntimeException rEx) {
        String errMessage = "Exception caught when removing rule tag with id " + id;
        LOGGER.error(errMessage, rEx);
        tagCache.remove(id);
        for (Long inputTagId : ruleInputTagIds) {
          tagLocationService.remove(inputTagId);
        }            
        throw new UnexpectedRollbackException(errMessage, rEx);   
      } finally {
        if (ruleTag.getWriteLock().isHeldByCurrentThread()) {
          ruleTag.getWriteLock().unlock();
        }        
      }
    } catch (CacheElementNotFoundException e) {
      LOGGER.debug("Attempting to remove a non-existent RuleTag - no action taken.");
      elementReport.setWarning("Attempting to removed a non-existent RuleTag");      
    }       
  }
  
}
