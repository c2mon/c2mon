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
package cern.c2mon.server.common.rule;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.rule.RuleExpression;

import java.sql.Timestamp;
import java.util.Set;

/**
 * Interface implemented by the cache representation of a rule
 * in the TIM server. This interface should only be used within
 * the server.
 * @author mbrightw
 *
 */
public interface RuleTag extends Tag {

  /**
   * Returns the datatags USED by this rule. If the rule expression
   * is null returns an empty collection.
   * @return the ids of Tag used in this rule
   */
  Set<Long> getRuleInputTagIds();

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
  @Override
  Timestamp getTimestamp();

  Timestamp getEvalTimestamp();

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
   * Sets the collection of SubEquipments whose tags
   * are used in this rule.
   *
   * @param subEquipmentIds the new collection that will replace the old
   */
  void setSubEquipmentIds(Set<Long> subEquipmentIds);

  /**
   * Sets the collection of Processes whose tags
   * are used in this rule.
   *
   * @param processIds the new collection that will replace the old
   */
  void setProcessIds(Set<Long> processIds);

  /**
   * Returns the process with the lowest id from the process id
   * list (sort each time). Used for creating cache object.
   * Returns 0 if list is empty.
   *
   * @return the id of the process
   */
  Long getLowestProcessId();

}
