package cern.c2mon.server.eslog.structure.mappings;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Tests the good bahaviour of the class TagStringMapping. Needed to do a good indexing in ElasticSearch.
 * @author Alban Marguet.
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TagStringMappingTest {

	@Test
	public void testGetStringMapping() {
		TagStringMapping mapping = new TagStringMapping(Mapping.stringType);
		String valueType = mapping.properties.getValueType();
		assertEquals(Mapping.stringType, valueType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void wrongGetStringMapping() {
		TagStringMapping mapping = new TagStringMapping(Mapping.dateType);
		mapping.properties.getValueType();
	}
}