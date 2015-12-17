package cern.c2mon.server.eslog.structure.mappings;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;

/**
 * Test the good behaviour of the TagNumericMapping class. We need a good mapping to index correctly the data in ElasticSearch.
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagNumericMappingTest {

	@Test
	public void testGetNumericMapping() {
		TagNumericMapping mapping = new TagNumericMapping(ValueType.intType);
		String valueType = mapping.properties.getValueType();
		assertTrue(ValueType.isNumeric(valueType));
	}

	@Test(expected = IllegalArgumentException.class)
	public void wrongGetNumericMapping() {
		TagNumericMapping mapping = new TagNumericMapping(ValueType.boolType);
		mapping.properties.getValueType();
	}
}