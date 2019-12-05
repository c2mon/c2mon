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
package cern.c2mon.server.configuration.handler.impl;

import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.DataTagConfigHandler;
import cern.c2mon.server.configuration.handler.RuleTagConfigHandler;
import cern.c2mon.server.configuration.handler.TagConfigHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Gateway bean used to call the TagConfigHandler methods on the
 * correct ConfigHandlerBean, according to the type of the 
 * passed tag.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class TagConfigGateway implements TagConfigHandler<Tag> {

  //autowired fields as circular dependencies (e.g. RuleTagConfigHandler uses config gateway
  // to run common methods)
  @Autowired
  private DataTagConfigHandler dataTagConfigHandler;
  
  @Autowired  
  private ControlTagConfigHandler controlTagConfigHandler;
  
  @Autowired
  private RuleTagConfigHandler ruleTagConfigHandler;
  
  private TagLocationService tagLocationService;
  
  @Autowired
  public TagConfigGateway(final TagLocationService tagLocationService) {
    super();    
    this.tagLocationService = tagLocationService;
  }

  /**
   * Returns the required config handler for this type
   * of Tag.
   * 
   * (Cast OK. Must keep ControlTag first as extends DataTag.)
   * @param <T>
   * @param tag
   * @return
   */
  @SuppressWarnings("unchecked")
  private <T extends Tag> TagConfigHandler<T> getFacade(T tag) {
    if (tag instanceof RuleTag) {
      return (TagConfigHandler<T>) ruleTagConfigHandler;
    } else if (tag instanceof ControlTag) {
      return (TagConfigHandler<T>) controlTagConfigHandler;
    } else {       
      return (TagConfigHandler<T>) dataTagConfigHandler;
    }
  }
  
  
  @Override
  public void addAlarmToTag(Long tagId, Long alarmId) {
    Tag tag = tagLocationService.get(tagId);
    getFacade(tag).addAlarmToTag(tagId, alarmId);    
  }

  @Override
  public void addRuleToTag(Long tagId, Long ruleId) {
    Tag tag = tagLocationService.get(tagId);
    getFacade(tag).addRuleToTag(tagId, ruleId);      
  }

  @Override
  public void removeAlarmFromTag(Long tagId, Long alarmId) {
    Tag tag = tagLocationService.get(tagId);
    getFacade(tag).removeAlarmFromTag(tagId, alarmId);    
  }

  @Override
  public void removeRuleFromTag(Long tagId, Long ruleId) {
    Tag tag = tagLocationService.get(tagId);
    getFacade(tag).removeRuleFromTag(tagId, ruleId);
  }
}
