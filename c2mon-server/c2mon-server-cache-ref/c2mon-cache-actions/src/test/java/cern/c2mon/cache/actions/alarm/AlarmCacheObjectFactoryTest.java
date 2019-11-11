package cern.c2mon.cache.actions.alarm;

import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Szymon Halastra
 */
public class AlarmCacheObjectFactoryTest {

  private AlarmService alarmService;

  private AlarmCacheObjectFactory factory;

  @Before
  public void init() {
    alarmService = EasyMock.createNiceMock(AlarmService.class);

    factory = new AlarmCacheObjectFactory();
  }

  @Test
  public void createAlarmCacheObjectWithProperties() throws IllegalAccessException {
    expect(alarmService.getTopicForAlarm(anyObject(AlarmCacheObject.class))).andReturn("tim.alarm");

    replay(alarmService);

    Properties properties = new Properties();
    properties.setProperty("dataTagId", "100");
    properties.setProperty("faultFamily", "fault-family");
    properties.setProperty("faultMember", "fault-member");
    properties.setProperty("alarmCondition", "<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\">\n" +
      "  <alarm-value type=\"String\">TERMINATE</alarm-value>\n" +
      "</AlarmCondition>\n");

    AlarmCacheObject alarm = (AlarmCacheObject) factory.createCacheObject(1L, properties);

    assertEquals("alarm should have dataTagId set", Long.valueOf(100L), alarm.getDataTagId());
    assertEquals("alarm should have faultFamily set", "fault-family", alarm.getFaultFamily());
    assertEquals("alarm should have faultMember set", "fault-member", alarm.getFaultMember());
    assertEquals("alarm should have alarm topic set", "tim.alarm", alarm.getTopic());
  }

  @Test
  public void throwExceptionWhenDataTagIdIsMissing() throws IllegalAccessException {
    Properties properties = new Properties();

    try {
      factory.createCacheObject(1L, properties);
      fail("exception should be thrown");
    } catch (ConfigurationException e) {
      assertEquals("INVALID_PARAMETER should be thrown", 0, e.getErrorCode());
    }
  }

  @Test
  public void throwExceptionWhenFaultFamilyIsMissing() throws IllegalAccessException {
    Properties properties = new Properties();
    properties.setProperty("dataTagId", "100");

    try {
      factory.createCacheObject(1L, properties);
      fail("exception should be thrown");
    } catch (ConfigurationException e) {
      assertEquals("INVALID_PARAMETER should be thrown", 0, e.getErrorCode());
    }
  }

  @Test
  public void throwExceptionWhenFaultMemberIsMissing() throws IllegalAccessException {
    Properties properties = new Properties();
    properties.setProperty("dataTagId", "100");
    properties.setProperty("faultFamily", "fault family");

    try {
      factory.createCacheObject(1L, properties);
      fail("exception should be thrown");
    } catch (ConfigurationException e) {
      assertEquals("INVALID_PARAMETER should be thrown", 0, e.getErrorCode());
    }
  }

  @Test
  public void throwExceptionWhenAlarmConditionIsMissing() throws IllegalAccessException {
    Properties properties = new Properties();
    properties.setProperty("dataTagId", "100");
    properties.setProperty("faultFamily", "fault-family");
    properties.setProperty("faultMember", "fault-member");

    try {
      factory.createCacheObject(1L, properties);
      fail("exception should be thrown");
    } catch (ConfigurationException e) {
      assertEquals("INVALID_PARAMETER should be thrown", 0, e.getErrorCode());
    }
  }
}
