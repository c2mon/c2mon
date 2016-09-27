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
package cern.c2mon.client.core.tag;

import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cern.c2mon.shared.client.expression.Expression;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.rule.RuleEvaluationException;
import cern.c2mon.shared.rule.RuleExpression;

/**
 * This class represents a client rule object which subscribes itself to the
 * data tags used within the rule expression. Every time it receives an
 * update message it recomputes the rule result and the overall quality.<br>
 *
 * You can register a <code>ClientDataTagValueUpdateListener</code> to get
 * noticed when the value of or quality of this client rule changed.
 *
 * @author Matthias Braeger
 */
@Slf4j
public class ClientRuleTag<T> implements Tag, BaseTagListener {

  /** The rule expression of the client rule */
  private final RuleExpression rule;

  /** The result type of the rule */
  private final Class<T> resultType;

  /** Indicates when the client rule was evaluated for the last time */
  private Timestamp timestamp = null;

  /** The quality of the client rule which is computed from all input tags */
  private DataTagQuality ruleQuality = new DataTagQualityImpl();

  /** The rule result */
  private T ruleResult = null;

  /** Identifier for a ClientRuleTag */
  private Long id;

  /** List of unique update listeners */
  private final List<BaseTagListener> listeners = new ArrayList<>();
  ;

  /** The actual list of rule input values, that was received by onUpdate() method */
  private final Map<Long, Tag> ruleInputValues = new Hashtable<Long, Tag>();

  /** Thread synchronization lock for the rule input Values map */
  private final ReentrantReadWriteLock ruleMapLock = new ReentrantReadWriteLock();

  /** Thread synchronization lock for listeners list */
  private final ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();

  /** The name of the this rule tag */
  private String name = "UNKNOWN";

  /** The computed rule mode */
  private TagMode ruleMode = TagMode.OPERATIONAL;

  /** Default error message in case the Rule expression cannot be evaluated. */
  private final static String RULE_ERROR_MESSAGE = "Errors occurred while evaluating the Rule Expression.";

  /**
   * If on of the tags that is used to compute the rule is simulated, then
   * the <code>ClientRuleTag</code> itself is marked as simulated.
   */
  private boolean simulated = false;

  /** The rule tag description */
  private String description = "";

  /** The value description */
  private String valueDescription = "";

  /** Empty collection */
  private static final Collection<Long> EMPTY_LONG_LIST = new ArrayList<Long>();

  /**
   * In case the rule expression could not be evaluated, an error message describing the problem.
   * Null otherwise.
   */
  private String ruleError;

  /**
   * Empty alarm collection instance that is returned, if another class
   * wants to know whether there are alarms registered for this
   * <code>Tag</code> instance. By definition a client rule cannot
   * have any related alarms.
   */
  private static final Collection<AlarmValue> EMPTY_ALARM_LIST = new ArrayList<AlarmValue>();

  /**
   * Default Constructor<br>
   * Do not forget to unsubscribe from the input tags once you not need anymore
   * this <code>ClientRuleTag</code> instance.
   *
   * @param pRule       The client rule expression
   * @param pResultType The result type of the rule expression
   * @see ClientRuleTag#unsubscribe()
   */
  public ClientRuleTag(final RuleExpression pRule, Class<T> pResultType) {
    if (pRule == null || pResultType == null) {
      throw new NullPointerException("The arguments cannot be null");
    }
    this.rule = pRule;
    this.resultType = pResultType;
    this.id = new Long(-1);
  }

  /**
   * Forces an evaluation of the ClientRuleTag and informs then all registered
   * listeners.
   */
  public synchronized void forceUpdate() {
    computeRuleResult();
    fireUpdateReceivedEvent();
  }

  /**
   * @return Rule id: used to be -1 as a convention for all the ClientRuleTags.
   * Default is still -1, but can now be any minus (-) value,
   * to differentiate between RuleTags.
   */
  @Override
  public Long getId() {
    return id;
  }

  /**
   * Use this method to set Id for the ClientRuleTag.
   *
   * Rule id used to be -1 as a convention for all the ClientRuleTags.
   *
   * Default is still -1, but can now be any minus (-) value,
   * to differentiate between RuleTags.
   *
   * Client Rule Tag id's can only be a minus value.
   * Positive values are ignored.
   */
  public void setId(final Long id) {

    if (id >= 0) {
      return;
    }
    this.id = id;
  }

  /**
   * @return the timestamp
   */
  @Override
  public final Timestamp getTimestamp() {
    if (this.timestamp == null) {
      return new Timestamp(0);
    }
    return timestamp;
  }

  /**
   * Check whether this DataTag is the result of a rule. Rules are internal TIM
   * tags as opposed to regular DataTags which are acquired from an external
   * source.
   *
   * @return true if the DataTag is the result of a rule.
   */
  @Override
  public boolean isRuleResult() {
    return true;
  }

  /**
   * Returns DataTagQuality object of the client rule
   *
   * @return the DataTagQuality object for this client rule.
   */
  @Override
  public DataTagQuality getDataTagQuality() {
    return this.ruleQuality;
  }

  /**
   * Get the RuleExpression associated with this class.
   *
   * @return the RuleExpression associated with this class
   */
  @Override
  public RuleExpression getRuleExpression() {
    return rule;
  }

  /**
   * Returns the rule result. Value should be casted to the correct type
   * The tag type is available with the getType() method
   *
   * @return the tag value
   * @see #getType
   */
  @Override
  public T getValue() {
    return ruleResult;
  }

  /**
   * Returns the type of the tagValue attribute
   *
   * @return the class of the tag value
   * @see #getValue
   */
  @Override
  public final Class<T> getType() {
    return resultType;
  }

  @Override
  public TypeNumeric getTypeNumeric() {
    Class<?> type = getType();
    if (type != null) {
      int typeNumeric = type.hashCode();
      for (TypeNumeric t : TypeNumeric.values()) {
        if (t.getCode() == typeNumeric) {
          return t;
        }
      }
    }

    return TypeNumeric.TYPE_UNKNOWN;
  }

  /**
   * Evaluates the rule and the quality of the rule and sets a new timestamp
   */
  private void computeRuleResult() {
    DataTagQuality newRuleQuality = new DataTagQualityImpl();
    newRuleQuality.validate();

    this.simulated = false; // reset simulation flag
    TagMode newRuleMode = TagMode.OPERATIONAL;

    if (rule != null) {
      ruleMapLock.readLock().lock();
      try {
        if (ruleInputValues.size() == rule.getInputTagIds().size()) {
          // Iterate of input tags and compute the state of the client rule
          for (Tag inputValue : ruleInputValues.values()) {
            // compute simulated flag
            this.simulated |= inputValue.isSimulated();
            // Compute rule mode
            switch (inputValue.getMode()) {
              case TEST:
                if (!newRuleMode.equals(TagMode.MAINTENANCE)) {
                  newRuleMode = TagMode.TEST;
                }
                break;
              case MAINTENANCE:
                newRuleMode = TagMode.MAINTENANCE;
                break;
              default:
                // Do nothing
            }

          } // end of for loop

          // Set the rule quality
          this.ruleQuality = newRuleQuality;
          // Set new rule mode
          this.ruleMode = newRuleMode;

          try {
            this.ruleResult = rule.evaluate(new Hashtable<Long, Object>(ruleInputValues), resultType);
          } catch (RuleEvaluationException e) {
            log.debug("computeRule() - \"" + rule.getExpression() + "\" is Invalid.", e);

            ruleError = null;
            this.ruleQuality = getInvalidTagQuality();
            this.ruleResult = rule.forceEvaluate(new Hashtable<Long, Object>(ruleInputValues), resultType);
          } catch (Exception e) {
            this.ruleQuality.setInvalidStatus(TagQualityStatus.UNKNOWN_REASON, RULE_ERROR_MESSAGE);
            ruleError = e.getMessage();
            log.debug("computeRule() - \"" + rule.getExpression() + "\" could not be evaluated.", e);

            this.ruleResult = rule.forceEvaluate(new Hashtable<Long, Object>(ruleInputValues), resultType);
          }
          // Update the time stamp of the ClientRuleTag
          this.timestamp = new Timestamp(System.currentTimeMillis());
        }
      } finally {
        ruleMapLock.readLock().unlock();
      }
    }
  }

  /**
   * In case a ClientRule tag is Invalid, the invalidity can be a result of multiple
   * invalid Datatags that belong to the Client rule.
   *
   * @return an overall Datatag quality from all the Tags belonging to this rule
   *
   * IMPORTANT! ->
   * This should only be called if we know that the Rule is Invalid.
   *
   * This is because a rule can be VALID, even though it contains INVALID tags. In such a case
   * calling this method will give the wrong result..
   * @see https://issues.cern.ch/browse/TIMS-833
   */
  private DataTagQuality getInvalidTagQuality() {

    DataTagQuality invalidRuleQuality = new DataTagQualityImpl();
    invalidRuleQuality.validate();

    for (Tag inputValue : ruleInputValues.values()) {
      // Check, if value tag is valid or not
      if (!inputValue.isValid()) {
        // Add Invalidations flags to the the rule
        Map<TagQualityStatus, String> qualityStatusMap = inputValue.getDataTagQuality().getInvalidQualityStates();
        for (Entry<TagQualityStatus, String> entry : qualityStatusMap.entrySet()) {
          invalidRuleQuality.addInvalidStatus(entry.getKey(), entry.getValue());
        }
      }
    }
    return invalidRuleQuality;
  }


  /**
   * @return In case the rule expression could not be evaluated, the error message.
   * Returns null if the rule was evaluated without problems.
   */
  public String getRuleError() {
    return ruleError;
  }

  /**
   * Sets the client rule tag name.
   *
   * @param pName The name of the client rule tag
   */
  public void setName(final String pName) {
    this.name = pName;
  }

  /**
   * Returns the rule tag name or <code>UNKNOWN</code>,
   * if not set explicitly.
   *
   * @return The rule tag name or <code>UNKNOWN</code>,
   * if not set explicitly.
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Sets the client rule tag description
   *
   * @param pDescription The description of the client rule
   */
  public void setDescription(final String pDescription) {
    this.description = pDescription;
  }

  @Override
  public String getDescription() {
    if (this.description == null || this.description.equalsIgnoreCase("")) {
      return "Client rule tag";
    }

    return "Client rule tag: " + this.description;
  }

  /**
   * Sets the value Description
   *
   * @param pValueDescription the value description
   */
  public void setValueDescription(final String pValueDescription) {
    this.valueDescription = pValueDescription;
  }

  @Override
  public String getValueDescription() {
    if (this.valueDescription == null || this.valueDescription.equalsIgnoreCase("")) {
      return "Client rule tag result";
    }
    return this.valueDescription;
  }

  @Override
  public Map<String, Object> getMetadata() {
    return Collections.emptyMap();
  }

  @Override
  public Collection<Expression> getExpressions() {
    return null;
  }

  @Override
  public String getUnit() {
    return "";
  }

  /**
   * You shall call this method at the end of the lifecycle of this
   * instance in order to de-register from all input Tags of this
   * client rule and to clear the list of registered listeners.
   */
  public void unsubscribe() {
    try {
      listenersLock.writeLock().lock();
      listeners.clear();
    } finally {
      listenersLock.writeLock().unlock();
    }
  }

  @Override
  public void onUpdate(final Tag cdt) {
    ruleMapLock.writeLock().lock();
    try {
      ruleInputValues.put(cdt.getId(), cdt);
    } finally {
      ruleMapLock.writeLock().unlock();
    }
    forceUpdate();
  }

  @Override
  public int hashCode() {
    return this.rule.hashCode();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ClientRuleTag) {
      ClientRuleTag<?> crt = (ClientRuleTag<?>) obj;
      if (this.getRuleExpression().getExpression().equalsIgnoreCase(crt.getRuleExpression().getExpression())
          && this.getType().equals(crt.getType())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Registers a <code>ClientDataTagValueUpdateListener</code> instance as
   * listener that will be informed when this <code>ClientRuleTag</code> gets
   * updated.
   *
   * @param pListener Listener to be registered for updates
   * @return <code>true</code>, if the listener was not already registered
   */
  public boolean addUpdateListener(final BaseTagListener pListener) {
    boolean retval = false;
    try {
      listenersLock.writeLock().lock();
      boolean isRegistered = false;
      // Search for pListener by reference
      for (BaseTagListener listener : listeners) {
        if (listener == pListener) {
          isRegistered = true;
          retval = true;
          break;
        }
      }

      if (!isRegistered) {
        retval = listeners.add(pListener);
      }
    } finally {
      listenersLock.writeLock().unlock();
    }
    return retval;
  }

  /**
   * Removes a <code>ClientDataTagValueUpdateListener</code> instance from
   * listener that will be informed when this <code>ClientRuleTag</code> gets
   * updated.
   *
   * @param listener Listener to be removed
   * @return <code>true</code>, if the listener successfully removed from
   * the listeners list
   */
  public boolean removeClientDataTagUpdateListener(final BaseTagListener listener) {
    boolean retval = false;
    try {
      listenersLock.writeLock().lock();
      retval = listeners.remove(listener);
    } finally {
      listenersLock.writeLock().unlock();
    }
    return retval;
  }

  /**
   * Private method to inform listeners about an update of this client rule
   */
  private void fireUpdateReceivedEvent() {
    try {
      listenersLock.readLock().lock();
      for (BaseTagListener listener : listeners) {
        listener.onUpdate(this);
      }
    } finally {
      listenersLock.readLock().unlock();
    }
  }

  @Override
  public Collection<Long> getAlarmIds() {
    return EMPTY_LONG_LIST;
  }

  @Override
  public Collection<Long> getEquipmentIds() {
    return EMPTY_LONG_LIST;
  }

  @Override
  public Collection<Long> getSubEquipmentIds() {
    return EMPTY_LONG_LIST;
  }

  @Override
  public TagMode getMode() {
    return ruleMode;
  }

  /**
   * @return The <code>CLientRuleTag</code> will always return an empty
   * list of process id's.
   */
  @Override
  public Collection<Long> getProcessIds() {
    return EMPTY_LONG_LIST;
  }

  /**
   * @return By definition the <code>ClientRuleTag</code> will always return
   * <code>null</code> as DAQ timestamp.
   */
  @Override
  public Timestamp getDaqTimestamp() {
    return null;
  }

  /**
   * @return By definition the <code>ClientRuleTag</code> will always return
   * <code>null</code> as server timestamp.
   */
  @Override
  public Timestamp getServerTimestamp() {
    return null;
  }

  /**
   * If one of the tags used within the rule is marked as simulated,
   * then this <code>ClientRuleTag</code> is also marked as simulated.
   */
  @Override
  public boolean isSimulated() {
    return simulated;
  }

  @Override
  public Collection<AlarmValue> getAlarms() {
    return EMPTY_ALARM_LIST;
  }

  @Override
  public boolean isValid() {
    return this.ruleQuality.isValid();
  }

  @Override
  public boolean isAliveTag() {
    return false;
  }

  @Override
  public boolean isControlTag() {
    return false;
  }
}
