/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2004 - 2012 CERN. This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.shared.rule;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * The conditioned rule expression class uses internally {@link DefaultRuleCondition} 
 * and allows creating rule expressions allows returning any string as
 * result.
 * 
 * @author Matthias Braeger
 */
public final class ConditionedRuleExpression extends RuleExpression 
    implements IConditionedRule, Cloneable, Serializable {

  private static final long serialVersionUID = 5664905942598603825L;

  /**
   * Ordered array of conditions making up this rule expression.
   */
  private List<IRuleCondition> conditions;

  /**
   * Create a GTPM rule expression object from a string expression.
   * 
   * @param pExpression The rule expression string
   * @throws RuleFormatException In case of parsing errors
   */
  public ConditionedRuleExpression(final String pExpression) throws RuleFormatException {
    
    super(pExpression, RuleType.ConditionedRule);
    String[] subConditions = pExpression.split(",");
    this.conditions = new ArrayList<IRuleCondition>();
    for (int i = 0; i != subConditions.length; i++) {
      final String subCondition = subConditions[i].trim();
      if (MultipleReturnValueRuleExpression
            .isMultipleReturnValueExpression(subCondition)) {
        this.conditions.add(new MultipleReturnValueRuleExpression(subCondition));
      } else {
        this.conditions.add(new DefaultRuleCondition(subCondition));
      }
    }
  }
  
  /**
   * Clone method implementation
   */
  public Object clone() {

    ConditionedRuleExpression clone = (ConditionedRuleExpression) super.clone();

    clone.conditions = new ArrayList<IRuleCondition>();
    Iterator<IRuleCondition> i = conditions.iterator();
    while (i.hasNext()) {
      final IRuleCondition condition = i.next();
      clone.conditions.add((IRuleCondition) condition.clone());
    }
    return clone;
  }

  /**
   * Evaluate the rule for the input parameters specified as a Hashtable.
   * 
   * @param pInputParams values for all input tags as a Hashtable.
   */
  @Override
  public Object evaluate(final Map<Long, Object> pInputParams) throws RuleEvaluationException {
    
    Object result = null;
    
    Iterator<IRuleCondition> i = conditions.iterator();
    while (result == null && i.hasNext()) {
      result = i.next().evaluate(pInputParams);
    }
    if (result == null) {
      throw new RuleEvaluationException("Evaluation error: none of the rule's conditions are TRUE.");
    }
    return result;
  }

  @Override
  public Object forceEvaluate(final Map<Long, Object> pInputParams) {
    Object result = null;
    
    Iterator<IRuleCondition> i = conditions.iterator();
    while (result == null && i.hasNext()) {
      result = i.next().forceEvaluate(pInputParams);
    }
    return result;
  }

  /**
   * @return a Deque of IRuleCondition objects making up this conditioned rule
   */
  public List<IRuleCondition> getConditions() {
    return conditions;
  }

  /**
   * @return identifiers of all input tags used in this rule expression (and its dependent conditions).
   */
  @Override
  public Set<Long> getInputTagIds() {
    Set<Long> ids = new LinkedHashSet<Long>();
    Iterator<IRuleCondition> i = conditions.iterator();
    while (i.hasNext()) {
      ids.addAll(i.next().getInputTagIds());
    }
    return ids;
  }

  public String toString() {

    StringBuffer str = new StringBuffer(this.expression.length());
    Iterator<IRuleCondition> i = conditions.iterator();
    while (i.hasNext()) {
      str.append(i.next().toString());
      if (i.hasNext()) {
        str.append(", ");
      }
      str.append("\n");
    }
    return str.toString();
  }

  public String toXml() {
    StringBuffer str = new StringBuffer(this.expression.length());
    str.append("<RuleExpression type=\"ConditionedRuleExpression\">\n");

    Iterator<IRuleCondition> i = conditions.iterator();
    while (i.hasNext()) {
      str.append(i.next().toString());
    }
    str.append("</RuleExpression>\n");
    return str.toString();
  }

  /**
   * @return True if an unreachable statement is detected in the current rule.
   * 
   *         This follows the simple logic:
   * 
   *         IF a condition is always true AND other conditions have to be evaluated after it, THEN those conditions are unreachable!
   */
  private boolean hasUnreachableStatements(final Map<Long, Object> pInputParams) {

    int currentConditionCount = 0;
    final int conditionCount = this.conditions.size();

    for (final IRuleCondition currentCondition : this.conditions) {

      currentConditionCount++;
      final boolean isLastCondition = (currentConditionCount == conditionCount);

      final String condition = currentCondition.getExpression();
      final boolean isConditionAlwaysTrue = isConditionAlwaysTrue(condition, pInputParams);

      // IF current condition is always true AND there is more conditions after it =>
      if (isConditionAlwaysTrue && !isLastCondition) {
        // => then the conditions that follow are unreachable!
        return true;
      }
    }
    return false;
  }

  /**
   * @return True if the given condition is always true, false otherwise
   * 
   * @param condition The condition to be checked
   * @param pInputParams Map of value objects related to the input tag ids
   */
  private boolean isConditionAlwaysTrue(final String condition, final Map<Long, Object> pInputParams) {

    RuleExpression expr = null;
    try {
      expr = RuleExpression.createExpression(condition);
    } catch (RuleFormatException e) {
      return false;
    }
    final boolean isAlwaysTrue = isExpressionAlwaysTrue(expr, pInputParams);
    return isAlwaysTrue;
  }

  /**
   * @return True if the given Expression is always true, false otherwise
   * 
   * @param expr The Expression to be checked
   * @param pInputParams Map of value objects related to the input tag ids
   */
  private boolean isExpressionAlwaysTrue(final RuleExpression expr, final Map<Long, Object> pInputParams) {

    if (expr instanceof SimpleRuleExpression) {
      SimpleRuleExpression simpleExpr = (SimpleRuleExpression) expr;
      try {
        if (simpleExpr.isAlwaysTrue(pInputParams)) {
          return true;
        }
      } catch (RuleEvaluationException e) {
        return false;
      }
    }
    return false;
  }

  @Override
  public RuleValidationReport validate(final Map<Long, Object> pInputParams) {

    Object result = null;

    if (hasUnreachableStatements(pInputParams)) {
      return new RuleValidationReport(false, "Unreachable Statement!");
    }

    for (final IRuleCondition condition : this.conditions) {

      // Let's validate this condition....
      final RuleValidationReport report = condition.validate(pInputParams);
      if (!report.isValid()) {
        return new RuleValidationReport(false);
      }
      
      // The condition was successfully validated!
      // But we also need to check that at least one of the conditions is TRUE
      final Object tempResult = condition.forceEvaluate(pInputParams);
      if (tempResult != null) {
        result = tempResult;
      }
    }
    if (result == null) {
      final String errorMessage = "Evaluation error: none of the rule's conditions are TRUE.";
      return new RuleValidationReport(false, errorMessage);
    }
    return new RuleValidationReport(true);
  }
}