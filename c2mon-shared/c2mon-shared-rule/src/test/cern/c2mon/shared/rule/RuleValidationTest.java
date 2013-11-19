package cern.c2mon.shared.rule;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.easymock.EasyMock;

import cern.c2mon.shared.common.rule.RuleInputValue;
import cern.c2mon.shared.rule.parser.RuleConstant;

/**
 * Tests to make sure Rule validation works properly.
 * 
 * @author ekoufaki
 */
public class RuleValidationTest {
  

  private static final String WRONG_RULE_1 = " (#156381 = true)[0],true[2],(#156381 = true)[3]";
  private static final String WRONG_RULE_2 = "(#160686 = false) & (#155719 = false)[0],(#160686 != false) " 
      + "& (#155719 = false)[2],(#155719 = false)[3]";
  
  private static final String WRONG_RULE_3 = "'(#51042 = true)': (#51042 = 0)[0],(#51042 = true)[1]," 
      + "(#51042 = true)[2],(#51042 = true)[3]";
  
  private static final String WRONG_RULE_4 
    = "((#43719 = true) | (#187086 = true) | (#53004 = true) | (#53005 = true)" +
    		" | (#53006 = true) | (#53007 = true) | (#54008 = true)) [3]," +
    		"((((#43719 = false) & (#187086 = false) & (#53004 = false) " +
    		"& (#53005 = false) & (#53006 = false) & (#53007  = false) " +
    		"& (#54008 = false) & (#162337 = false))[4]," +
    		"true [1]";
  
  private static final String WRONG_RULE_5 =  "((2 = 2) | (3 = 2)) " + "| (#156279L = " 
      + RuleConstant.INVALID_KEYWORD.toString() + ")[2]" 
      + ", 1 + 1 [5],"  // wrong part! => not a condition
      + "true[4]";
  
  private static final String WRONG_RULE_6 =  "((2 = 2) | (3 = 2)) " 
      /**
       * wrong part! operator (>) does not make sense 
       * for the RuleConstant.INVALID_KEYWORD
       */
      + "| (#156279L > "   
      + RuleConstant.INVALID_KEYWORD.toString() + ")[2]" 
      + "," 
      + "true[4]";
  
  /** All the tags in the rules above are replaced with a Valid tag (return Value = True) */
  private RuleInputValue validTag;

  @Before
  public void setUp() throws Exception {
    validTag = EasyMock.createMock(RuleInputValue.class);
    
    EasyMock.expect(validTag.isValid()).andReturn(true).times(199);
    EasyMock.expect(validTag.getValue()).andReturn((Object) Boolean.TRUE).times(299);
    EasyMock.replay(validTag);
    
    assertTrue(validTag.isValid());
  }
  
  /**
   * A validation test against the rules above to make sure they are INVALID.
   */
  @Test
  public void validateWrongRules()  {
    
      RuleValidationReport status1 = validateRule(WRONG_RULE_1);
      RuleValidationReport status2 = validateRule(WRONG_RULE_2);
      RuleValidationReport status3 = validateRule(WRONG_RULE_3);
      RuleValidationReport status4 = validateRule(WRONG_RULE_4);
      RuleValidationReport status5 = validateRule(WRONG_RULE_5);
      RuleValidationReport status6 = validateRule(WRONG_RULE_6);
      
      assertTrue(!status1.isValid());
      assertTrue(!status2.isValid());
      assertTrue(!status3.isValid());
      assertTrue(!status4.isValid());
      assertTrue(!status5.isValid());
      assertTrue(!status6.isValid());
  }
  
  private static final String CORRECT_RULE_1 = "((#189755 = true) | (#189757 = true)) [2], " +
      "((#32420 = true) | (#188585 = true) | (#188587 = true) | (#188589 = true) " +
      "| (#188591 = true) | (#188593 = true) | (#188595 = true) | (#46337 = true)" +
      "| (#46338 = true) | (#46119 = true) | (#32416 = true)) [3], true [1]";

  private static final String CORRECT_RULE_2 = "(#34570 = false) & (#39073 = false)" +
      " & (#39075 = false) & (#39077= false) & (#39078 = false) & (#39079 = false)[1]," +
      "true[2]";
  
  private static final String CORRECT_RULE_3 = "(#156278 != true)[0],(#156279 = true)[3],true[2]";
  
  private static final String CORRECT_RULE_4 = "((2 = 2) | (3 = 2)) " + "| (#156279L = " 
  + RuleConstant.INVALID_KEYWORD.toString() + ")[2]" + ", true[4]";
  
  private static final String CORRECT_RULE_5 = "(#156278 = $INVALID)[0],(#156279 = true)[3],true[2]";
  private static final String CORRECT_RULE_6 = "($INVALID = $INVALID)[0],(#156279 = true)[3],true[2]";
  private static final String CORRECT_RULE_7 = "($INVALID != #156278)[0],(#156279 = true)[3],true[2]";
  
  /**
   * A validation test against the rules above to make sure they are VALID.
   */
  @Test
  public void validateCorrectRules()  {
    
      RuleValidationReport validStatus1 = validateRule(CORRECT_RULE_1);
      RuleValidationReport validStatus2 = validateRule(CORRECT_RULE_2);
      RuleValidationReport validStatus3 = validateRule(CORRECT_RULE_3);
      RuleValidationReport validStatus4 = validateRule(CORRECT_RULE_4);
      RuleValidationReport validStatus5 = validateRule(CORRECT_RULE_5);
      RuleValidationReport validStatus6 = validateRule(CORRECT_RULE_6);
      RuleValidationReport validStatus7 = validateRule(CORRECT_RULE_7);
      
      assertTrue(validStatus1.isValid());
      assertTrue(validStatus2.isValid());
      assertTrue(validStatus3.isValid());
      assertTrue(validStatus4.isValid());
      assertTrue(validStatus5.isValid());
      assertTrue(validStatus6.isValid());
      assertTrue(validStatus7.isValid());
  }
  
  private RuleValidationReport validateRule(final RuleExpression rule) {

    try {
      Set<Long> inputTags = rule.getInputTagIds();
      Iterator<Long> i = inputTags.iterator();

      Map<Long, Object> inputTagsMap = new HashMap<Long, Object>();
      while (i.hasNext()) {
        inputTagsMap.put(i.next(), validTag);
      }
      RuleValidationReport ruleValidation = rule.validate(inputTagsMap);
      return ruleValidation;

    } catch (Exception e) {
      assertTrue(false);
      return null;
    }
  }
  
  private RuleValidationReport validateRule(final String ruleString) {

      RuleExpression rule = null;
      try {
        rule = RuleExpression.createExpression(ruleString);
      } catch (RuleFormatException e) {
        assertTrue(false);
      }
      return validateRule(rule);
  }

}
