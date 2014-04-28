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

import java.util.Map;
import java.util.Set;


/**
 * This class is used internally by {@link ConditionedRuleExpression} 
 * and allows to write conditioned rules with no
 * restrictions on the resulting String.
 * 
 * @author Matthias Braeger
 */
class DefaultRuleCondition extends RuleExpression implements IRuleCondition, Cloneable {

    private static final long serialVersionUID = -3182130061024381487L;

    /** The value that shall be returned in case the condition is true */
    private final Object resultValue;

    /** The simple rule condition expression */
    private final RuleExpression condition;

    /**
     * Default constructor
     * 
     * @param pExpression A default rule expression string 
     * which consist of a {@link SimpleRuleExpression} followed by
     *            the resulting value in square brackets, e.g. [WARNING]
     * @throws RuleFormatException In case of problems during the parsing of the default rule string
     */
    DefaultRuleCondition(final String pExpression) throws RuleFormatException {
        super(pExpression, RuleType.ConditionedRule);
        try {
            // Extract result value
            String resultValueStr = expression.substring(expression.indexOf("[") + 1,
                expression.indexOf("]")).trim();
            if (resultValueStr == null || resultValueStr.equalsIgnoreCase("")) {
                throw new RuleFormatException("Error parsing Default rule condition." 
                    + " A result value must be defined.");
            }
            if (resultValueStr.equalsIgnoreCase("true")) {
                this.resultValue = Boolean.TRUE;
            } else if (resultValueStr.equalsIgnoreCase("false")) {
                this.resultValue = Boolean.FALSE;
            } else {
                this.resultValue = resultValueStr;
            }

            // Create a new SimpleRuleExpression from the condition
            this.condition = new SimpleRuleExpression(expression.substring(0,
                expression.indexOf("[")).trim());
        } catch (IndexOutOfBoundsException iob) {
            throw new RuleFormatException(
                    "Error parsing Default rule condition. Expected a result value in square brackets.");
        } catch (Exception e) { // Exception extracting the result
            throw new RuleFormatException("Error parsing Default rule condition", e);
        }
    }

    @Override
    public IRuleCondition clone() {
        DefaultRuleCondition ruleCondition = (DefaultRuleCondition) super.clone();
        return ruleCondition;
    }
    
    public String toXml() {

      StringBuffer str = new StringBuffer();
      str.append("<RuleExpression type=\"DefaultRuleCondition\">\n");
      str.append(this.condition);
      str.append("  <result-value>");
      str.append(this.resultValue);
      str.append("</result-value>\n");
      str.append("\n</RuleExpression>\n");
      return str.toString();
    }

    @Override
    public String toString() {
      
      StringBuffer str = new StringBuffer();
      str.append(this.condition);
      str.append("[");
      str.append(this.resultValue);
      str.append("] ");
      return str.toString();
    }

    /**
     * Evaluates the expression and returns
     * <OL>
     * <LI>the result value if the underlying condition is evaluated to TRUE
     * <LI>null if the underlying condition is evaluated to FALSE
     * </OL>
     * 
     * @param pInputParams Map of tag values for the corresponding tags
     * @throws RuleEvaluationException In case an error occurs or the rule condition
     *  does not evaluate TRUE or FALSE
     */
    @Override
    public Object evaluate(final Map<Long, Object> pInputParams) throws RuleEvaluationException {
        Object result;
        try {
            result = this.condition.evaluate(pInputParams);
        } catch (RuleEvaluationException e) {
            throw e;
        } catch (Exception e) {
            final String ruleExpression;
            if (this.condition != null && this.condition.getExpression() != null) {
                ruleExpression = this.condition.getExpression();
            } else {
                ruleExpression = "";
            }
          throw new RuleEvaluationException(String.
              format("Unexpected error while trying to evaluate the expression '%s'",
              ruleExpression), e);
        }

        if (result == null) {
            throw new RuleEvaluationException(new StringBuffer("Error evaluating condition: ").append(
                    this.condition.getExpression()).toString());
        }
       
        return calculateReturnValue(result);
    }
    
    /**
     * Depending on whether the condition is evaluated to TRUE / FALSE,
     * a different return value should be returned.
     * 
     * @return The return value that corresponds to the status of this condition.
     * 
     * @param result
     * @throws RuleEvaluationException
     */
    private Object calculateReturnValue(final Object result) 
        throws RuleEvaluationException {
      
      try {
          if (((Boolean) result).equals(Boolean.TRUE)) {
              return this.resultValue;
          } else {
              return null;
          }
      } catch (ClassCastException ce) {
          throw new RuleEvaluationException(
              new StringBuffer("Condition does not evaluate to TRUE or FALSE: ")
                  .append(this.condition.getExpression()).toString());
      }
    }
    
    @Override
    public Object forceEvaluate(final Map<Long, Object> pInputParams) {
        Object result;
        
        result = this.condition.forceEvaluate(pInputParams);

        if (result == null) {
            return null;
        }
        try {
            if (((Boolean) result).equals(Boolean.TRUE)) {
                return this.resultValue;
            } else {
                return null;
            }
        } catch (final ClassCastException ce) {
          return null;
        }
    }

    @Override
    public Set<Long> getInputTagIds() {
        return this.condition.getInputTagIds();
    }

    @Override
    public Object getResultValue() {
        return this.resultValue;
    }

    @Override
    public String getExpression() {
        return this.condition.getExpression();
    }


    @Override
    public RuleValidationReport validate(final Map<Long, Object> pInputParams) {

      try {
        RuleValidationReport conditionReport = this.condition.validate(pInputParams);
      if (!conditionReport.isValid()) {
        return conditionReport;
      }
      
      final Object result = this.condition.forceEvaluate(pInputParams);
        calculateReturnValue(result);
      } catch (final Exception e) {
        return new RuleValidationReport(false, e.getMessage());
      }
      return new RuleValidationReport(true);
    }
}
