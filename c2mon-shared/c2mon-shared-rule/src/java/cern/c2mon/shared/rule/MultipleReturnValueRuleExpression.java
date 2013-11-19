package cern.c2mon.shared.rule;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Rule expression consisting of OR statements 
 * with different Return Values.
 * 
 * @see http://issues/browse/TIMS-839
 * 
 * @author ekoufaki
 */
public class MultipleReturnValueRuleExpression extends RuleExpression 
    implements IConditionedRule, IRuleCondition {

  public MultipleReturnValueRuleExpression(final String pExpression) throws RuleFormatException {
    super(pExpression);
    this.conditions = splitToConditions(pExpression);
  }
  
  /**
   * @return True if the rule is a {@link MultipleReturnValueRuleExpression}
   * @see http://issues/browse/TIMS-839
   * 
   * @param rule the rule to be checked
   */
  public static boolean isMultipleReturnValueExpression(final String rule) {
    final int bracketsCount = StringUtils.countMatches(rule.toString(), "[");
    return bracketsCount > 1;
  }

  /**
   * Ordered conditions making up this rule expression.
   */
  private List<IRuleCondition> conditions;

  /**
   * @return Splits the given expression to multiple {@link IRuleCondition}.
   * 
   * @param expression String representing a {@link MultipleReturnValueRuleExpression}
   * @throws RuleFormatException in case the rule expression has incorrect syntax
   */
  public static List<IRuleCondition> splitToConditions(final String expression) throws RuleFormatException {

    final String[] subConditions = expression.split("]");
    List<IRuleCondition> conditions = new ArrayList<IRuleCondition>();

    for (int i = 0; i != subConditions.length; i++) {
      if (i > 0) {
        subConditions[i] = subConditions[i].substring(subConditions[i].indexOf("|") + 1);
      }
      conditions.add(new DefaultRuleCondition(subConditions[i].trim() + "]"));
    }
    return conditions;
  }

  @Override
  public Object evaluate(final Map<Long, Object> pInputParams) throws RuleEvaluationException {

    Object result = null;
    Iterator<IRuleCondition> i = conditions.iterator();
    
    boolean isInvalid = false;
    
    while (result == null && i.hasNext()) {

      try {
        result = i.next().evaluate(pInputParams);
      } catch (final RuleEvaluationException e) {
        isInvalid = true;
      }
    }
    if (result == null && isInvalid) {
      throw new RuleEvaluationException();
    }
    return result;
  }

  @Override
  public Object forceEvaluate(final Map<Long, Object> pInputParams) {

    try {
      return evaluate(pInputParams);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public RuleValidationReport validate(final Map<Long, Object> pInputParams) {

    try {

      Object result = null;
      Iterator<IRuleCondition> i = conditions.iterator();
      while (result == null && i.hasNext()) {
        result = i.next().evaluate(pInputParams);
      }
      return new RuleValidationReport(true);
    } catch (final Exception e) {
      return new RuleValidationReport(false, e.getMessage());
    }
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

  /**
   * @return a Deque of IRuleCondition objects making up this conditioned rule
   */
  public List<IRuleCondition> getConditions() {
    return conditions;
  }

  @Override
  public Object getResultValue() {
    
    Collection<Object> resultValues = new ArrayList<Object>();
    Iterator<IRuleCondition> i = conditions.iterator();
    while (i.hasNext()) {
      resultValues.add(i.next().getResultValue());
    }
    return resultValues;
  }
  
  /**
   * Clone method implementation
   */
  public Object clone() {

    MultipleReturnValueRuleExpression clone = (MultipleReturnValueRuleExpression) super.clone();

    clone.conditions = new ArrayList<IRuleCondition>();
    Iterator<IRuleCondition> i = conditions.iterator();
    while (i.hasNext()) {
      clone.conditions.add((IRuleCondition) i.next().clone());
    }
    return clone;
  }
}
