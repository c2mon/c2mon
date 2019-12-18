package cern.c2mon.shared.client.configuration.api.alarm;

import org.junit.Assert;
import org.junit.Test;

public class RangeConditionTest {

  String expectedXmlString = "<AlarmCondition class=\"cern.c2mon.server.common.alarm.RangeAlarmCondition\">"
      + "\n  <min-value type=\"java.lang.Float\">0.0</min-value>"
      + "\n  <max-value type=\"java.lang.Float\">100.0</max-value>"
      + "\n  <out-of-range-alarm type=\"java.lang.Boolean\">false</out-of-range-alarm>"
      + "\n</AlarmCondition>";

  @Test
  public void testClass() {
    RangeCondition<Number> intRangeCondition = RangeCondition.builder().minValue(0f).maxValue(100f).build();
    String xmlString = intRangeCondition.getXMLCondition();
    Assert.assertEquals(expectedXmlString, xmlString);
  }
}
