package cern.c2mon.server.common.alarm;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;

import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

/**
 * Unit test of value alarm condition implementation.
 * 
 * @author Mark Brightwell
 *
 */
public class ValueAlarmConditionTest {
  
  /**
   * Tests that Enums can be compared to String alarm conditions.
   * @throws ParserConfigurationException 
   */
  @Test
  public void testEnumHandling() throws ParserConfigurationException {
    String xmlString = "<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\">"
      + "<alarm-value type=\"String\">DOWN</alarm-value>"
      + "</AlarmCondition>";
    SimpleXMLParser parser = new SimpleXMLParser();
    Document document = parser.parse(xmlString);
    AlarmCondition condition = AlarmCondition.fromConfigXML(document.getDocumentElement());
    
    SupervisionStatus value = SupervisionStatus.DOWN;
    Assert.assertEquals(AlarmCondition.ACTIVE, condition.evaluateState(value));
    
    SupervisionStatus value2 = SupervisionStatus.RUNNING;
    Assert.assertEquals(AlarmCondition.TERMINATE, condition.evaluateState(value2));    
  }
  
}
