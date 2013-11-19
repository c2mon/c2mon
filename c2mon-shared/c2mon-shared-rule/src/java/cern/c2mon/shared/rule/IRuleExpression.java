package cern.c2mon.shared.rule;

import java.io.Serializable;
import java.util.Map;

/**
 * Describes the methods of a rule expression.
 *
 * @author ekoufaki
 */
public interface IRuleExpression extends Cloneable, Serializable{

  /**
   * Protected method that evaluates the rule for the given input values of the tags 
   * without doing a final rule result cast.
   * 
   * @param pInputParams Map of value objects related to the input tag ids
   * @return The rule result for the given input values
   * @throws RuleEvaluationException In case of errors during the rule evaluation
   */
  Object evaluate(final Map<Long, Object> pInputParams) throws RuleEvaluationException;

  /**
   * Tries to calculate a value for a rule even if it is marked as Invalid
   * (this can be possible if a value is received for that Invalid tag).
   * 
   * Never throws exceptions. If the rule cannot be evaluated, null is returned.
   * 
   * @see http://issues/browse/TIMS-835
   * 
   * @param pInputParams Map of value objects related to the input tag ids
   * @return The rule result for the given input values.
   */
  Object forceEvaluate(final Map<Long, Object> pInputParams);

  
  /**
   * @return A report containing information on whether the Rule is valid or not,
   * and the errors found (if any).
   */
  RuleValidationReport validate(final Map<Long, Object> pInputParams);
  
  /**
   * Public Clone
   */
  Object clone();
}
