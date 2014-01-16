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
package cern.c2mon.server.cache.common;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.CommonTagFacade;
import cern.c2mon.server.cache.ControlTagFacade;
import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.RuleTagFacade;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.daq.config.Change;

/**
 * Implementation of the TagFacadeGateway.
 * @author Mark Brightwell
 *
 */
@Service
public class TagFacadeGatewayImpl implements TagFacadeGateway {

  /**
   * Reference to the DataTagFacade bean.
   */
  private DataTagFacade dataTagFacade;
  
  /**
   * Reference to the ControlTagFacade bean.
   */
  private ControlTagFacade controlTagFacade;
  
  /**
   * Reference to the RuleTagFacade bean.
   */
  private RuleTagFacade ruleTagFacade;
  
  /**
   * Service for locating Tags in the various caches.
   */
  private TagLocationService tagLocationService;
  
  /**
   * Autowired constructor.
   * 
   * @param dataTagFacade the DataTag facade bean
   * @param controlTagFacade the ControlTag facade
   * @param ruleTagFacade the RuleTag facade
   * @param tagLocationService the Tag location service
   */
  @Autowired
  public TagFacadeGatewayImpl(final DataTagFacade dataTagFacade, final ControlTagFacade controlTagFacade,
      final RuleTagFacade ruleTagFacade, final TagLocationService tagLocationService) {
    super();
    this.dataTagFacade = dataTagFacade;
    this.controlTagFacade = controlTagFacade;
    this.ruleTagFacade = ruleTagFacade;
    this.tagLocationService = tagLocationService;
  }

//  private <T extends Tag> CommonTagFacade<>
  
  @SuppressWarnings("unchecked")
  private <T extends Tag> CommonTagFacade<T> getFacade(final T tag) {
    if (tag instanceof RuleTag) {
      return (CommonTagFacade<T>) ruleTagFacade;
    } else if (tag instanceof ControlTag) {
      return (CommonTagFacade<T>) controlTagFacade;
    } else {       
      return (CommonTagFacade<T>) dataTagFacade;
    }
  }

  @Override
  public void invalidate(final Long tagId, final TagQualityStatus statusToAdd, final String statusDescription, final Timestamp timestamp) {
    tagLocationService.acquireWriteLockOnKey(tagId);
    try {
      Tag tag = tagLocationService.get(tagId);
      getFacade(tag).invalidate(tag.getId(), statusToAdd, statusDescription, timestamp);
    } finally {
      tagLocationService.releaseWriteLockOnKey(tagId);
    }    
  }

  @Override
  public void invalidate(final Tag tag, final TagQualityStatus statusToAdd, final String statusDescription, final Timestamp timestamp) {
    getFacade(tag).invalidate(tag, statusToAdd, statusDescription, timestamp);
  }

//  @Override
//  public void setStatus(Tag tag, Status status) {
//    getFacade(tag).setStatus(tag, status);
//  }
 
  @Override
  public void addAlarm(final Tag tag, final Long alarmId) {
    getFacade(tag).addAlarm(tag, alarmId);    
  }

  @Override
  public void addDependentRuleToTag(final Tag tag, final Long ruleId) {
    getFacade(tag).addDependentRuleToTag(tag, ruleId);
  }

  @Override
  public Tag createCacheObject(final Long id, final Properties properties) {
    throw new UnsupportedOperationException("This method cannot be called for on the Facade gateway!");
  }

  @Override
  public Change updateConfig(Tag cacheable, Properties properties) {
    throw new UnsupportedOperationException("This method cannot be called for on the Facade gateway!");
  }

  @Override
  public List<Alarm> evaluateAlarms(Tag tag) {
    return getFacade(tag).evaluateAlarms(tag);
  }

  @Override
  public void setQuality(Tag tag, Collection<TagQualityStatus> flagsToAdd, Collection<TagQualityStatus> flagsToRemove, Map<TagQualityStatus, String> qualityDescriptions, Timestamp timestamp) {
    getFacade(tag).setQuality(tag, flagsToAdd, flagsToRemove, qualityDescriptions, timestamp);
  }

  @Override
  public Collection<Long> getParentEquipments(Tag tag) {
    return getFacade(tag).getParentEquipments(tag);
  }

  @Override
  public Collection<Long> getParentProcesses(Tag tag) {
    return getFacade(tag).getParentProcesses(tag); 
  }

  @Override
  public void removeDependentRuleFromTag(Tag tag, Long ruleTagId) {
    getFacade(tag).removeDependentRuleFromTag(tag, ruleTagId);
  }

  @Override
  public TagWithAlarms getTagWithAlarms(Long id) {
    Tag tag = tagLocationService.get(id);
    return getFacade(tag).getTagWithAlarms(id);    
  }

  @Override
  public void setQuality(Long tagId, Collection<TagQualityStatus> flagsToAdd,
      Collection<TagQualityStatus> flagsToRemove, Map<TagQualityStatus, String> qualityDescriptions, Timestamp timestamp) {
    Tag tag = tagLocationService.get(tagId);
    getFacade(tag).setQuality(tag, flagsToAdd, flagsToRemove, qualityDescriptions, timestamp);
  }

  
}
