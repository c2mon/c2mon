package cern.c2mon.shared.rule;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.shared.common.rule.RuleInputValue;
import cern.c2mon.shared.rule.RuleEvaluationException;
import cern.c2mon.shared.rule.RuleExpression;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * Tests for Separate Return values for OR statements in the Rule Engine .
 * @see http://issues/browse/TIMS-839
 * 
 * @author ekoufaki
 */
public class MultipleReturnValueRuleExpressionTest {
  

  /** --------------------------------------------------------------------------------------------**/
  /** --------------------------------------------------------------------------------------------**/

  /**
   * Correct RULES
   * These are tested bellow to make sure they return the expected value.
   */
  
  private static final String CORRECT_RULE_1 = "(#2L = 2)[3]" + "|" + "(#1L = 2)[2]" + ", true[4]";
  private static final String CORRECT_RULE_2 = "((1 = 2) | (3 = 2)) [5]" 
      + " | (#2 = 2)[3]" + "|" + "(#1 = 2)[2]" + ", true[4]";
  
  private static final String CORRECT_RULE_3 = "((1 = 2) | (3 = 2)) [5]" 
      + ", ((1 = 1) & (3 = 2)) [3]" + "| (#2L = 2)[4] | (#2L = 2)[1]  | (#1L = 2)[2]" + ", true[4]";
  
  private static final String CORRECT_RULE_4 = "(#2L = 2) | (#2L = 2) [3]" 
      + "|" + "(#1 = 2)[2]" + ", true[4]";
  
  private static final String CORRECT_RULE_5 = "(#1L = 2) [2] | (#1L = 3) [3]";
  
  
  /** --------------------------------------------------------------------------------------------**/
  /** --------------------------------------------------------------------------------------------**/

  private static final String RULE_1 = "#1L = false [navy] | true[red]";
  private static final String RULE_2 = "#1L = false [navy] | #2L = true [lime] | true[red]";

  /** --------------------------------------------------------------------------------------------**/
  /** --------------------------------------------------------------------------------------------**/
  
  /**
   * INVALID RULES
   * These are tested bellow and they should THROW an Exception as they are INVALID.
   */
  private static final String INVALID_RULE_1 = "((1 = 2) | (3 = 2)) [5]" + "| (#2L = 2)[2]" + ", true[4]";
  private static final String INVALID_RULE_2 = "(#2L * 2) > 0 [2] | ((1 = 2) | (3 = 2))" + ", true[4]";
  private static final String INVALID_RULE_3 = "((1 = 2) | (3 = 2)) [5]" 
      + ", ((1 = 2) | (3 = 2)) [5]" + "| (#2L = 2)[2]" + ", true[4]";

  private static final String INVALID_RULE_4 = "((1 = 2) | (3 = 2)) [5]" 
      + ", ((1 = 1) & (3 = 2)) [5]" + "| (#2L = 2)[2] | (#2L = 2)[2]  | (1 = 2)[2]" + ", true[4]";
  
  private static final String INVALID_RULE_5 = "((1 = 2) | (3 = 2)) [5]" + "| (#2L = #2L)[2]" + ", true[4]";
  
  private static final String INVALID_RULE_6 = "((1 = 2) | (3 = 2)) [5]" + "| (#2L != 2)[2]" + ", true[4]";

  /** --------------------------------------------------------------------------------------------**/
  /** --------------------------------------------------------------------------------------------**/
  
  
  private static final String TEST_1 = "#156279L = 0[0]|  true[3]";
  
  /**
   * Helper tags used in the rule above.
   */
  private static final Long TAG_TO_BE_MARKED_AS_VALID = 1L; //  value = 2
  private static final Long TAG_TO_BE_MARKED_AS_INVALID = 2L; //  value = 2
  
  private RuleInputValue invalidTag;
  private RuleInputValue validTag;
  
  @Before
  public void setUp() throws Exception {
    
    validTag = EasyMock.createMock(RuleInputValue.class);
    EasyMock.expect(validTag.isValid()).andReturn(true).times(99);
    EasyMock.expect(validTag.getValue()).andReturn((Object) 2).times(99);
    EasyMock.expect(validTag.getId()).andReturn((Long) TAG_TO_BE_MARKED_AS_VALID).times(99);
    EasyMock.replay(validTag);
    
    assertTrue(validTag.isValid());

    invalidTag = EasyMock.createMock(RuleInputValue.class);
    EasyMock.expect(invalidTag.isValid()).andReturn(false).times(99);
    EasyMock.expect(invalidTag.getValue()).andReturn((Object) 2).times(99);
    EasyMock.expect(invalidTag.getId()).andReturn((Long) TAG_TO_BE_MARKED_AS_INVALID).times(99);
    EasyMock.replay(invalidTag);

    assertTrue(!invalidTag.isValid());
  }
  
  @Test
  public void testSplit() throws RuleFormatException {
    
    MultipleReturnValueRuleExpression.splitToConditions(TEST_1);
  }
  

  /**
   * A validation test against the rules above to make sure they are INVALID. 
   */
  @Test(expected=RuleEvaluationException.class)
  public void testInvalidRules1() throws MBeanRegistrationException, InstanceNotFoundException,
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    evaluateRule(INVALID_RULE_1);
  }
  

  /**
   * A validation test against the rules above to make sure they are INVALID. 
   */
  @Test(expected=RuleEvaluationException.class)
  public void testInvalidRules2() throws MBeanRegistrationException, InstanceNotFoundException,
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    evaluateRule(INVALID_RULE_2);
  }
  
  /**
   * A validation test against the rules above to make sure they are INVALID. 
   */
  @Test(expected=RuleEvaluationException.class)
  public void testInvalidRules3() throws MBeanRegistrationException, InstanceNotFoundException,
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    evaluateRule(INVALID_RULE_3);
  }
  
  /**
   * A validation test against the rules above to make sure they are INVALID. 
   */
  @Test(expected=RuleEvaluationException.class)
  public void testInvalidRules4() throws MBeanRegistrationException, InstanceNotFoundException,
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    evaluateRule(INVALID_RULE_4);
  }
  

  /**
   * A validation test against the rules above to make sure they are INVALID. 
   */
  @Test(expected=RuleEvaluationException.class)
  public void testInvalidRules5() throws MBeanRegistrationException, InstanceNotFoundException,
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    evaluateRule(INVALID_RULE_5);
  }
  

  /**
   * A validation test against the rules above to make sure they are INVALID. 
   */
  @Test(expected=RuleEvaluationException.class)
  public void testInvalidRules6() throws MBeanRegistrationException, InstanceNotFoundException,
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    evaluateRule(INVALID_RULE_6);
  }
  
  @Test
  public void testCorrectRules() throws RuleFormatException, RuleEvaluationException {
    
    final Object result1 = evaluateRule(CORRECT_RULE_1);
    assertTrue(result1.equals("2"));
    
    final Object result2 = evaluateRule(CORRECT_RULE_2);
    assertTrue(result2.equals("2"));
    
    final Object result3 = evaluateRule(CORRECT_RULE_3);
    assertTrue(result3.equals("2"));

    final Object result4 = evaluateRule(CORRECT_RULE_4);
    assertTrue(result4.equals("2"));

    final Object result5 = evaluateRule(CORRECT_RULE_5);
    assertTrue(result5.equals("2"));
  }
  
  /**
   * Makes sure the rules below are instantiated as {@link MultipleReturnValueRuleExpression}
   * (@see {@link RuleExpression#createExpression(String)}
   */
  @Test
  public void testRuleCreation() throws RuleFormatException, RuleEvaluationException {

    final RuleExpression rule1 = RuleExpression.createExpression(RULE_1);
    assertTrue(rule1 instanceof MultipleReturnValueRuleExpression);

    final RuleExpression rule2 = RuleExpression.createExpression(RULE_2);
    assertTrue(rule2 instanceof MultipleReturnValueRuleExpression);
  }
  

  @Test
  public void testResultValue() throws RuleFormatException, RuleEvaluationException {
    
    ConditionedRuleExpression rule1 = 
        (ConditionedRuleExpression) RuleExpression.createExpression(CORRECT_RULE_1);
    
    IRuleCondition r = rule1.getConditions().get(0);
    
    Collection<Object> resultValue = (Collection<Object>) r.getResultValue();
    assertTrue(resultValue.contains("2"));
    assertTrue(resultValue.contains("3"));
    assertTrue(resultValue.size() == 2);
  }
  
  private Object evaluateRule(final RuleExpression rule) throws RuleEvaluationException {

    Set<Long> inputTags = rule.getInputTagIds();
    Iterator<Long> i = inputTags.iterator();

    Map<Long, Object> inputTagsMap = new HashMap<Long, Object>();

    while (i.hasNext()) {
      final Long id = i.next();
      if (id.equals(invalidTag.getId())) {
        inputTagsMap.put(id, invalidTag);
      } else {
        inputTagsMap.put(id, validTag);
      }
    }
    Object result = rule.evaluate(inputTagsMap);
    return result;
  }

  private Object evaluateRule(final String ruleString) throws RuleEvaluationException {

    RuleExpression rule = null;
    try {
      rule = RuleExpression.createExpression(ruleString);
    } catch (RuleFormatException e) {
      assertTrue(false);
    }
    return evaluateRule(rule);
  }
}
