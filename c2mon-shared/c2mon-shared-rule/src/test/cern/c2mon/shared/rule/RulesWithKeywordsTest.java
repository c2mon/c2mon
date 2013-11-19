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
import cern.c2mon.shared.rule.parser.RuleConstant;

/**
 * Tests the Constants found in {@link RuleConstant}.
 * 
 * (for now only the {@link RuleConstant#INVALID} since it's the only one)
 * @see http://issues/browse/TIMS-836
 */
public class RulesWithKeywordsTest {

  private static final String RULE_WITH_INVALIDS_1 = "#156279L = $INVALID[0], true[3]";
  private static final String RULE_WITH_INVALIDS_2 = "#156278L != $INVALID[0], true[3]";
  private static final String RULE_WITH_NO_INVALIDS = "#156278L != 5[0], true[3]";

  private static final Long TAG_TO_BE_MARKED_AS_INVALID = 156279L;
  private static final Long TAG_TO_BE_MARKED_AS_VALID = 156278L;

  private RuleInputValue validTag;
  private RuleInputValue invalidTag;

  @Before
  public void setUp() throws Exception {

    validTag = EasyMock.createMock(RuleInputValue.class);

    EasyMock.expect(validTag.isValid()).andReturn(true).times(99);
    EasyMock.expect(validTag.isValid()).andReturn(true).times(99);
    EasyMock.expect(validTag.getValue()).andReturn((Object) Boolean.TRUE).times(99);
    EasyMock.expect(validTag.getId()).andReturn((Long) TAG_TO_BE_MARKED_AS_VALID).times(99);
    EasyMock.replay(validTag);

    assertTrue(validTag.isValid());

    invalidTag = EasyMock.createMock(RuleInputValue.class);
    EasyMock.expect(invalidTag.isValid()).andReturn(false).times(99);
    EasyMock.expect(invalidTag.getValue()).andReturn((Object) Boolean.TRUE).times(99);
    EasyMock.expect(invalidTag.getId()).andReturn((Long) TAG_TO_BE_MARKED_AS_INVALID).times(99);
    EasyMock.replay(invalidTag);

    assertTrue(!invalidTag.isValid());
  }

  /**
   * Tests that use INVALID as a KEYWORD. Let's make sure they work.
   * @throws RuleEvaluationException 
   */
  @Test
  public void validateRulesThatUseInvalidAsKeyword() throws MBeanRegistrationException,
      InstanceNotFoundException, MalformedObjectNameException,
      NullPointerException, InterruptedException, RuleEvaluationException {

    String result1 = (String) evaluateRule(RULE_WITH_INVALIDS_1);
    assertTrue(result1.equals("0"));

    String result2 = (String) evaluateRule(RULE_WITH_INVALIDS_2);
    assertTrue(result2.equals("0"));
  }
  

  /**
   * Tests helper method {@link SimpleRuleExpression#usesTheInvalidKeyword()}
   */
  @Test
  public void testHelperMethods() throws MBeanRegistrationException,
      InstanceNotFoundException, MalformedObjectNameException,
      NullPointerException, InterruptedException, RuleEvaluationException, RuleFormatException {

    SimpleRuleExpression rule1 = new SimpleRuleExpression(RULE_WITH_INVALIDS_1);
    assertTrue(rule1.usesTheInvalidKeyword());

    SimpleRuleExpression rule2 = new SimpleRuleExpression(RULE_WITH_INVALIDS_2);
    assertTrue(rule2.usesTheInvalidKeyword());

    SimpleRuleExpression rule3 = new SimpleRuleExpression(RULE_WITH_NO_INVALIDS);
    assertTrue(!rule3.usesTheInvalidKeyword());
  }

  private Object evaluateRule(final RuleExpression rule) throws RuleEvaluationException {

    Set<Long> inputTags = rule.getInputTagIds();
    Iterator<Long> i = inputTags.iterator();

    Map<Long, Object> inputTagsMap = new HashMap<Long, Object>();

    while (i.hasNext()) {
      final Long id = i.next();
      if (id.equals(validTag.getId())) {
        inputTagsMap.put(id, validTag);
      } else if (id.equals(invalidTag.getId())) {
        inputTagsMap.put(id, invalidTag);
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
