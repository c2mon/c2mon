/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.common.rule;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.rule.RuleExpression;

/**
 * Interface implemented by the cache representation of a rule
 * in the TIM server. This interface should only be used within 
 * the server.
 * @author mbrightw
 *
 */
public interface RuleTag extends Tag, Cacheable {

  /**
   * Returns the datatags USED by this rule. If the rule expression
   * is null returns an empty collection.
   * @return the ids of Tag used in this rule
   */
  Collection<Long> getRuleInputTagIds();

  /**
   * Returns the RuleExpression that encodes the logic behind the rule.
   * May be null if rule parsing failed when loading the rule.
   * 
   * @return the expression containing the rule logic
   */
  RuleExpression getRuleExpression();
  
  /**
   * Returns the cache timestamp of the current value held by the rule.
   * 
   * @return the cache timestamp of the rule value
   */
  Timestamp getTimestamp();

  /**
   * Returns the text form of the rule expression.
   * 
   * @return the expression in text format as stored in the DB
   */
  String getRuleText();

  /**
   * Sets the collection of Equipments whose tags
   * are used in this rule.
   * 
   * @param equipmentIds the new collection that will replace the old
   */
  void setEquipmentIds(Set<Long> equipmentIds);

  /**
   * Sets the collection of Processes whose tags
   * are used in this rule.
   * 
   * @param processIds the new collection that will replace the old
   */
  void setProcessIds(Set<Long> processIds);

  /**
   * Returns an own copy of the tags used by this rule.
   * @return ids of tags used in this rule
   */
  Collection<Long> getCopyRuleInputTagIds();

  /**
   * Returns the process with the lowest id from the process id
   * list (sort each time). Used for creating cache object.
   * Returns 0 if list is empty.
   * 
   * @return the id of the process
   */
  Long getLowestProcessId();
  

}
