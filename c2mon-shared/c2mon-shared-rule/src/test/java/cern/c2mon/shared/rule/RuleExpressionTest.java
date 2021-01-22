package cern.c2mon.shared.rule;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import lombok.AllArgsConstructor;
import lombok.Data;

import cern.c2mon.shared.common.rule.RuleInputValue;

public class RuleExpressionTest {

  @Test
  public void testEvaluateForResultBoolean() throws RuleFormatException, RuleEvaluationException {
    String rule1 = "((#1 != 1) | ((#2 - #3) > 20) | ((#4 - #5) > 20))";
    RuleExpression expression = RuleExpression.createExpression(rule1);
    
    Map<Long, RuleInputValue> inputValues = new HashMap<>();
    inputValues.put(1L, new RuleInputValueImpl(1L, 0.0f, true));
    inputValues.put(2L, new RuleInputValueImpl(2L, 100.234f, true));
    inputValues.put(3L, new RuleInputValueImpl(3L, 10.345f, true));
    inputValues.put(4L, new RuleInputValueImpl(4L, 100.234d, true));
    inputValues.put(5L, new RuleInputValueImpl(5L, 10.345f, true));
    
    Boolean resultO = expression.evaluate(inputValues, Boolean.class);
    Assert.assertTrue(resultO);

    inputValues.put(1L, new RuleInputValueImpl(1L, 1.0f, true));
    Boolean result = expression.evaluate(inputValues, Boolean.class);
    Assert.assertTrue(result);
    
    inputValues.put(2L, new RuleInputValueImpl(2L, 20.234f, true));
    result = expression.evaluate(inputValues, Boolean.class);
    Assert.assertTrue(result);
    
    inputValues.put(4L, new RuleInputValueImpl(4L, 20.234d, true));
    result = expression.evaluate(inputValues, Boolean.class);
    Assert.assertFalse(result);
  }

  @Data
  @AllArgsConstructor
  private class RuleInputValueImpl implements RuleInputValue {
    private long id;
    private Object value;
    private boolean valid;
  }
}
