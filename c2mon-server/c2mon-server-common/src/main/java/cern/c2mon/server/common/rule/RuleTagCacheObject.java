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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

/**
 * Cache object representing a rule in the server. Make sure to update the clone method if modifying the fields.
 *
 * @author Mark Brightwell
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RuleTagCacheObject extends AbstractTagCacheObject implements RuleTag {

    private static final long serialVersionUID = -3382383610136394447L;

    /**
     * Private class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleTagCacheObject.class);

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
        if (this.equipmentIds != null) {
            ruleTagCacheObject.equipmentIds = new HashSet<Long>();
            for (Long eqId : this.equipmentIds) {
                ruleTagCacheObject.equipmentIds.add(eqId);
            }
        }
        if (this.subEquipmentIds != null) {
          ruleTagCacheObject.subEquipmentIds = new HashSet<Long>();
          for (Long subEqId : this.subEquipmentIds) {
              ruleTagCacheObject.subEquipmentIds.add(subEqId);
          }
      }
        if (this.processIds != null) {
            ruleTagCacheObject.processIds = new HashSet<Long>();
            for (Long procId : this.processIds) {
                ruleTagCacheObject.processIds.add(procId);
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

    @Override
    public Timestamp getTimestamp() {
      return getCacheTimestamp();
    }

    @Override
    public final Collection<Long> getRuleInputTagIds() {
        Collection<Long> ruleCollection;
        if (ruleExpression != null) {
            ruleCollection = ruleExpression.getInputTagIds();
        } else {
            ruleCollection = Collections.emptyList();
        }
        return ruleCollection;
    }

    @Override
    public final Collection<Long> getCopyRuleInputTagIds() {
        Collection<Long> ruleCollection;
        if (ruleExpression != null) {
            ruleCollection = new ArrayList<>(ruleExpression.getInputTagIds());
        } else {
            ruleCollection = Collections.emptyList();
        }
        return ruleCollection;
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
                LOGGER.error("Exception caught in setting rule expression: unable to parse rule text: ", formatEx);
            }
        } else {
            throw new NullPointerException("Attempting to set RuleTag ruleText field to null!");
        }
    }

    @Override
    public Long getLowestProcessId() {
        if (!processIds.isEmpty()) {
            List<Long> sortedList = new ArrayList<Long>(processIds);
            Collections.sort(sortedList);
            return sortedList.get(0);
        } else
            return 0L;
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
