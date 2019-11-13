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
package cern.c2mon.server.test;

import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.test.cache.*;

/**
 * Contains static methods for creating test cache objects.
 *
 * @author Mark Brightwell
 * @see AbstractCacheObjectFactory use this for the type safe specific samples
 */
public final class CacheObjectCreation {
  private static final AlarmCacheObjectFactory alarmCacheObjectFactory;
  private static final ControlTagCacheObjectFactory controlTagCacheObjectFactory;
  private static final DataTagCacheObjectFactory dataTagCacheObjectFactory;
  private static final CommandTagCacheObjectFactory commandTagCacheObjectFactory;
  private static final RuleTagCacheObjectFactory ruleTagCacheObjectFactory;

  static {
    alarmCacheObjectFactory = new AlarmCacheObjectFactory();
    controlTagCacheObjectFactory = new ControlTagCacheObjectFactory();
    dataTagCacheObjectFactory = new DataTagCacheObjectFactory();
    commandTagCacheObjectFactory = new CommandTagCacheObjectFactory();
    ruleTagCacheObjectFactory = new RuleTagCacheObjectFactory();
  }

  /**
   * Hide constructor.
   */
  private CacheObjectCreation() {
  }

  /**
   * Does not set reference to tag id.
   *
   * @return the alarm
   */
  public static AlarmCacheObject createTestAlarm1() {
    return alarmCacheObjectFactory.sampleBase();
  }

  /**
   * Does not set reference to tag id.
   *
   * @return the alarm
   */
  public static AlarmCacheObject createTestAlarm3() {
    return alarmCacheObjectFactory.alarmWithOtherId();
  }

  /**
   * Does not set reference to tag id.
   * Created alarm has not been published to external alarm system (i.e. publication field is null)
   *
   * @return the alarm
   */
  public static AlarmCacheObject createTestAlarm2() {
    return alarmCacheObjectFactory.alarmActiveWithFalseCondition();
  }

  /**
   * Constructs a test ControlTag.
   *
   * @return the ControlTag
   */
  public static ControlTagCacheObject createTestControlTag() {
    return controlTagCacheObjectFactory.sampleBase();
  }

  /**
   * Need to first insert test equipment using EquipmentMapper
   *
   * @return the DataTag
   */
  public static DataTagCacheObject createTestDataTag() {
    return dataTagCacheObjectFactory.sampleBase();
  }

  /**
   * Constructs second DataTag.
   *
   * @return the DataTag.
   */
  public static DataTagCacheObject createTestDataTag2() {
    return dataTagCacheObjectFactory.sample2();
  }

  /**
   * Need to first insert test equipment using EquipmentMapper
   *
   * @return the DataTag
   */
  public static DataTagCacheObject createTestDataTag3() {
    //construct fake DataTagCacheObject, setting all fields
    return dataTagCacheObjectFactory.sampleDown();
  }

  /**
   * Returns a test rule tag object
   *
   * @return the RuleTag
   */
  public static RuleTagCacheObject createTestRuleTag() {
    return ruleTagCacheObjectFactory.sampleBase();
  }

  /**
   * Creates an AliveTimer for a Process
   *
   * @return the ControlTag
   */
  public static ControlTagCacheObject createTestProcessAlive() {
    return controlTagCacheObjectFactory.createTestProcessAlive();
  }

  /**
   * Creates a test CommandTag
   *
   * @return the test CommandTag
   */
  public static CommandTagCacheObject createTestCommandTag() {
    return commandTagCacheObjectFactory.sampleBase();
  }

}
