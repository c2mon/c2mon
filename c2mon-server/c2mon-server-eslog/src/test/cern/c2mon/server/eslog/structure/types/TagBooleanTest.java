package cern.c2mon.server.eslog.structure.types;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;
import cern.c2mon.server.eslog.structure.mappings.TagBooleanMapping;

/**
 * Tests the good behaviour of the TagBoolean class. verify that it builds
 * correctly in JSON and accept/reject good/bad types of value.
 * 
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagBooleanTest {
  @InjectMocks
  TagBoolean tagBoolean;

  @Test
  public void testValue() {
    tagBoolean.setValue(true);

    assertEquals(true, tagBoolean.getValue());
    assertEquals(1, tagBoolean.getValueNumeric());
    assertTrue(tagBoolean.getValueBoolean());
    assertTrue(tagBoolean.getValue() instanceof Boolean);

    tagBoolean.setValue(false);

    assertEquals(false, tagBoolean.getValue());
    assertTrue(tagBoolean.getValue() instanceof Boolean);
    assertFalse(tagBoolean.getValueBoolean());
    assertEquals(0, tagBoolean.getValueNumeric());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadValue() {
    tagBoolean.setValue("NotBoolean");
  }


  @Test
  public void testBuild() throws IOException {
    tagBoolean.setDataType("boolean");
    String line = "\n  \"dataType\": \"boolean\",";
    String text = "{\n  \"id\": 0," + line
        + "\n  \"sourceTimestamp\": 0,\n  \"serverTimestamp\": 0,\n  \"daqTimestamp\": 0,\n  \"status\": 0\n" +
        "}";

    assertEquals(text, tagBoolean.build());
  }

  @Test
  public void testNullValue() {
    tagBoolean.setValue(null);
    assertNull(tagBoolean.getValue());
  }
}