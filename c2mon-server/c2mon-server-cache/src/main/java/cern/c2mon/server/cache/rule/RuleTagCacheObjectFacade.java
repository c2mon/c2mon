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
package cern.c2mon.server.cache.rule;

import java.sql.Timestamp;

import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.tag.AbstractTagObjectFacade;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

@Service
public class RuleTagCacheObjectFacade extends AbstractTagObjectFacade<RuleTag> {

  public void update(final RuleTag ruleTag, final Object value, final String valueDesc, final Timestamp timestamp) {           
    updateValue(ruleTag, value, valueDesc);    
    setTimestamp(ruleTag, timestamp);                  
  }

  /**
   * Add the invalidation flag to this RuleTag together with the associated description.
   */
  public void invalidate(RuleTag ruleTag, TagQualityStatus status, String statusDescription, Timestamp timestamp) {
    updateQuality(ruleTag, status, statusDescription);
    setTimestamp(ruleTag, timestamp);
    
  }
  
  private void setTimestamp(RuleTag ruleTag, Timestamp timestamp) {
    // TODO change this to modifying the timestamps if they are not null, rather than putting a new object
    RuleTagCacheObject ruleTagCacheObject = (RuleTagCacheObject) ruleTag;    
    ruleTagCacheObject.setCacheTimestamp(timestamp);
  }
  
}
