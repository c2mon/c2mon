/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.common.alarm;

import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.w3c.dom.Document;

import cern.c2mon.shared.client.alarm.condition.AlarmCondition;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

import static org.junit.Assert.assertEquals;

/**
 * Unit test of Range alarm condition implementation.
 *
 * @author Matthias Braeger
 *
 */
@Slf4j
public class RangeAlarmConditionTest {

  @Test
  public void testXmlDeserializationBackwardComp() throws ParserConfigurationException {
    String xmlString = "<AlarmCondition class=\"cern.c2mon.server.common.alarm.RangeAlarmCondition\">"
    + "<min-value type=\"Float\">0.0</min-value>"
    + "<max-value type=\"Float\">100.0</max-value>"
    + "</AlarmCondition>";

    SimpleXMLParser parser = new SimpleXMLParser();
    Document document = parser.parse(xmlString);
    AlarmCondition condition = AlarmCondition.fromConfigXML(document.getDocumentElement());
    checkFloatConditions(condition, false);
  }

  @Test
  public void testXmlDeserializationForOutOfRange() throws ParserConfigurationException {
    RangeAlarmCondition<Float> rangeAlarmCondition = new RangeAlarmCondition<>(0f, 100f);
    rangeAlarmCondition.setOutOfRangeAlarm(true);
    String xmlString = rangeAlarmCondition.toConfigXML();

    SimpleXMLParser parser = new SimpleXMLParser();
    Document document = parser.parse(xmlString);
    AlarmCondition condition = AlarmCondition.fromConfigXML(document.getDocumentElement());
    checkFloatConditions(condition, rangeAlarmCondition.isOutOfRangeAlarm());
  }

  private void checkFloatConditions(AlarmCondition rangeAlarmCondition, boolean outOfRangeAlarm) {
    assertEquals(!outOfRangeAlarm, rangeAlarmCondition.evaluateState(5f));
    assertEquals(!outOfRangeAlarm, rangeAlarmCondition.evaluateState(0f));
    assertEquals(!outOfRangeAlarm, rangeAlarmCondition.evaluateState(99.9f));
    assertEquals(!outOfRangeAlarm, rangeAlarmCondition.evaluateState(100f));

    assertEquals(!outOfRangeAlarm, rangeAlarmCondition.evaluateState(89));

    assertEquals(outOfRangeAlarm, rangeAlarmCondition.evaluateState(-10f));
    assertEquals(outOfRangeAlarm, rangeAlarmCondition.evaluateState(150f));
  }
}
