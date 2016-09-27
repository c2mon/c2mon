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
package cern.c2mon.client.core.tag;

import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import cern.c2mon.shared.client.expression.Expression;
import org.junit.Test;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;

/**
 * Tests for  {@link TagController#isValidUpdate(TagValueUpdate)}
 *
 * Covers all the cases described in the truth table of issue:
 * http://issues.cern.ch/browse/TIMS-826
 *
 * @author ekoufaki
 */
public class TagControllerIsValidUpdateTest {

  /** Just a random tag id for our tests */
  private final static long TAG_ID = 1234L;

  private final static Timestamp TIME_JUST_A_BIT_IN_THE_FUTURE
    = new Timestamp(System.currentTimeMillis() + 1000);

  private final static Timestamp CURRENT_TIME = new Timestamp(System.currentTimeMillis());

  private final static Timestamp TIME_JUST_A_BIT_AGO = new Timestamp(System.currentTimeMillis() - 1000);

  @Test
  /**
   * case1:
   * Update with newer server timestamp: always true
   */
  public void testCase1() {

    TagController tagController = new TagController(TAG_ID);

    TestTagValueUpdate tagValueUpdate_PAST = createTagVUpdate();
    tagValueUpdate_PAST.setServerTimestamp(TIME_JUST_A_BIT_AGO);

    TestTagValueUpdate tagValueUpdate_2 = createTagVUpdate();
    tagValueUpdate_2.setServerTimestamp(TIME_JUST_A_BIT_IN_THE_FUTURE);

    tagController.onUpdate(tagValueUpdate_PAST);
    final boolean status = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(status);

    // case b: let's make sure Source Timestamp
    // in the PAST does not make a difference..
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_AGO);
    final boolean statusB = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusB == true);

    // case c: let's make sure Source Timestamp
    // in the FUTURE does not make a difference..
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_IN_THE_FUTURE);
    final boolean statusC = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusC == true);

    // case d: let's make sure DAQ Timestamp
    // in the PAST does not make a difference..
    tagValueUpdate_2.setDaqTimestamp(TIME_JUST_A_BIT_AGO);
    final boolean statusD = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusD == true);

    // case e: let's make sure DAQ Timestamp
    // in the FUTURE does not make a difference..
    tagValueUpdate_2.setDaqTimestamp(TIME_JUST_A_BIT_IN_THE_FUTURE);
    final boolean statusE = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusE == true);

    // case f: let's make sure NULL DAQ Timestamp
    // does not make a difference..
    tagValueUpdate_2.setDaqTimestamp(null);
    final boolean statusF = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusF == true);

    // case g: let's make sure instance of TagUpdate
    // does not make a difference..
    TestTagUpdate tagUpdate_2 = createTagUpdate();
    tagUpdate_2.setServerTimestamp(TIME_JUST_A_BIT_IN_THE_FUTURE);
    tagUpdate_2.setDaqTimestamp(null);
    tagUpdate_2.setSourceTimestamp(null);
    final boolean statusG = tagController.isValidUpdate(tagUpdate_2);
    assertTrue(statusG == true);
  }

  @Test
  /**
   * case2:
   * Update with older/null server timestamp: always false
   */
  public void testCase2() {

    TagController tagController = new TagController(TAG_ID);

    TestTagValueUpdate tagValueUpdate_CURRENT = createTagVUpdate();
    tagValueUpdate_CURRENT.setServerTimestamp(CURRENT_TIME);

    TestTagValueUpdate tagValueUpdate_2 = createTagVUpdate();
    tagValueUpdate_2.setServerTimestamp(TIME_JUST_A_BIT_AGO);

    tagController.onUpdate(tagValueUpdate_CURRENT);
    final boolean status = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(status == false);

    // case b: let's make sure Source Timestamp
    // in the PAST does not make a difference..
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_AGO);
    final boolean statusB = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusB == false);

    // case c: let's make sure Source Timestamp
    // in the FUTURE does not make a difference..
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_IN_THE_FUTURE);
    final boolean statusC = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusC == false);

    // case d: let's make sure DAQ Timestamp
    // in the PAST does not make a difference..
    tagValueUpdate_2.setDaqTimestamp(TIME_JUST_A_BIT_AGO);
    final boolean statusD = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusD == false);

    // case e: let's make sure DAQ Timestamp
    // in the FUTURE does not make a difference..
    tagValueUpdate_2.setDaqTimestamp(TIME_JUST_A_BIT_IN_THE_FUTURE);
    final boolean statusE = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusE == false);

    // case f: let's make sure NULL DAQ Timestamp
    // does not make a difference..
    tagValueUpdate_2.setDaqTimestamp(null);
    final boolean statusF = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusF == false);

    // case g:let's make sure an update with NULL server timestamp is always ignored
    tagValueUpdate_2.setServerTimestamp(null);
    final boolean statusG = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusG == false);
  }

  @Test
  /**
   * case3:
   * Update with DAQ timestamp is preferred
   * in case the previous update arrived with no DAQ Timestamp.
   */
  public void testCase3() {

    TagController tagController = new TagController(TAG_ID);

    TestTagValueUpdate tagValueUpdate_1 = createTagVUpdate();
    tagValueUpdate_1.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setDaqTimestamp(null);

    TestTagValueUpdate tagValueUpdate_2 = createTagVUpdate();
    tagValueUpdate_2.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setDaqTimestamp(CURRENT_TIME);

    tagController.onUpdate(tagValueUpdate_1);
    final boolean status = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(status == true);

    // case b: let's make sure Source Timestamp
    // in the PAST does not make a difference..
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_AGO);
    final boolean statusB = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusB == true);

    // case c: let's make sure Source Timestamp
    // in the FUTURE does not make a difference..
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_IN_THE_FUTURE);
    final boolean statusC = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusC == true);
  }

  @Test
  /**
   * case4:
   * Update with NO DAQ timestamp
   *  when the previous update arrived WITH DAQ Timestamp, is ignored.
   */
  public void testCase4() {

    TagController tagController = new TagController(TAG_ID);

    TestTagValueUpdate tagValueUpdate_1 = createTagVUpdate();
    tagValueUpdate_1.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setDaqTimestamp(CURRENT_TIME);
    tagController.onUpdate(tagValueUpdate_1);

    TestTagValueUpdate tagValueUpdate_2 = createTagVUpdate();
    tagValueUpdate_2.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setDaqTimestamp(null);
    final boolean status = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(status == false);

    // case b: let's make sure Source Timestamp
    // in the PAST does not make a difference..
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_AGO);
    final boolean statusB = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusB == false);

    // case c: let's make sure Source Timestamp
    // in the FUTURE does not make a difference..
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_IN_THE_FUTURE);
    final boolean statusC = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusC == false);
  }

  @Test
  /**
   * case5:
   * Same Server timestamp, newer DAQ timestamp.
   */
  public void testCase5() {

    TagController tagController = new TagController(TAG_ID);

    TestTagValueUpdate tagValueUpdate_1 = createTagVUpdate();
    tagValueUpdate_1.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setDaqTimestamp(CURRENT_TIME);
    tagController.onUpdate(tagValueUpdate_1);

    TestTagValueUpdate tagValueUpdate_2 = createTagVUpdate();
    tagValueUpdate_2.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setDaqTimestamp(TIME_JUST_A_BIT_IN_THE_FUTURE);
    final boolean status = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(status == true);

    // case b: let's make sure Source Timestamp
    // in the PAST does not make a difference..
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_AGO);
    final boolean statusB = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusB == true);

    // case c: let's make sure Source Timestamp
    // in the FUTURE does not make a difference..
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_IN_THE_FUTURE);
    final boolean statusC = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusC == true);
  }

  @Test
  /**
   * case6:
   * Update with DAQ timestamp that is old..
   * Should not be accepted!
   */
  public void testCase6() {

    TagController tagController = new TagController(TAG_ID);

    TestTagValueUpdate tagValueUpdate_1 = createTagVUpdate();
    tagValueUpdate_1.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setDaqTimestamp(CURRENT_TIME);
    tagController.onUpdate(tagValueUpdate_1);

    // case a:
    TestTagValueUpdate tagValueUpdate_2 = createTagVUpdate();
    tagValueUpdate_2.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setDaqTimestamp(TIME_JUST_A_BIT_AGO);
    final boolean statusA = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusA == false);

    // case b: let's make sure Source Timestamp
    // in the PAST does not make a difference..
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_AGO);
    final boolean statusB = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusB == false);

    // case c: let's make sure Source Timestamp
    // in the FUTURE does not make a difference..
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_IN_THE_FUTURE);
    final boolean statusC = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusC == false);
  }

  @Test
  /**
   * case7: we accept a TagUpdate also when server & DAQ time are equals
   * but both source timestamps are not set
   */
  public void testCase7() {

    TagController tagController = new TagController(TAG_ID);

    TestTagUpdate tagValueUpdate_1 = createTagUpdate();
    tagValueUpdate_1.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setSourceTimestamp(null);

    TestTagUpdate tagValueUpdate_2 = createTagUpdate();
    tagValueUpdate_2.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setSourceTimestamp(null);

    tagController.onUpdate(tagValueUpdate_1);
    final boolean status = tagController.isValidUpdate(tagValueUpdate_2);

    assertTrue(status == true);
  }

  @Test
  /**
   * case8:
   */
  public void testCase8() {

    TagController tagController = new TagController(TAG_ID);

    TestTagUpdate tagValueUpdate_1 = createTagUpdate();
    tagValueUpdate_1.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setSourceTimestamp(null);

    TestTagUpdate tagValueUpdate_2 = createTagUpdate();
    tagValueUpdate_2.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setSourceTimestamp(null);

    tagController.onUpdate(tagValueUpdate_1);
    final boolean status = tagController.isValidUpdate(tagValueUpdate_2);

    assertTrue(status);
  }

  @Test
  /**
   * case9: Update with  Source  timestamp  is preferred
   * when the previous update arrived with no  Source  Timestamp.
   */
  public void testCase9() {

    TagController tagController = new TagController(TAG_ID);

    TestTagValueUpdate tagValueUpdate_1 = createTagVUpdate();
    tagValueUpdate_1.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setSourceTimestamp(null);
    tagController.onUpdate(tagValueUpdate_1);

    /**
     * Result should be the same for both cases a, b that follow.
     */
    // case a: Update is instance of TagValueUpdate
    TestTagValueUpdate tagValueUpdate_2A = createTagVUpdate();
    tagValueUpdate_2A.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2A.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_2A.setSourceTimestamp(CURRENT_TIME);

    final boolean statusA = tagController.isValidUpdate(tagValueUpdate_2A);
    assertTrue(statusA == true);

    // case b: Update is instance of TagUpdate
    TestTagUpdate tagValueUpdate_2B = createTagUpdate();
    tagValueUpdate_2B.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2B.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_2B.setSourceTimestamp(CURRENT_TIME);

    final boolean statusB = tagController.isValidUpdate(tagValueUpdate_2B);
    assertTrue(statusB == true);
  }

  @Test
  /**
   * case10: Update with NO Source  timestamp
   * when the previous update arrived WITH  Source  Timestamp, is ignored.
   */
  public void testCase10() {

    TagController tagController = new TagController(TAG_ID);

    TestTagValueUpdate tagValueUpdate_1 = createTagVUpdate();
    tagValueUpdate_1.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setSourceTimestamp(CURRENT_TIME);

    tagController.onUpdate(tagValueUpdate_1);

    /**
     * Result should be the same for both cases a, b that follow.
     */
    // case a: Update is instance of TagValueUpdate
    TestTagValueUpdate tagValueUpdate_2A = createTagVUpdate();
    tagValueUpdate_2A.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2A.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_2A.setSourceTimestamp(null);

    final boolean statusA = tagController.isValidUpdate(tagValueUpdate_2A);
    assertTrue(statusA == false);

    // case b: Update is instance of TagUpdate
    TestTagUpdate tagValueUpdate_2B = createTagUpdate();
    tagValueUpdate_2B.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2B.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_2B.setSourceTimestamp(null);

    final boolean statusB = tagController.isValidUpdate(tagValueUpdate_2B);
    assertTrue(statusB == false);
  }

  @Test
  /**
   * case11:
   */
  public void testCase11() {

    TagController tagController = new TagController(TAG_ID);

    TestTagUpdate tagValueUpdate_1 = createTagUpdate();
    tagValueUpdate_1.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setSourceTimestamp(CURRENT_TIME);

    TestTagUpdate tagValueUpdate_2 = createTagUpdate();
    tagValueUpdate_2.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setSourceTimestamp(CURRENT_TIME);

    tagController.onUpdate(tagValueUpdate_1);
    final boolean status = tagController.isValidUpdate(tagValueUpdate_2);

    assertTrue(status == true);
  }

  @Test
  /**
   * case12: Same DAQ timestamp, Same Source timestamp
   */
  public void testCase12() {

    TagController tagController = new TagController(TAG_ID);

    TestTagValueUpdate tagValueUpdate_1 = createTagVUpdate();
    tagValueUpdate_1.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setSourceTimestamp(CURRENT_TIME);

    TestTagValueUpdate tagValueUpdate_2 = createTagVUpdate();
    tagValueUpdate_2.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setSourceTimestamp(CURRENT_TIME);

    tagController.onUpdate(tagValueUpdate_1);
    final boolean status = tagController.isValidUpdate(tagValueUpdate_2);

    assertTrue(status == false);
  }

  @Test
  /**
   * case13: Same DAQ timestamp, Different Source timestamp
   */
  public void testCase13() {

    TagController tagController = new TagController(TAG_ID);

    TestTagValueUpdate tagValueUpdate_1 = createTagVUpdate();
    tagValueUpdate_1.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setDaqTimestamp(CURRENT_TIME);
    tagValueUpdate_1.setSourceTimestamp(CURRENT_TIME);

    /**
     * Source Timestamp just has to be different: whether it is in the future or the past doesn't matter!
     */
    tagController.onUpdate(tagValueUpdate_1);

    TestTagValueUpdate tagValueUpdate_2 = createTagVUpdate();
    tagValueUpdate_2.setServerTimestamp(CURRENT_TIME);
    tagValueUpdate_2.setDaqTimestamp(CURRENT_TIME);

    // case a: new source timestamp in the future
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_IN_THE_FUTURE);
    final boolean statusA = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusA == true);

    // case b: new source timestamp in the past
    tagValueUpdate_2.setSourceTimestamp(TIME_JUST_A_BIT_AGO);
    tagController.onUpdate(tagValueUpdate_1);
    final boolean statusB = tagController.isValidUpdate(tagValueUpdate_2);
    assertTrue(statusB == true);
  }

  private TestTagValueUpdate createTagVUpdate() {

    TestTagValueUpdate tagUpdate = new TestTagValueUpdate(new Timestamp(System.currentTimeMillis() - 10000L), null, null, "java.lang.String");
    return tagUpdate;
  }

  private TestTagUpdate createTagUpdate() {

    TestTagUpdate tagUpdate = new TestTagUpdate(new Timestamp(System.currentTimeMillis() - 10000L), null, null, "java.lang.String");

    return tagUpdate;
  }

  /**
   * Helper class for the tests above.
   */
  private class TestTagValueUpdate implements TagValueUpdate {

    private Timestamp sourceTimestamp;

    private Timestamp daqTimestamp;

    private Timestamp serverTimestamp;

    private String valueClassName;

    private final Long id = 1234L;

    public TestTagValueUpdate(final Timestamp sourceTimestamp, final Timestamp daqTimestamp, final Timestamp serverTimestamp, final String valueClassName) {

      this.sourceTimestamp = sourceTimestamp;
      this.daqTimestamp = daqTimestamp;
      this.serverTimestamp = serverTimestamp;
      this.valueClassName = valueClassName;
    }

    public void setServerTimestamp(Timestamp serverTimestamp) {
      this.serverTimestamp = serverTimestamp;
    }

    public void setSourceTimestamp(Timestamp sourceTimestamp) {
      this.sourceTimestamp = sourceTimestamp;
    }

    public void setDaqTimestamp(Timestamp daqTimestamp) {
      this.daqTimestamp = daqTimestamp;
    }

    @Override
    public Long getId() {
      return id;
    }

    @Override
    public DataTagQuality getDataTagQuality() {
      return new DataTagQualityImpl();
    }

    @Override
    public Object getValue() {
      return null;
    }

    @Override
    public Timestamp getSourceTimestamp() {
      return sourceTimestamp;
    }

    @Override
    public Timestamp getDaqTimestamp() {
      return daqTimestamp;
    }

    @Override
    public Timestamp getServerTimestamp() {
      return serverTimestamp;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Collection<AlarmValue> getAlarms() {
      return new ArrayList<AlarmValue>();
    }

    @Override
    public TagMode getMode() {
      return null;
    }

    @Override
    public boolean isSimulated() {
      return false;
    }

    @Override
    public String getValueDescription() {
      return null;
    }

    @Override
    public String getValueClassName() {
      return this.valueClassName;
    }

    @Override
    public Collection<Expression> getExpressions() {
      return null;
    }

    @Override
    public void setValue(Object arg) {
    }
  }

  /**
   * Helper class for the tests above.
   */
  private class TestTagUpdate implements TagUpdate {

    private Timestamp sourceTimestamp;

    private Timestamp daqTimestamp;

    private Timestamp serverTimestamp;

    private String valueClassName;

    private final Long id = 1234L;

    public TestTagUpdate(final Timestamp sourceTimestamp, final Timestamp daqTimestamp, final Timestamp serverTimestamp, final String valueClassName) {

      this.sourceTimestamp = sourceTimestamp;
      this.daqTimestamp = daqTimestamp;
      this.serverTimestamp = serverTimestamp;
      this.valueClassName = valueClassName;
    }

    public void setServerTimestamp(Timestamp serverTimestamp) {
      this.serverTimestamp = serverTimestamp;
    }

    public void setSourceTimestamp(Timestamp sourceTimestamp) {
      this.sourceTimestamp = sourceTimestamp;
    }

    public void setDaqTimestamp(Timestamp daqTimestamp) {
      this.daqTimestamp = daqTimestamp;
    }

    @Override
    public Long getId() {
      return id;
    }

    @Override
    public DataTagQuality getDataTagQuality() {
      return new DataTagQualityImpl();
    }

    @Override
    public Object getValue() {
      return null;
    }

    @Override
    public Timestamp getSourceTimestamp() {
      return sourceTimestamp;
    }

    @Override
    public Timestamp getDaqTimestamp() {
      return daqTimestamp;
    }

    @Override
    public Timestamp getServerTimestamp() {
      return serverTimestamp;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Collection<AlarmValue> getAlarms() {
      return new ArrayList<AlarmValue>();
    }

    @Override
    public TagMode getMode() {
      return null;
    }

    @Override
    public boolean isSimulated() {
      return false;
    }

    @Override
    public String getValueDescription() {
      return null;
    }

    @Override
    public String getValueClassName() {
      return this.valueClassName;
    }

    @Override
    public Collection<Expression> getExpressions() {
      return null;
    }

    @Override
    public void setValue(Object arg) {

    }

    @Override
    public Collection<Long> getProcessIds() {
      return null;
    }

    @Override
    public Collection<Long> getEquipmentIds() {
      return null;
    }

    @Override
    public Collection<Long> getSubEquipmentIds() {
      return null;
    }

    @Override
    public String getUnit() {
      return null;
    }

    @Override
    public String getRuleExpression() {
      return null;
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public String getTopicName() {
      return null;
    }

    @Override
    public boolean isControlTag() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isAliveTag() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public Map<String, Object> getMetadata(){
      return null;
    }
  }
}
