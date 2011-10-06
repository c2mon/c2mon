package ch.cern.tim.driver.jec.tools;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.cern.tim.driver.jec.tools.JECConversionHelper;

public class JECConversionHelperTest {
    private static final float EPSILON = 0.0001f;
    private float TEST_FLOAT = 25.0234f;
    
    @Test
    public void testIEEEConversion() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(TEST_FLOAT , 0, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 0, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + TEST_FLOAT + " Restored: " + restoredJavaValue,TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testIEEEConversionNegative() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(-TEST_FLOAT , 0, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 0, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + (-TEST_FLOAT) + " Restored: " + restoredJavaValue,-TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testRaw1Conversion() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(TEST_FLOAT, 1, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 1, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + TEST_FLOAT + " Restored: " + restoredJavaValue,TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testRaw2Conversion() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(TEST_FLOAT, 2, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 2, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + TEST_FLOAT + " Restored: " + restoredJavaValue,TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testRaw3Conversion() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(TEST_FLOAT, 3, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 3, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + TEST_FLOAT + " Restored: " + restoredJavaValue,TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testRaw4Conversion() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(TEST_FLOAT, 4, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 4, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + TEST_FLOAT + " Restored: " + restoredJavaValue,TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testRaw5Conversion() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(TEST_FLOAT, 5, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 5, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + TEST_FLOAT + " Restored: " + restoredJavaValue, TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testRaw6Conversion() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(TEST_FLOAT, 6, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 6, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + TEST_FLOAT + " Restored: " + restoredJavaValue, TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testRaw1ConversionNegative() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(-TEST_FLOAT, 1, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 1, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + (-TEST_FLOAT) + " Restored: " + restoredJavaValue, -TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testRaw2ConversionNegative() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(-TEST_FLOAT, 2, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 2, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + (-TEST_FLOAT) + " Restored: " + restoredJavaValue, -TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testRaw3ConversionNegative() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(-TEST_FLOAT, 3, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 3, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + (-TEST_FLOAT) + " Restored: " + restoredJavaValue, -TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testRaw4ConversionNegative() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(-TEST_FLOAT, 4, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 4, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + (-TEST_FLOAT) + " Restored: " + restoredJavaValue, -TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testRaw5ConversionNegative() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(-TEST_FLOAT, 5, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 5, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + (-TEST_FLOAT) + " Restored: " + restoredJavaValue, -TEST_FLOAT - restoredJavaValue < EPSILON);
    }
    
    @Test
    public void testRaw6ConversionNegative() {
        int plcValue = JECConversionHelper.convertJavaToPLCValue(-TEST_FLOAT, 6, 0.1f, 0.8f);
        float restoredJavaValue = JECConversionHelper.convertPLCValueToFloat(plcValue, 6, 0.1f, 0.8f);
        assertTrue("PLCValue " + plcValue + " JavaFloat: " + (-TEST_FLOAT) + " Restored: " + restoredJavaValue, -TEST_FLOAT - restoredJavaValue < EPSILON);
    }
}
