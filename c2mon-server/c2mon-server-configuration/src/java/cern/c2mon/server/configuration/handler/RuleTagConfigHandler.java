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
package cern.c2mon.server.configuration.handler;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.tim.server.cache.RuleTagCache;
import cern.tim.server.cache.RuleTagFacade;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.cache.loading.RuleTagLoaderDAO;
import cern.tim.server.common.rule.RuleTag;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.common.datatag.DataTagQuality;

/**
 * Class managing the reconfiguration action on RuleTags.
 * The update and create methods should throw exceptions
 * which will be caught by the ConfigurationLoader class
 * and entered into the report.
 * 
 * <p>The remove method may be called from the Process 
 * remove method, in which case the sub-report can be
 * filled in if necessary.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class RuleTagConfigHandler extends TagConfigHandlerImpl<RuleTag> {
  
  /**
   * Circular dependency between RuleTagConfigHandler
   * and TagConfigGateway, so autowire field.
   */
  @Autowired
  private TagConfigGateway tagConfigGateway;

  @Autowired
  public RuleTagConfigHandler(RuleTagCache ruleTagCache,
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
   */
  public void createRuleTag(ConfigurationElement element) throws IllegalAccessException {
    checkId(element.getEntityId());
    RuleTag ruleTag = commonTagFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
    Collection<Long> tagIds = ruleTag.getRuleInputTagIds();
    for (Long tagId : tagIds) {      
      tagConfigGateway.addRuleToTag(tagId, ruleTag.getId()); 
    }
    configurableDAO.insert(ruleTag);
    tagCache.putQuiet(ruleTag);        
  }
  
  /**
   * Takes all the necessary actions when updating
   * the configuration of a rule tag (updating the cache
   * object and the database).
   * 
   * @param id the id of the rule that is being reconfigured
   * @param properties the properties of fields that have changed
   * @throws IllegalAccessException 
   */
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
    } finally {
      ruleTag.getWriteLock().unlock();      
    }
    
  }
  
  public void removeRuleTag(Long id, ConfigurationElementReport elementReport) {
    RuleTag ruleTag = tagCache.get(id);
    ruleTag.getWriteLock().lock();
    try {     
      if (!ruleTag.getRuleIds().isEmpty()) {
        elementReport.setFailure("Unable to remove Rule with id " + id + " until the following rules have been removed " + ruleTag.getRuleIds().toString());      
      } else if (!ruleTag.getAlarmIds().isEmpty()) {
        elementReport.setFailure("Unable to remove Rule with id " + id + " until the following alarms have been removed " + ruleTag.getAlarmIds().toString()); 
      } else {
        //commonTagFacade.invalidate(ruleTag.getId(), new DataTagQuality(DataTagQuality.REMOVED, "The Rule has been removed from the system and is no longer monitored."), new Timestamp(System.currentTimeMillis()));
        configurableDAO.deleteItem(ruleTag.getId());
        tagCache.remove(ruleTag.getId());        
      }
    } finally {
      ruleTag.getWriteLock().unlock();
    }   
  }
     
}
