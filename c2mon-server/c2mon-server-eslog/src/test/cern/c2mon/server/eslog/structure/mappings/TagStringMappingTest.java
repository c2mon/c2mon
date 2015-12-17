package cern.c2mon.server.eslog.structure.mappings;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;

/**
 * Tests the good bahaviour of the class TagStringMapping. Needed to do a good indexing in ElasticSearch.
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagStringMappingTest {

	@Test
	public void testGetStringMapping() {
		TagStringMapping mapping = new TagStringMapping(ValueType.stringType);
		String valueType = mapping.properties.getValueType();
		assertEquals(ValueType.stringType.toString(), valueType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void wrongGetStringMapping() {
		TagStringMapping mapping = new TagStringMapping(ValueType.dateType);
		mapping.properties.getValueType();
	}
}