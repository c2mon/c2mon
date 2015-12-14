package cern.c2mon.server.eslog.structure.mappings;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Verify that we always get a Mapping. Important for a good indexing in ElasticSearch.
 * @author Alban Marguet.
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TagESMappingTest {

	@Test
	public void testGetESMapping() {
		TagESMapping mapping = new TagESMapping();
		assertNotNull(mapping.getMapping());
	}
}