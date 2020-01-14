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

import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.shared.rule.RuleExpression;
import cern.c2mon.shared.rule.RuleFormatException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.*;

/**
 * Cache object representing a rule in the server. Make sure to update the clone method if modifying the fields.
 *
 * @author Mark Brightwell
 */
@Slf4j
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class RuleTagCacheObject extends AbstractTagCacheObject implements RuleTag {

  private static final long serialVersionUID = -3382383610136394447L;

  /**
   * The rule as a String. Should never be null for a RuleTag (set as empty String if necessary).
   */
  private String ruleText;

  /**
   * The rule as a RuleExpression.
   */
  private RuleExpression ruleExpression;

  /**
   * Reference to all the parent Equipments providing tags for this rule.
   */
  private Set<Long> equipmentIds = new HashSet<>();

  /**
   * Reference to all the parent SubEquipments providing tags for this rule.
   */
  private Set<Long> subEquipmentIds = new HashSet<>();

  /**
   * Reference to all the parent Processes providing tags for this rule.
   */
  private Set<Long> processIds = new HashSet<>();

  private Timestamp evalTimestamp;

  /**
   * Constructor used to return a cache object when the object cannot be found in the cache.
   *
   * @param id       id of the Tag
   * @param name     name of the Tag
   * @param datatype the datatype of the Tag value
   * @param mode     the mode (TEST/OPER) the tag is configured in
   * @param ruleText the String with the rule specification
   */
  public RuleTagCacheObject(final Long id, final String name, final String datatype, final short mode,
                            final String ruleText) {
    this(id);
    setName(name);
    setDataType(datatype);
    setMode(mode);
    // TODO add quality init
    setRuleText(ruleText);
  }

  /**
   * Used for config loader.
   *
   * @param id
   */
  public RuleTagCacheObject(Long id) {
    super(id);
  }

  /**
   * Clone implementation.
   *
   * @throws CloneNotSupportedException
   */
  @Override
  public RuleTagCacheObject clone() {
    RuleTagCacheObject otherObjeect = (RuleTagCacheObject) super.clone();
    if (equipmentIds != null) {
      otherObjeect.equipmentIds = new HashSet<>();
      otherObjeect.equipmentIds.addAll(this.equipmentIds);
    }
    if (subEquipmentIds != null) {
      otherObjeect.subEquipmentIds = new HashSet<>();
      otherObjeect.subEquipmentIds.addAll(this.subEquipmentIds);
    }
    if (processIds != null) {
      otherObjeect.processIds = new HashSet<>();
      otherObjeect.processIds.addAll(this.processIds);
    }
    otherObjeect.ruleExpression = null;
    if (ruleExpression != null && ruleText != null) {
      // TODO: Test correct clone support to all rule objects and replace then again the statement
//         otherObjeect.ruleExpression = (RuleExpression) this.ruleExpression.clone();
      otherObjeect.setRuleText(ruleText);
    }

    return otherObjeect;
  }

  @Override
  // TODO (Alex) Kept only because a vast number of consumers call this method, change them to call eval/cacheTimestamp eventually
  public Timestamp getTimestamp() {
    return evalTimestamp != null
      ? evalTimestamp
      : getCacheTimestamp();
  }

  @Override
  public final Collection<Long> getRuleInputTagIds() {
    return ruleExpression != null
      ? ruleExpression.getInputTagIds()
      : Collections.emptyList();
  }

  @Override
  public final Collection<Long> getCopyRuleInputTagIds() {
    return (ruleExpression != null)
      ? new ArrayList<>(ruleExpression.getInputTagIds())
      : Collections.emptyList();
  }

  /**
   * Also attempts to set the rule expression field (stays null if fails and logs an error).
   *
   * @param ruleText the ruleText to set
   */
  public void setRuleText(String ruleText) {
    this.ruleText = ruleText;

    if (this.ruleText != null) {
      try {
        ruleExpression = RuleExpression.createExpression(this.ruleText);
      } catch (RuleFormatException formatEx) {
        log.error("Exception caught in setting rule expression: unable to parse rule text: ", formatEx);
      }
    } else {
      throw new NullPointerException("Attempting to set RuleTag ruleText field to null!");
    }
  }

  @Override
  public Long getLowestProcessId() {
    return processIds.stream().sorted().findFirst().orElse(0L);
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();

    str.append(getId());
    str.append('\t');
    str.append(getName());
    str.append('\t');
    str.append(getTimestamp());
    str.append('\t');
    str.append(getValue());
    str.append('\t');
    str.append(getDataType());

    if (!isValid()) {
      str.append('\t');
      str.append(getDataTagQuality().getInvalidQualityStates());
    } else {
      str.append("\t0\tOK");
    }

    if (getValueDescription() != null) {
      str.append('\t');
      // remove all \n and replace all \t characters of the value description string
      str.append(getValueDescription().replace("\n", "").replace("\t", "  "));
    }

    return str.toString();
  }
}
