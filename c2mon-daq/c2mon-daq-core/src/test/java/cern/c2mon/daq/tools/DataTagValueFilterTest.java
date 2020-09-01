package cern.c2mon.daq.tools;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;


import org.junit.Ignore;
import org.junit.Test;

import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.common.datatag.util.JmsMessagePriority;
import cern.c2mon.shared.common.datatag.util.SourceDataTagQualityCode;
import cern.c2mon.shared.common.datatag.util.ValueDeadbandType;
import cern.c2mon.shared.common.filter.FilteredDataTagValue.FilterType;

public class DataTagValueFilterTest {
  DataTagValueFilter dvf = new DataTagValueFilter();
  
  private static final float VALUE_DEADBAND = 25.0f;

  @Test
  public void testIsAbsoluteValueDeadband() {
    // Most basic sanity test
    assertTrue("Lossless precision float values should be filtered", dvf.isAbsoluteValueDeadband(0f, 1f, 1.1f));
    assertTrue("Lossless precision int values should be filtered", dvf.isAbsoluteValueDeadband(0, 1, 2.0f));
    assertFalse("Lossless precision int values should not be filtered", dvf.isAbsoluteValueDeadband(0, 1, 1.0f));
    assertTrue("Lossless precision byte values should be filtered", dvf.isAbsoluteValueDeadband((byte) 0, (byte) 1, 1.1f));
    assertTrue("Lossless precision long values should be filtered", dvf.isAbsoluteValueDeadband(0l, 1l, 1.1f));
    assertTrue("Lossless precision double values should be filtered", dvf.isAbsoluteValueDeadband(0.0, 1.0, 1.1f));

    // A few sanity tests
    assertFalse("Lossless precision float values should not be filtered", dvf.isAbsoluteValueDeadband(88888f, 88889f, 1f));
    assertFalse("Lossless precision float values should not be filtered", dvf.isAbsoluteValueDeadband(0f, 1f, 1f));
    assertFalse("Lossless precision float values should not be filtered", dvf.isAbsoluteValueDeadband(88888.68f, 88889.68f, 1f));

    // Checking that any float value that might cause integer precision loss is not
    // evaluated for deadband filtering
    assertFalse("Integer values expressed over the max float precision float ", dvf.isAbsoluteValueDeadband(167772119f, 167772120f, 1.1f));

    // Checking that any long value that might cause double precision loss is not
    // evaluated for deadband filtering
    assertFalse("Long values expressed over the max  double precision should not be filtered in any case",
        dvf.isAbsoluteValueDeadband(Long.MAX_VALUE - 1, Long.MAX_VALUE, 1.1f));

  }

  @Test
  public void testIsRelativeValueDeadband() {
    assertTrue("Integer value 167772119 should be flagged as causing a loss of precision", DataTagValueFilter.willCausePrecisionLoss(167772119));

    // Most basic sanity test
    assertFalse("Incoming zero value should never get evaluated", dvf.isRelativeValueDeadband(0f, 1f, 110));
    assertTrue("Lossless precision float values should be filtered", dvf.isRelativeValueDeadband(1f, 2f, 110));
    assertTrue("Lossless precision int values should be filtered", dvf.isRelativeValueDeadband(1, 2, 200));
    assertFalse("Lossless precision int values should not be filtered", dvf.isRelativeValueDeadband(1, 2, 100));
    assertTrue("Lossless precision byte values should be filtered", dvf.isRelativeValueDeadband((byte) 1, (byte) 2, 110));
    assertTrue("Lossless precision long values should be filtered", dvf.isRelativeValueDeadband(1l, 2l, 110));
    assertTrue("Lossless precision double values should be filtered", dvf.isRelativeValueDeadband(1.0, 2.0, 110));

    // A few sanity tests
    assertFalse("Lossless precision float values should not be filtered", dvf.isRelativeValueDeadband(100f, 200f, 100));
    assertFalse("Lossless precision float values should not be filtered", dvf.isRelativeValueDeadband(1f, 2f, 100));

    // Checking that any float value that might cause integer precision loss is not
    // evaluated for deadband filtering
    assertFalse("Integer values expressed over the max float precision float should not be filtered in any case",
        dvf.isRelativeValueDeadband(167772119, 167772120, 10000));

    // Checking that any long value that might cause double precision loss is not
    // evaluated for deadband filtering
    assertFalse("Long values expressed over the max  double precision should not be filtered in any case",
        dvf.isRelativeValueDeadband(Long.MAX_VALUE - 1, Long.MAX_VALUE, 110));

  }

  @Test
  @Ignore("Reactivate me once issue 196 is fixed")
  public void testDeadbandAsAFloatIssue() {
    float deadband = 0.9999999f;
    // The following should not trigger the deadband, since 1 > 0.9999999f
    assertFalse("Integer floats should be handled okay", dvf.isAbsoluteValueDeadband(4.9f, 3.9f, deadband));
    // The following should also not trigger the deadband, since 1 > 0.9999999f
    // However this assertion fails, due to the float to double conversion's loss of
    // precision.
    assertFalse("Defining the deadband as a float introduces a loss of precision !", dvf.isAbsoluteValueDeadband(3.2f, 4.2f, deadband));
  }

  @Test
  public void testIsCandidateForFiltering() {
    SourceDataTag sdt1 = createSourceDataTag(1L, "sdt1", "Boolean", ValueDeadbandType.PROCESS_RELATIVE, 0, JmsMessagePriority.PRIORITY_LOW, false);
    ValueUpdate currentValue = new ValueUpdate(false, System.currentTimeMillis() - 100);
    sdt1.update(currentValue);
    
    SourceDataTagQuality qualityGood = new SourceDataTagQuality();
    SourceDataTagQuality qualityBad = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE);
    
    
    // Different Boolean value
    ValueUpdate valueUpdate1 = new ValueUpdate(true);
    FilterType filterReason = dvf.isCandidateForFiltering(sdt1, valueUpdate1, qualityGood);
    assertEquals(FilterType.NO_FILTERING, filterReason);
    
    // Repeated value
    ValueUpdate valueUpdate2 = new ValueUpdate(false);
    filterReason = dvf.isCandidateForFiltering(sdt1, valueUpdate2, qualityGood);
    assertEquals(FilterType.REPEATED_VALUE, filterReason);
    
    // Different Boolean value
    filterReason = dvf.isCandidateForFiltering(sdt1, valueUpdate1, qualityBad);
    assertEquals(FilterType.NO_FILTERING, filterReason);
    
    // Repeated value
    filterReason = dvf.isCandidateForFiltering(sdt1, valueUpdate2, qualityBad);
    assertEquals(FilterType.NO_FILTERING, filterReason);
  }
  
  private SourceDataTag createSourceDataTag(long id, String name, String dataType, ValueDeadbandType deadBandType, int timeDeadband, JmsMessagePriority priority, boolean guaranteed) {
    DataTagAddress address = new DataTagAddress(100, deadBandType, VALUE_DEADBAND, timeDeadband, priority, guaranteed);
    SourceDataTagValue sdtValue = new SourceDataTagValue(id, name, false);
    sdtValue.setTimestamp(new Timestamp(System.currentTimeMillis() - 1000));
    SourceDataTag sdt = new SourceDataTag(id, name, false, DataTagConstants.MODE_OPERATIONAL, dataType, address);
//    sdt.setCurrentValue(sdtValue);
    return sdt;
  }

}
