package cern.c2mon.daq.tools;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class DataTagValueFilterTest {
	DataTagValueFilter dvf = new DataTagValueFilter();
	
	@Test
	public void testIsAbsoluteValueDeadband() {
		// Most basic sanity test
		assertTrue("Lossless precision float values should be filtered", dvf.isAbsoluteValueDeadband(0f, 1f, 1.1f));
		assertTrue("Lossless precision int values should be filtered", dvf.isAbsoluteValueDeadband(0, 1, 2.0f));
		assertFalse("Lossless precision int values should not be filtered", dvf.isAbsoluteValueDeadband(0, 1, 1.0f));
		assertTrue("Lossless precision byte values should be filtered", dvf.isAbsoluteValueDeadband((byte)0, (byte)1, 1.1f));
		assertTrue("Lossless precision long values should be filtered", dvf.isAbsoluteValueDeadband(0l, 1l, 1.1f));
		assertTrue("Lossless precision double values should be filtered", dvf.isAbsoluteValueDeadband(0.0, 1.0, 1.1f));
		
		// A few sanity tests
		assertFalse("Lossless precision float values should not be filtered", dvf.isAbsoluteValueDeadband(88888f, 88889f, 1f));
		assertFalse("Lossless precision float values should not be filtered", dvf.isAbsoluteValueDeadband(0f, 1f, 1f));
		assertFalse("Lossless precision float values should not be filtered", dvf.isAbsoluteValueDeadband(88888.68f, 88889.68f, 1f));
		
		// Checking that any float value that might cause integer precision loss is not evaluated for deadband filtering
		assertFalse("Integer values expressed over the max float precision float ", dvf.isAbsoluteValueDeadband(167772119f, 167772120f, 1.1f));

		// Checking that any long value that might cause double precision loss is not evaluated for deadband filtering
		assertFalse("Long values expressed over the max  double precision should not be filtered in any case", dvf.isAbsoluteValueDeadband(Long.MAX_VALUE-1, Long.MAX_VALUE, 1.1f));
		
	}



	@Test
	public void testIsRelativeValueDeadband() {
		assertTrue("Integer value 167772119 should be flagged as causing a loss of precision", DataTagValueFilter.willCausePrecisionLoss(167772119));
		
		// Most basic sanity test
		assertFalse("Incoming zero value should never get evaluated", dvf.isRelativeValueDeadband(0f, 1f, 110));
		assertTrue("Lossless precision float values should be filtered", dvf.isRelativeValueDeadband(1f, 2f, 110));
		assertTrue("Lossless precision int values should be filtered", dvf.isRelativeValueDeadband(1, 2, 200));
		assertFalse("Lossless precision int values should not be filtered", dvf.isRelativeValueDeadband(1, 2, 100));
		assertTrue("Lossless precision byte values should be filtered", dvf.isRelativeValueDeadband((byte)1, (byte)2, 110));
		assertTrue("Lossless precision long values should be filtered", dvf.isRelativeValueDeadband(1l, 2l, 110));
		assertTrue("Lossless precision double values should be filtered", dvf.isRelativeValueDeadband(1.0, 2.0, 110));
		
		// A few sanity tests
		assertFalse("Lossless precision float values should not be filtered", dvf.isRelativeValueDeadband(100f, 200f, 100));
		assertFalse("Lossless precision float values should not be filtered", dvf.isRelativeValueDeadband(1f, 2f, 100));
		
		// Checking that any float value that might cause integer precision loss is not evaluated for deadband filtering
		assertFalse("Integer values expressed over the max float precision float should not be filtered in any case", dvf.isRelativeValueDeadband(167772119, 167772120, 10000));

		// Checking that any long value that might cause double precision loss is not evaluated for deadband filtering
		assertFalse("Long values expressed over the max  double precision should not be filtered in any case", dvf.isRelativeValueDeadband(Long.MAX_VALUE-1, Long.MAX_VALUE, 110));
		
	}
	
	@Test
	@Ignore("Reactivate me once issue 196 is fixed")
	public void testDeadbandAsAFloatIssue() {
		float deadband = 0.9999999f;
		// The following should not trigger the deadband, since 1 > 0.9999999f
		assertFalse("Integer floats should be handled okay",dvf.isAbsoluteValueDeadband(4.9f, 3.9f, deadband));
		// The following should also not trigger the deadband, since 1 > 0.9999999f
		// However this assertion fails, due to the float to double conversion's loss of precision.
		assertFalse("Defining the deadband as a float introduces a loss of precision !",dvf.isAbsoluteValueDeadband(3.2f, 4.2f, deadband));
	}

}
