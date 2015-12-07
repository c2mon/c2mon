package cern.c2mon.server.eslog.structure.mappings;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * @author Alban Marguet.
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TagBooleanMappingTest {
    @Test
    public void testGetBooleanMapping() {
        TagBooleanMapping mapping = new TagBooleanMapping(Mapping.boolType);
        String valueType = mapping.properties.getValueType();
        assertEquals(Mapping.boolType, valueType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongGetBooleanMapping() {
        TagBooleanMapping mapping = new TagBooleanMapping(Mapping.stringType);
        mapping.properties.getValueType();
    }
}
