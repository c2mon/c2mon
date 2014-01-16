/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2005-2011 CERN. This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.common.rule;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.shared.rule.RuleExpression;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * Cache object representing a rule in the server. Make sure to update the clone method if modifying the fields.
 * 
 * @author Mark Brightwell
 */
public class RuleTagCacheObject extends AbstractTagCacheObject implements RuleTag, Cloneable {

    private static final long serialVersionUID = -3382383610136394447L;

    /**
     * Private class logger.
     */
    private static final Logger LOGGER = Logger.getLogger(RuleTagCacheObject.class);

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
    private Set<Long> parentEquipments = new HashSet<Long>();

    /**
     * Reference to all the Processes providing tags for this rule.
     */
    private Set<Long> parentProcesses = new HashSet<Long>();

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

    @Override
    public final RuleExpression getRuleExpression() {
        return ruleExpression;
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
            ruleCollection = new ArrayList<Long>(ruleExpression.getInputTagIds());
        } else {
            ruleCollection = Collections.emptyList();
        }
        return ruleCollection;
    }

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
                LOGGER.error("Exception caught in setting rule expression: unable to parse rule text: ", formatEx);
            }
        } else {
            throw new NullPointerException("Attempting to set RuleTag ruleText field to null!");
        }

    }

    @Override
    public Timestamp getTimestamp() {
        return getCacheTimestamp();
    }

    @Override
    public void setEquipmentIds(Set<Long> parentEquipments) {
        this.parentEquipments = parentEquipments;
    }

    @Override
    public Set<Long> getEquipmentIds() {
        return parentEquipments;
    }

    @Override
    public void setProcessIds(Set<Long> parentProcesses) {
        this.parentProcesses = parentProcesses;
    }

    @Override
    public Set<Long> getProcessIds() {
        return parentProcesses;
    }

    @Override
    public Long getLowestProcessId() {
        if (!parentProcesses.isEmpty()) {
            List<Long> sortedList = new ArrayList<Long>(parentProcesses);
            Collections.sort(sortedList);
            return sortedList.get(0);
        } else
            return 0L;
    }
}
