package cern.c2mon.server.eslog.structure.mappings;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Test the good behaviour of the TagNumericMapping class. We need a good mapping to index correctly the data in ElasticSearch.
 * @author Alban Marguet.
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TagNumericMappingTest {

	@Test
	public void testGetNumericMapping() {
		TagNumericMapping mapping = new TagNumericMapping(Mapping.intType);
		String valueType = mapping.properties.getValueType();
		assertTrue(Mapping.doubleType.compareTo(valueType) == 0 || Mapping.intType.compareTo(valueType) == 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void wrongGetNumericMapping() {
		TagNumericMapping mapping = new TagNumericMapping(Mapping.boolType);
		mapping.properties.getValueType();
	}
}