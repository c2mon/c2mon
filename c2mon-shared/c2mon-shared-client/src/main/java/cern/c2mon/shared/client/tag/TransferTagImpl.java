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
package cern.c2mon.shared.client.tag;

import java.sql.Timestamp;
import java.util.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.rule.RuleExpression;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * This class implements the <code>TransferTag</code> interface which defines
 * the transport object that is transfered to the client layer for initializing
 * a given tag. Furthermore it is used to communicate configuration changes.
 *
 * @author Matthias Braeger
 */
@Data
public final class TransferTagImpl extends TransferTagValueImpl implements TagUpdate {

  /**
   * Containing all process id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple processes (DAQs).
   */
  @Size(min = 1)
  private final Collection<Long> processIds = new HashSet<Long>();

  /**
   * Containing all equipment id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple equipments.
   */
  @Size(min = 1)
  private final Collection<Long> equipmentIds = new HashSet<Long>();

  /**
   * Containing all sub equipment id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple equipments.
   */
  @Size(min = 0)
  private final Collection<Long> subEquipmentIds = new HashSet<Long>();

  /**
   * String representation of the JMS destination where
   * <code>TransferTagValue</code> is published on change.
   */
  @NotNull
  private String topicName;

  /** The unique name of the tag */
  @NotNull
  private String tagName;

  /** Unit of the tag */
  private String unit = null;

  /** In case of a rule tag this field should not be null */
  private String ruleExpressionStr = null;

  /** In case of a Control tag update, this field is set to <code>true</code> */
  private boolean controlTag = false;

  /** In case of an Alive Control tag update, this field is set to <code>true</code> */
  private boolean aliveTag = false;

  /**
   * Metadata according to the tag in this class.
   */
  @NotNull
  private Map<String, Object> metadata = new HashMap<>();

  /**
   * Private default constructor needed for JSON
   */
  @SuppressWarnings("unused")
  private TransferTagImpl() {
    this(null, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Default Constructor
   *
   * @param pTagId The unique tag id
   * @param pTagValue The current value of the tag
   * @param pTagValueDescription The description of the current value
   * @param pTagQuality The quality of the tag
   * @param pMode The mode of the tag, either OPERATIONAL, TEST or MAINTENANCE
   * @param pSourceTimestamp The source timestamp that indicates when the value change was generated
   * @param pDaqTimestamp The DAQ timestamp that indicates when the value change message has been sent from the DAQ
   * @param pServerTimestamp The server timestamp that indicates when the change message passed the server
   * @param pDescription The current tag value description
   * @param pTagName The unique name of the tag
   * @param pTopicName JMS destination where <code>TransferTagValue</code> is published on change.
   */
  public TransferTagImpl(final Long pTagId,
                         final Object pTagValue,
                         final String pTagValueDescription,
                         final DataTagQualityImpl pTagQuality,
                         final TagMode pMode,
                         final Timestamp pSourceTimestamp,
                         final Timestamp pDaqTimestamp,
                         final Timestamp pServerTimestamp,
                         final String pDescription,
                         final String pTagName,
                         final String pTopicName) {
    super(pTagId, pTagValue, pTagValueDescription, pTagQuality, pMode, pSourceTimestamp, pDaqTimestamp, pServerTimestamp, pDescription);

    tagName = pTagName;
    topicName = pTopicName;
  }

  @Override
  @JsonIgnore
  public String getName(){
    return this.tagName;
  }

  @Override
  @JsonIgnore
  public String getRuleExpression(){
    return this.ruleExpressionStr;
  }

  /**
   * Adds the equipment id as dependency to this tag
   * @param equipmentId The equipment id
   * @return <code>true</code>, if the equipment id was added, else <code>false</code>
   */
  public boolean addEquipmentId(final Long equipmentId) {
    if (equipmentId != null && equipmentId > 0) {
      return equipmentIds.add(equipmentId);
    }

    return false;
  }

  /**
   * Adds all equipment id's of that list as dependency to this tag
   * @param equipmentIds List of equipment id's
   */
  public void addEquipmentIds(final Collection<Long> equipmentIds) {
    for (Long equipmentId : equipmentIds) {
      addEquipmentId(equipmentId);
    }
  }

  /**
   * @return A copy of the list of equipment id's
   */
  @Override
  public Collection<Long> getEquipmentIds() {
    return new ArrayList<Long>(equipmentIds);
  }

  @Override
  public String getType() {
    return this.getValueClassName();
  }

  /**
   * Adds the sub equipment id as dependency to this tag
   * @param subEquipmentId The sub equipment id
   * @return <code>true</code>, if the sub equipment id was added, else <code>false</code>
   */
  public boolean addSubEquipmentId(final Long subEquipmentId) {
    if (subEquipmentId != null && subEquipmentId > 0) {
      return subEquipmentIds.add(subEquipmentId);
    }

    return false;
  }

  /**
   * Adds all sub equipment id's of that list as dependency to this tag
   * @param subEquipmentIds List of sub equipment id's
   */
  public void addSubEquipmentIds(final Collection<Long> subEquipmentIds) {
    for (Long subEquipmentId : subEquipmentIds) {
      addSubEquipmentId(subEquipmentId);
    }
  }

  /**
   * Adds the process id as dependency to this tag
   * @param processId The process id
   * @return <code>true</code>, if the process id was added, else <code>false</code>
   */
  public boolean addProcessId(final Long processId) {
    if (processId != null && processId > 0) {
      return processIds.add(processId);
    }

    return false;
  }

  /**
   * Adds all process id's of that list as dependency to this tag
   * @param processIds List of process id's
   */
  public void addProcessIds(final Collection<Long> processIds) {
    for (Long processId : processIds) {
      addProcessId(processId);
    }
  }

  /**
   * @return A copy of the list of process id's
   */
  @Override
  public Collection<Long> getProcessIds() {
    return new ArrayList<Long>(processIds);
  }

  /**
   * In case of a rule tag this value shall be set.
   * @param ruleExpression The rule which was used to compute the tag value
   */
  public void defineRuleExpression(final RuleExpression ruleExpression) {
    this.ruleExpressionStr = ruleExpression.getExpression();
  }

  /**
   * @param controlTag the controlTag flag is set to <code>true</code>,
   *        if the update is from a Alive-, CommFault- or Status tag.
   *        In case this flag is set to <code>false</code>, the aliveTag
   *        flag will automatically also be set to <code>false</code>.
   */
  public final void setControlTagFlag(boolean controlTag) {
    this.controlTag = controlTag;

    if (!controlTag) {
      this.aliveTag = false;
    }
  }

  /**
   * @param aliveTag the aliveTag flag is set to <code>true</code>,
   *        if the update is from an Alive Control tag. Please note,
   *        that for <code>true</code>, the controlTag flag will automatically
   *        also be set to <code>true</code>.
   */
  public final void setAliveTagFlag(boolean aliveTag) {
    this.aliveTag = aliveTag;

    if (aliveTag) {
      this.controlTag = true;
    }
  }

  public void setTopicName(String topicName) {
    this.topicName = topicName;
  }
}
