package cern.c2mon.server.eslog.structure.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;
import cern.c2mon.server.eslog.structure.mappings.TagNumericMapping;

/**
 * Tests the good behaviour of the TagNumeric class. verify that it builds
 * correctly in JSON and accept/reject good/bad types of value.
 * 
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagNumericTest {

  @InjectMocks
  TagNumeric tagNumeric;

  @Test
  public void testValue() {
    tagNumeric.setTagValue(123);

    assertEquals(123, tagNumeric.getTagValue());
    assertTrue(tagNumeric.getTagValue() instanceof Integer);

    tagNumeric.setTagValue(1.23);

    assertEquals(1.23, tagNumeric.getTagValue());
    assertTrue(tagNumeric.getTagValue() instanceof Double);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadValue() {
    tagNumeric.setTagValue("notNumeric");
  }

  @Test
  public void testMapping() throws IOException {
    TagNumericMapping mapping = new TagNumericMapping(ValueType.doubleType);
    String expected = mapping.getMapping();
    tagNumeric.setMapping(ValueType.doubleType);

    assertEquals(expected, tagNumeric.getMapping());
  }

  @Test
  public void testBuild() throws IOException {
    tagNumeric.setDataType("numeric");
    String line = "\n  \"dataType\": \"numeric\",";
    String text = "{\n  \"metadataProcess\": {},\n  \"tagId\": 0," + line
        + "\n  \"tagTime\": 0,\n  \"tagServerTime\": 0,\n  \"tagDaqTime\": 0,\n  \"tagStatus\": 0\n}";

    assertEquals(text, tagNumeric.build());
  }
}
