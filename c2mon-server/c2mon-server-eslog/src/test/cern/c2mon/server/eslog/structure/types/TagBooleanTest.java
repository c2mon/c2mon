package cern.c2mon.server.eslog.structure.types;

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
    tagBoolean.setTagValue(true);

    assertEquals(true, tagBoolean.getTagValue());
    assertTrue(tagBoolean.getTagValue() instanceof Boolean);

    tagBoolean.setTagValue(false);

    assertEquals(false, tagBoolean.getTagValue());
    assertTrue(tagBoolean.getTagValue() instanceof Boolean);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadValue() {
    tagBoolean.setTagValue("NotBoolean");
  }

  @Test
  public void testMapping() throws IOException {
    TagBooleanMapping mapping = new TagBooleanMapping(ValueType.boolType);
    String expected = mapping.getMapping();
    tagBoolean.setMapping(ValueType.boolType);

    assertEquals(expected, tagBoolean.getMapping());
  }

  @Test
  public void testBuild() throws IOException {
    tagBoolean.setDataType("boolean");
    String line = "\n  \"dataType\": \"boolean\",";
    String text = "{\n  \"metadataProcess\": {},\n  \"tagId\": 0," + line
        + "\n  \"tagTime\": 0,\n  \"tagServerTime\": 0,\n  \"tagDaqTime\": 0,\n  \"tagStatus\": 0\n}";

    assertEquals(text, tagBoolean.build());
  }
}