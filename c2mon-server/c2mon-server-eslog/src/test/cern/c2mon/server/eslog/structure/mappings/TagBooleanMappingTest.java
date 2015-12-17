package cern.c2mon.server.eslog.structure.mappings;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;

/**
 * Verify the good behaviour of the TagBooleanMapping class.
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagBooleanMappingTest {

	@Test
	public void testGetBooleanMapping() {
		TagBooleanMapping mapping = new TagBooleanMapping(ValueType.boolType);
		String valueType = mapping.properties.getValueType();
		assertEquals(ValueType.boolType.toString(), valueType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void wrongGetBooleanMapping() {
		TagBooleanMapping mapping = new TagBooleanMapping(ValueType.stringType);
		mapping.properties.getValueType();
	}
}