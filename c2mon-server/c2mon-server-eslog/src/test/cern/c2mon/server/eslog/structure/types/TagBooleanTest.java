package cern.c2mon.server.eslog.structure.types;

import cern.c2mon.server.eslog.structure.mappings.TagBooleanMapping;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Alban Marguet.
 */
@Slf4j
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

    @Test(expected=IllegalArgumentException.class)
    public void testBadValue() {
        tagBoolean.setTagValue("NotBoolean");
    }

    @Test
    public void testMapping() throws IOException {
        TagBooleanMapping mapping = new TagBooleanMapping("boolean");
        String expected = mapping.getMapping();
        tagBoolean.setMapping("boolean");
        assertEquals(expected, tagBoolean.getMapping());
    }

    @Test
    public void testBuild() throws IOException {
        tagBoolean.setDataType("boolean");
        String line = "\n  \"dataType\": \"boolean\",";
        String text = "{\n  \"metadataProcess\": {},\n  \"tagId\": 0," + line + "\n  \"tagTime\": 0,\n  \"tagServerTime\": 0,\n  \"tagDaqTime\": 0,\n  \"tagStatus\": 0\n}";
        assertEquals(text, tagBoolean.build());
    }
}