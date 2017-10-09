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

import java.sql.Timestamp;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.shared.rule.RuleExpression;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * Cache object representing a rule in the server. Make sure to update the clone method if modifying the fields.
 *
 * @author Mark Brightwell
 */
@Slf4j
public class RuleTagCacheObject extends AbstractTagCacheObject implements RuleTag, Cloneable {

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
     * Reference to all the Equipments providing tags for this rule.
     */
    private Set<Long> parentEquipments = new HashSet<>();

    /**
     * Reference to all the SubEquipments providing tags for this rule.
     */
    private Set<Long> parentSubEquipments = new HashSet<>();

    /**
     * Reference to all the Processes providing tags for this rule.
     */
    private Set<Long> parentProcesses = new HashSet<>();

    /**
     * Constructor used to return a cache object when the object cannot be found in the cache.
     *
     * @param id id of the Tag
     * @param name name of the Tag
     * @param datatype the datatype of the Tag value
     * @param mode the mode (TEST/OPER) the tag is configured in
     * @param ruleText the String with the rule specification
     */
    public RuleTagCacheObject(final Long id, final String name, final String datatype, final short mode,
            final String ruleText) {
        setId(id);
        setName(name);
        setDataType(datatype);
        setMode(mode);
        // TODO add quality init
        setRuleText(ruleText);
    }

    // TODO remove this constructor once fixed result maps
    public RuleTagCacheObject() {
        super();
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
    public RuleTagCacheObject clone() throws CloneNotSupportedException {
        RuleTagCacheObject ruleTagCacheObject = (RuleTagCacheObject) super.clone();
        if (this.parentEquipments != null) {
            ruleTagCacheObject.parentEquipments = new HashSet<Long>();
            for (Long eqId : this.parentEquipments) {
                ruleTagCacheObject.parentEquipments.add(eqId);
            }
        }
        if (this.parentSubEquipments != null) {
          ruleTagCacheObject.parentSubEquipments = new HashSet<Long>();
          for (Long subEqId : this.parentSubEquipments) {
              ruleTagCacheObject.parentSubEquipments.add(subEqId);
          }
      }
        if (this.parentProcesses != null) {
            ruleTagCacheObject.parentProcesses = new HashSet<Long>();
            for (Long procId : this.parentProcesses) {
                ruleTagCacheObject.parentProcesses.add(procId);
            }
        }
        ruleTagCacheObject.ruleExpression = null;
        if (this.ruleExpression != null && ruleText != null) {
          // TODO: Test correct clone support to all rule objects and replace then again the statement
//         ruleTagCacheObject.ruleExpression = (RuleExpression) this.ruleExpression.clone();
          ruleTagCacheObject.setRuleText(ruleText);
        }

        return ruleTagCacheObject;
    }

  /**
   * Returns the RuleExpression that encodes the logic behind the rule.
   * May be null if rule parsing failed when loading the rule.
   *
   * @return the expression containing the rule logic
   */
    public final RuleExpression getRuleExpression() {
        return ruleExpression;
    }

  /**
   * Returns the datatags USED by this rule. If the rule expression
   * is null returns an empty collection.
   * @return the ids of Tag used in this rule
   */
    public final Collection<Long> getRuleInputTagIds() {
        Collection<Long> ruleCollection;
        if (ruleExpression != null) {
            ruleCollection = ruleExpression.getInputTagIds();
        } else {
            ruleCollection = Collections.emptyList();
        }
        return ruleCollection;
    }

  /**
   * Returns an own copy of the tags used by this rule.
   * @return ids of tags used in this rule
   */
    public final Collection<Long> getCopyRuleInputTagIds() {
        Collection<Long> ruleCollection;
        if (ruleExpression != null) {
            ruleCollection = new ArrayList<Long>(ruleExpression.getInputTagIds());
        } else {
            ruleCollection = Collections.emptyList();
        }
        return ruleCollection;
    }

    @Override
    public final String getRuleText() {
        return this.ruleText;
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
                this.ruleExpression = RuleExpression.createExpression(this.ruleText);
            } catch (RuleFormatException formatEx) {
                log.error("Exception caught in setting rule expression: unable to parse rule text: ", formatEx);
            }
        } else {
            throw new NullPointerException("Attempting to set RuleTag ruleText field to null!");
        }
    }

  /**
   * Returns the cache timestamp of the current value held by the rule.
   *
   * @return the cache timestamp of the rule value
   */
    @Override
    public Timestamp getTimestamp() {
        return getCacheTimestamp();
    }

  /**
   * Sets the collection of Equipments whose tags
   * are used in this rule.
   *
   * @param equipmentIds the new collection that will replace the old
   */
    public void setEquipmentIds(Set<Long> equipmentIds) {
        this.parentEquipments = equipmentIds;
    }

    @Override
    public Set<Long> getEquipmentIds() {
        return parentEquipments;
    }

  /**
   * Sets the collection of Processes whose tags
   * are used in this rule.
   *
   * @param processIds the new collection that will replace the old
   */
    public void setProcessIds(Set<Long> processIds) {
        this.parentProcesses = processIds;
    }

    @Override
    public Set<Long> getProcessIds() {
        return parentProcesses;
    }

  /**
   * Returns the process with the lowest id from the process id
   * list (sort each time). Used for creating cache object.
   * Returns 0 if list is empty.
   *
   * @return the id of the process
   */
    public Long getLowestProcessId() {
        if (!parentProcesses.isEmpty()) {
            List<Long> sortedList = new ArrayList<Long>(parentProcesses);
            Collections.sort(sortedList);
            return sortedList.get(0);
        } else
            return 0L;
    }

  /**
   * Sets the collection of SubEquipments whose tags
   * are used in this rule.
   *
   * @param subEquipmentIds the new collection that will replace the old
   */
    public void setSubEquipmentIds(Set<Long> subEquipmentIds) {
        this.parentSubEquipments = subEquipmentIds;
    }

    public Set<Long> getSubEquipmentIds() {
      return parentSubEquipments;
    }

    @Override
    public String toString() {
      StringBuffer str = new StringBuffer();

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
      }
      else {
        str.append("\t0\tOK");
      }
      
      if (getValueDescription() != null) {
        str.append('\t');
        // remove all \n and replace all \t characters of the value description string
        str.append(getValueDescription().replace("\n", "").replace("\t", "  ") );
      }
      
      return str.toString();
    }
}
