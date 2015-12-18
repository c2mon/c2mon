package cern.c2mon.server.eslog.structure.types;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Timestamp;

import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagValueDictionary;
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
    tagNumeric.setValue(123);

    assertEquals(123, tagNumeric.getValue());
    assertTrue(tagNumeric.getValue() instanceof Integer);

    tagNumeric.setValue(1.23);

    assertEquals(1.23, tagNumeric.getValue());
    assertTrue(tagNumeric.getValue() instanceof Double);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadValue() {
    tagNumeric.setValue("notNumeric");
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
    String text = "{\n  \"id\": 0," + line
        + "\n  \"sourceTime\": 0,\n  \"serverTime\": 0,\n  \"daqTime\": 0,\n  \"status\": 0\n}";

    assertEquals(text, tagNumeric.build());
  }

  @Test
  public void testNullValue() {
    tagNumeric.setValue(null);
    assertNull(tagNumeric.getValue());
  }
}