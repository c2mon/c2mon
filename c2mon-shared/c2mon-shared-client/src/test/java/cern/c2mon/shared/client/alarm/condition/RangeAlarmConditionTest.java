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
package cern.c2mon.shared.client.alarm.condition;

import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.w3c.dom.Document;

import cern.c2mon.shared.util.parser.SimpleXMLParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test of Range alarm condition implementation.
 *
 * @author Matthias Braeger
 *
 */
@Slf4j
public class RangeAlarmConditionTest {

  @Test
  public void testRangeInteger() throws ParserConfigurationException {
    RangeAlarmCondition<Integer> rangeAlarmCondition = new RangeAlarmCondition<>(0, 100);
    assertEquals(true, rangeAlarmCondition.evaluateState(5));
    assertEquals(true, rangeAlarmCondition.evaluateState(0));
    assertEquals(true, rangeAlarmCondition.evaluateState(100));

    assertEquals(true, rangeAlarmCondition.evaluateState(89.5f));
    assertEquals(false, rangeAlarmCondition.evaluateState(-10));
    assertEquals(false, rangeAlarmCondition.evaluateState(150));
  }

  @Test
  public void testOnlyMinRangeSet() throws ParserConfigurationException {
    RangeAlarmCondition<Integer> rangeAlarmCondition = new RangeAlarmCondition<>(0, null);
    assertEquals(true, rangeAlarmCondition.evaluateState(5));
    assertEquals(true, rangeAlarmCondition.evaluateState(0));
    assertEquals(true, rangeAlarmCondition.evaluateState(100));

    assertEquals(true, rangeAlarmCondition.evaluateState(89.5f));
    assertEquals(true, rangeAlarmCondition.evaluateState("89.5"));
    assertEquals(true, rangeAlarmCondition.evaluateState(true)); // true == 1

    assertEquals(false, rangeAlarmCondition.evaluateState(-10));
    assertEquals(true, rangeAlarmCondition.evaluateState(150));
  }

  @Test
  public void testOnlyMinRangeSetWithOutOfRangeFlag() throws ParserConfigurationException {
    RangeAlarmCondition<Integer> rangeAlarmCondition = new RangeAlarmCondition<>(0, null);
    rangeAlarmCondition.setOutOfRangeAlarm(true);

    assertEquals(false, rangeAlarmCondition.evaluateState(5));
    assertEquals(false, rangeAlarmCondition.evaluateState(0));
    assertEquals(false, rangeAlarmCondition.evaluateState(100));

    assertEquals(false, rangeAlarmCondition.evaluateState(89.5f));
    assertEquals(false, rangeAlarmCondition.evaluateState("89.5"));
    assertEquals(false, rangeAlarmCondition.evaluateState(true)); // true == 1

    assertEquals(true, rangeAlarmCondition.evaluateState(-10));
    assertEquals(true, rangeAlarmCondition.evaluateState(-10.4f));
    assertEquals(true, rangeAlarmCondition.evaluateState("-10"));
    assertEquals(false, rangeAlarmCondition.evaluateState(150));
  }

  @Test
  public void testOnlyMaxRangeSet() throws ParserConfigurationException {
    RangeAlarmCondition<Integer> rangeAlarmCondition = new RangeAlarmCondition<>(null, 100);
    assertEquals(true, rangeAlarmCondition.evaluateState(5));
    assertEquals(true, rangeAlarmCondition.evaluateState(0));
    assertEquals(true, rangeAlarmCondition.evaluateState(100));

    assertEquals(true, rangeAlarmCondition.evaluateState(89.5f));

    assertEquals(true, rangeAlarmCondition.evaluateState(-10));
    assertEquals(false, rangeAlarmCondition.evaluateState(150));
  }

  @Test
  public void testOnlyMaxRangeSetWithOutOfRangeFlag() throws ParserConfigurationException {
    RangeAlarmCondition<Integer> rangeAlarmCondition = new RangeAlarmCondition<>(null, 100);
    rangeAlarmCondition.setOutOfRangeAlarm(true);
    assertEquals(false, rangeAlarmCondition.evaluateState(5));
    assertEquals(false, rangeAlarmCondition.evaluateState(0));
    assertEquals(false, rangeAlarmCondition.evaluateState(100));

    assertEquals(false, rangeAlarmCondition.evaluateState(89.5f));

    assertEquals(false, rangeAlarmCondition.evaluateState(-10));
    assertEquals(true, rangeAlarmCondition.evaluateState(150));
  }

  @Test
  public void testRangeFloat() throws ParserConfigurationException {
    RangeAlarmCondition<Float> rangeAlarmCondition = new RangeAlarmCondition<>(0f, 100f);
    checkFloatConditions(rangeAlarmCondition, rangeAlarmCondition.isOutOfRangeAlarm());

    rangeAlarmCondition.setOutOfRangeAlarm(true);
    checkFloatConditions(rangeAlarmCondition, rangeAlarmCondition.isOutOfRangeAlarm());
  }

  @Test
  public void testXmlDeserialization() throws ParserConfigurationException {
    RangeAlarmCondition<Float> rangeAlarmCondition = new RangeAlarmCondition<>(0f, 100f);
    String xmlString = rangeAlarmCondition.toConfigXML();

    SimpleXMLParser parser = new SimpleXMLParser();
    Document document = parser.parse(xmlString);
    AlarmCondition condition = AlarmCondition.fromConfigXML(document.getDocumentElement());

    checkFloatConditions(condition, rangeAlarmCondition.isOutOfRangeAlarm());
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

  @Test
  public void testGetDescription() throws ParserConfigurationException {
    RangeAlarmCondition<Float> rangeAlarmCondition = new RangeAlarmCondition<>(0f, 100f);
    assertTrue(rangeAlarmCondition.getDescription().contains("ACTIVE"));
    log.trace(rangeAlarmCondition.getDescription());

    rangeAlarmCondition.setOutOfRangeAlarm(true);
    assertTrue(rangeAlarmCondition.getDescription().contains("TERMINATE"));
    log.trace(rangeAlarmCondition.getDescription());
  }

  @Test
  public void testEquals() throws ParserConfigurationException {
    RangeAlarmCondition<Float> rangeAlarmCondition1 = new RangeAlarmCondition<>(0f, 100f);
    RangeAlarmCondition<Float> rangeAlarmCondition2 = new RangeAlarmCondition<>(0f, 100f);
    RangeAlarmCondition<Integer> rangeAlarmCondition3 = new RangeAlarmCondition<>(0, 100);
    assertTrue(rangeAlarmCondition1.equals(rangeAlarmCondition2));
    assertFalse(rangeAlarmCondition1.equals(rangeAlarmCondition3));

    rangeAlarmCondition1.setOutOfRangeAlarm(true);
    assertFalse(rangeAlarmCondition1.equals(rangeAlarmCondition2));
    assertFalse(rangeAlarmCondition1.equals(rangeAlarmCondition3));

    rangeAlarmCondition2.setOutOfRangeAlarm(true);
    assertTrue(rangeAlarmCondition1.equals(rangeAlarmCondition2));


  }

  @Test
  public void testXmlDeserializationBackwardComp() throws ParserConfigurationException {
    String xmlString = "<AlarmCondition class=\"cern.c2mon.shared.client.alarm.condition.RangeAlarmCondition\">"
    + "<min-value type=\"Float\">0.0</min-value>"
    + "<max-value type=\"Float\">100.0</max-value>"
    + "</AlarmCondition>";

    SimpleXMLParser parser = new SimpleXMLParser();
    Document document = parser.parse(xmlString);
    AlarmCondition condition = AlarmCondition.fromConfigXML(document.getDocumentElement());
    checkFloatConditions(condition, false);
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
