/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.shared.client.alarm.condition;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
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
    String xmlString = "<AlarmCondition class=\"cern.c2mon.shared.client.alarm.condition.ValueAlarmCondition\">"
      + "<alarm-value type=\"String\">DOWN</alarm-value>"
      + "</AlarmCondition>";
    SimpleXMLParser parser = new SimpleXMLParser();
    Document document = parser.parse(xmlString);
    AlarmCondition condition = AlarmCondition.fromConfigXML(document.getDocumentElement());

    SupervisionStatus value = SupervisionStatus.DOWN;
    Assert.assertEquals(true, condition.evaluateState(value));

    SupervisionStatus value2 = SupervisionStatus.RUNNING;
    Assert.assertEquals(false, condition.evaluateState(value2));
  }

  /**
   * Tests that Enums can be compared to String alarm conditions.
   * @throws ParserConfigurationException
   */
  @Test
  public void testEnumHandling2() throws ParserConfigurationException {
    String xmlString = "<AlarmCondition class=\"cern.c2mon.shared.client.alarm.condition.ValueAlarmCondition\">"
      + "<alarm-value type=\"String\">RUNNING</alarm-value>"
      + "</AlarmCondition>";
    SimpleXMLParser parser = new SimpleXMLParser();
    Document document = parser.parse(xmlString);
    AlarmCondition condition = AlarmCondition.fromConfigXML(document.getDocumentElement());

    SupervisionStatus value = SupervisionStatus.DOWN;
    Assert.assertEquals(false, condition.evaluateState(value));

    SupervisionStatus value2 = SupervisionStatus.RUNNING;
    Assert.assertEquals(true, condition.evaluateState(value2));
  }


  /**
   * Tests that Enums can be compared to String alarm conditions.
   * @throws ParserConfigurationException
   */
  @Test
  public void testDeserialization() throws ParserConfigurationException {
    AlarmCondition valueAlarmCondition = new ValueAlarmCondition("DOWN");
    String xmlString = valueAlarmCondition.toConfigXML();

    SimpleXMLParser parser = new SimpleXMLParser();
    Document document = parser.parse(xmlString);
    AlarmCondition condition = AlarmCondition.fromConfigXML(document.getDocumentElement());

    SupervisionStatus value = SupervisionStatus.DOWN;
    Assert.assertEquals(true, condition.evaluateState(value));

    SupervisionStatus value2 = SupervisionStatus.RUNNING;
    Assert.assertEquals(false, condition.evaluateState(value2));
  }
}
