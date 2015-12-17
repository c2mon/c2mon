package cern.c2mon.server.eslog.structure.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;
import cern.c2mon.server.eslog.structure.mappings.TagStringMapping;

/**
 * Tests the good behaviour of the TagString class.
 * verify that it builds correctly in JSON and accept/reject good/bad types of value.
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagStringTest {
	@InjectMocks
	TagString tagString;

	@Test
	public void testValue() {
		tagString.setTagValue("test");

		assertEquals("test", tagString.getTagValue());
		assertTrue(tagString.getTagValue() instanceof String);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadValue() {
		tagString.setTagValue(123456789);
	}

	@Test
	public void testMapping() throws IOException {
		TagStringMapping mapping = new TagStringMapping(ValueType.stringType);
		String expected = mapping.getMapping();
		tagString.setMapping(ValueType.stringType);

		assertEquals(expected, tagString.getMapping());
	}

	@Test
	public void testBuild() throws IOException {
		tagString.setQuality("ok");
		String line = "\n  \"quality\": \"ok\"";
		String text = "{\n  \"metadataProcess\": {},\n  \"tagId\": 0,\n  \"tagTime\": 0,\n  \"tagServerTime\": 0,\n  \"tagDaqTime\": 0,\n  \"tagStatus\": 0," + line + "\n}";

		assertEquals(text, tagString.build());
	}
}