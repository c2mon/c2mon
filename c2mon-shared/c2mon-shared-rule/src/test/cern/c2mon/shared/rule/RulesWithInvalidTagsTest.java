package cern.c2mon.shared.rule;

import static org.junit.Assert.assertTrue;

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
 * Tests to make sure Rule evaluation works properly.
 * 
 * In this test, some of the Tags contained in the Rule, are Marked as invalid. 
 * A validation is then performed using those Invalid Tags.
 * 
 * Depending on the case, the RULE can either return a result, or be marked as INVALID.
 * 
 * @see http://issues/browse/TIMS-834
 * 
 * @author ekoufaki
 */
public class RulesWithInvalidTagsTest {

  private static final String CORRECT_RULE_1 = "((1 = 2) | (3 = 2)) " + "& (#156279L = 2)[2]" + ", true[4]";
  private static final String CORRECT_RULE_2 = "((2 = 2) | (3 = 2)) " + "| (#156279L = 2)[2]" + ", true[4]";
  private static final String CORRECT_RULE_3 = "#156279L = $INVALID[2], true[4]";
  private static final String CORRECT_RULE_4 = "#156279L != $INVALID[2], true[4]";

  private static final String INVALID_RULE_1 = "((1 = 2) | (3 = 2)) " + "| (#156279L = 2)[2]" + ", true[4]";
  private static final String INVALID_RULE_2 = "((1 = 1) | (2 = 2)) " + "& (#156279L = 2)[2]" + ", true[4]";
  private static final String INVALID_RULE_3 = "(#156279L * 2) > 0 [2]" + ", true[4]";
  private static final String INVALID_RULE_4 = "#156279L = 0[0], true[3]";
  private static final String INVALID_RULE_5 = "#156279L > $INVALID[0], true[3]";
  private static final String INVALID_RULE_6 = "(#156279L > 3) + $INVALID[0], true[3]";

  private static final Long TAG_TO_BE_MARKED_AS_INVALID = 156279L;

  private RuleInputValue invalidTag;

  @Before
  public void setUp() throws Exception {

    invalidTag = EasyMock.createMock(RuleInputValue.class);
    EasyMock.expect(invalidTag.isValid()).andReturn(false).times(99);
    EasyMock.expect(invalidTag.getValue()).andReturn((Object) Boolean.TRUE).times(99);
    EasyMock.expect(invalidTag.getId()).andReturn((Long) TAG_TO_BE_MARKED_AS_INVALID).times(99);
    EasyMock.replay(invalidTag);

    assertTrue(!invalidTag.isValid());
  }

  /**
   * A validation test against the rules above to make sure they are VALID. The rules contain INVALID tags,
   *  but they can be properly evaluated.
   */
  @Test
  public void validateCorrectRules() 
      throws MBeanRegistrationException, InstanceNotFoundException, 
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    final Object result1 = evaluateRule(CORRECT_RULE_1);
    final Object result2 = evaluateRule(CORRECT_RULE_2);
    final Object result3 = evaluateRule(CORRECT_RULE_3);
    final Object result4 = evaluateRule(CORRECT_RULE_4);

    assertTrue(result1.equals("4"));
    assertTrue(result2.equals("2"));
    assertTrue(result3.equals("2"));
    assertTrue(result4.equals("4"));
  }

  /**
   * A validation test against the rules above to make sure they are INVALID. 
   * (The rules contain INVALID tags, but they can NOT be properly evaluated).
   */
  @Test(expected=RuleEvaluationException.class)
  public void validateWrongRules1() throws MBeanRegistrationException, InstanceNotFoundException,
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    evaluateRule(INVALID_RULE_1);
  }
  
  @Test(expected=RuleEvaluationException.class)
  public void validateWrongRules2() throws MBeanRegistrationException, InstanceNotFoundException,
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    evaluateRule(INVALID_RULE_2);
  }
  
  @Test(expected=RuleEvaluationException.class)
  public void validateWrongRules3() throws MBeanRegistrationException, InstanceNotFoundException,
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    evaluateRule(INVALID_RULE_3);
  }

  @Test(expected=RuleEvaluationException.class)
  public void validateWrongRules4() throws MBeanRegistrationException, InstanceNotFoundException,
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    evaluateRule(INVALID_RULE_4);
  }
  
  @Test(expected=RuleEvaluationException.class)
  public void validateWrongRules5() throws MBeanRegistrationException, InstanceNotFoundException,
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    evaluateRule(INVALID_RULE_5);
  }
  
  @Test(expected=RuleEvaluationException.class)
  public void validateWrongRules6() throws MBeanRegistrationException, InstanceNotFoundException,
      MalformedObjectNameException, NullPointerException, InterruptedException,
      RuleEvaluationException {

    evaluateRule(INVALID_RULE_6);
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
        assertTrue(false);
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
