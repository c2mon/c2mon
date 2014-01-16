package cern.c2mon.server.rule;

/**
 * Spring bean managing the rule evaluation logic.
 * 
 * @author Mark Brightwell
 *
 */
public interface RuleEvaluator {

  /**
   * Evaluate the rule with the given id. Will only be updated in the
   * cache if the evaluation is not the same as the existing cache value.
   * 
   * <p>Acquires rule write lock.
   * 
   * @param ruleId id of the rule
   */
  void evaluateRule(Long ruleId);

  
}
