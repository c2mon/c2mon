package cern.c2mon.shared.client.tag;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.rule.RuleExpression;

/**
 * This class implements the <code>TransferTag</code> interface which defines
 * the transport object that is transfered to the client layer for initializing
 * a given tag. Furthermore it is used to communicate configuration changes.
 *
 * @author Matthias Braeger
 */
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
   * String representation of the JMS destination where 
   * <code>TransferTagValue</code> is published on change.
   */
  @NotNull
  private final String topicName;
  
  /** The unique name of the tag */
  @NotNull
  private final String tagName;
  
  /** Unit of the tag */
  private String unit = null;
  
  /** In case of a rule tag this field should not be null */ 
  private String ruleExpressionStr = null;

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
  public void addEquimpmentIds(final Collection<Long> equipmentIds) {
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
   * @return A <code>String</code> representation of the JMS destination where the DataTag 
   *         is published on change.
   */
  @Override
  public String getTopicName() {
    return topicName;
  }

  @Override
  public String getName() {
    return tagName;
  }

  /**
   * In case of a rule tag this value shall be set.
   * @param ruleExpression The rule which was used to compute the tag value
   */
  public void setRuleExpression(final RuleExpression ruleExpression) {
    ruleExpressionStr = ruleExpression.getExpression();
  }
  
  @Override
  public String getRuleExpression() {
    return ruleExpressionStr;
  }

  /**
   * @param pUnit The value unit description of the tag
   */
  public void setUnit(final String pUnit) {
    unit = pUnit;
  }

  @Override
  public String getUnit() {
    return unit;
  }
  
  /**
   * Deserialized the JSON string into a <code>TransferTag</code> object instance
   * @param json A JSON string representation of a <code>TransferTagImpl</code> class
   * @return The deserialized <code>TransferTag</code> instance of the JSON message
   */
  public static TagUpdate fromJson(final String json) {
    return getGson().fromJson(json, TransferTagImpl.class);
  }
}
