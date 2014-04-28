package cern.c2mon.shared.rule;

import java.util.Map;
import java.util.Set;


public class GTPMRuleCondition extends RuleExpression implements IRuleCondition, Cloneable {

    private static final long serialVersionUID = 9148180861976415811L;

    /** The value that shall be returned in case the condition is true */
    private final Object resultValue;

    /** The simple rule condition expression */
    private final SimpleRuleExpression condition;

    /**
     * Default constructor
     * 
     * @param pExpression The GTPM rule expression string
     * @throws RuleFormatException In case of problems during the parsing of the GTPM rule
     */
    public GTPMRuleCondition(final String pExpression) throws RuleFormatException {
        super(pExpression, RuleType.ConditionedRule);
        try {
            // Extract result value
            String resultValueStr = expression.substring(expression.indexOf("[") + 1, expression.indexOf("]")).trim();
            if (resultValueStr.equalsIgnoreCase("true")) {
                this.resultValue = Boolean.TRUE;
            } else if (resultValueStr.equalsIgnoreCase("false")) {
                this.resultValue = Boolean.FALSE;
            } else {
                this.resultValue = Integer.valueOf(resultValueStr);
            }

            // Create a new SimpleRuleExpression from the condition
            this.condition = new SimpleRuleExpression(expression.substring(0, expression.indexOf("[")).trim());
        } catch (Exception e) {// Exception extracting the result
            throw new RuleFormatException("Error parsing GTPM rule condition", e);
        }
    }

    @Override
    public Object clone() {
        GTPMRuleCondition gtpmRuleCondition = (GTPMRuleCondition) super.clone();
        return gtpmRuleCondition;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("<RuleExpression type=\"GTPMRuleCondition\">\n");
        str.append(this.condition);
        str.append("  <result-value>");
        str.append(this.resultValue);
        str.append("</result-value>\n");
        str.append("\n</RuleExpression>\n");
        return str.toString();
    }

    /**
     * Evaluates the expression and returns
     * <OL>
     * <LI>the result value if the underlying condition is evaluated to TRUE
     * <LI>null if the underlying condition is evaluated to FALSE
     * </OL>
     */
    @Override
    public Object evaluate(Map<Long, Object> pInputParams) throws RuleEvaluationException {
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
            throw new RuntimeException(String.format("Unexpected error while trying to evaluate the expression '%s'",
                    ruleExpression), e);
        }
        if (result == null) {
            throw new RuleEvaluationException(new StringBuffer("Error evaluating condition: ").append(
                    this.condition.getExpression()).toString());
        }
        try {
            if (((Boolean) result).equals(Boolean.TRUE)) {
                return this.resultValue;
            } else {
                return null;
            }
        } catch (ClassCastException ce) {
            throw new RuleEvaluationException(new StringBuffer("Condition does not evaluate to TRUE or FALSE: ")
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
      } catch (ClassCastException ce) {
        return null;
      }
    }
    
    @Override
    public RuleValidationReport validate(final Map<Long, Object> pInputParams) {

      // Running evaluate() and checking for exceptions,
      // is sufficient for testing whether a rule is valid ONLY IF
      // a SINGLE rule condition has to be checked
      try {
        evaluate(pInputParams);
      } 
      catch (Exception e) {
        return new RuleValidationReport(false, e.getMessage());
      }
      return new RuleValidationReport(true);
    }

    @Override
    public Set<Long> getInputTagIds() {
        return this.condition.getInputTagIds();
    }

    public Object getResultValue() {
        return this.resultValue;
    }

    @Override
    public String getExpression() {
        return this.condition.getExpression();
    }
}
