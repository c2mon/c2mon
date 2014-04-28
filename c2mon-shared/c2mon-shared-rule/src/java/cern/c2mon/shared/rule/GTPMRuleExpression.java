package cern.c2mon.shared.rule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class GTPMRuleExpression extends RuleExpression implements IConditionedRule, Cloneable {

    private static final long serialVersionUID = -5874937263213582071L;
    
    /**
     * Ordered array of conditions making up this GTPM rule expression.
     */
    private List<IRuleCondition> conditions;

    /**
     * Create a GTPM rule expression object from a string expression.
     */
    public GTPMRuleExpression(final String pExpression) throws RuleFormatException {
        super(pExpression, RuleType.ConditionedRule);
        String[] subConditions = pExpression.split(",");
        this.conditions = new ArrayList<IRuleCondition>();
        for (int i = 0; i != subConditions.length; i++) {
            this.conditions.add(new GTPMRuleCondition(subConditions[i]));
        }
    }

    public Object clone() {
      
        GTPMRuleExpression clone = (GTPMRuleExpression) super.clone();
        
        clone.conditions = new ArrayList<IRuleCondition>();
        Iterator<IRuleCondition> i = conditions.iterator();
        while (i.hasNext()) {
          clone.conditions.add(i.next());
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
    public RuleValidationReport validate(final Map<Long, Object> pInputParams)  {
      
      Object result = null;
      for (final IRuleCondition condition: this.conditions) {
        
        Object tempResult;
        try {
          tempResult = condition.evaluate(pInputParams);
        } catch (RuleEvaluationException e) {
          final String errorMessage = e.getMessage();
          return new RuleValidationReport(false, errorMessage);
        }
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

    /**
     * @return a Deque of IRuleCondition objects making up this conditioned rule
     */
    public List<IRuleCondition> getConditions() {
      return conditions;
    }

    /**
     * Get the indentifiers of all input tags used in this rule expression (and its dependent conditions).
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
        str.append("<RuleExpression type=\"GTPMRuleExpression\">\n");
        
        Iterator<IRuleCondition> i = conditions.iterator();
        while (i.hasNext()) {
            str.append(i.next().toString());
        }
        str.append("</RuleExpression>\n");
        return str.toString();
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
}