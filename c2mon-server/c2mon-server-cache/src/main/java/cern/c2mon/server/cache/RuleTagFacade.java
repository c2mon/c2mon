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
package cern.c2mon.server.cache;

import java.sql.Timestamp;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;

/**
 * Bean that should be used to access and update rule cache objects.
 * 
 * <p>This methods are all thread-safe. They perform the required synchronization,
 * including for a Terracotta distributed cache.
 * 
 * @author Mark Brightwell
 *
 */
public interface RuleTagFacade extends CommonTagFacade<RuleTag> {

  /**
   * Sets the RuleTag quality to good and updates the rule with the new
   * value, value description and timestamp.
   * 
   * <p>If the update causes no changes, the cache object is not updated (see filterout method in AbstracTagFacade).
   * 
   * <p>Notifiers listeners if updated and logs the rule to the rule log file.
   * 
   * @param id of the rule to update
   * @param value new value
   * @param valueDescription new value description
   * @param timestamp new timestamp
   */
  void updateAndValidate(Long id, Object value, String valueDescription, Timestamp timestamp);

  /**
   * Sets the parent process and equipment fields for RuleTags.
   * 
   * @param ruleTag the RuleTag for which the fields should be set
   */
  void setParentSupervisionIds(RuleTagCacheObject ruleTag);
  
  /**
   * Sets the parent process and equipment fields for RuleTags.
   * 
   * @param ruleTagId the id of the RuleTag for which the fields should be set
   */
  void setParentSupervisionIds(Long ruleTagId);
}
