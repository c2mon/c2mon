package cern.c2mon.daq.tools;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class DataTagValueFilterTest {
	DataTagValueFilter dvf = new DataTagValueFilter();
	
	@Test
	public void testIsAbsoluteValueDeadband() {
		// Issue 195
		assertTrue("Integer values should not be filtered if equal", dvf.isAbsoluteValueDeadband(1, 2, 1f));
		
	}

	@Test
	public void testIsRelativeValueDeadband() {
		// Issue 195
		assertTrue("Integer values should not be filtered if equal",dvf.isRelativeValueDeadband(1, 2, 100));
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
