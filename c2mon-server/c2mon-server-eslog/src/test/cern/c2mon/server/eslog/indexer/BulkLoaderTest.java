package cern.c2mon.server.eslog.indexer;

import cern.c2mon.server.eslog.logger.Connector;
import cern.c2mon.server.eslog.logger.DataUtils;
import cern.c2mon.server.eslog.logger.TransportConnector;
import cern.c2mon.server.eslog.structure.queries.Query;
import cern.c2mon.server.eslog.structure.queries.QueryIndexBuilder;
import cern.c2mon.server.eslog.structure.types.TagES;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * @author Alban Marguet.
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class BulkLoaderTest {
    Set<String> indices = new HashSet<>();
    long id;
    String type;
    String notIndexed;
    String build;
    String notExistingAlias;

    @InjectMocks
    BulkLoader bulkLoader;

    @Mock
    DataUtils dataUtils;

    @Mock
    Client client;

    @Mock
    TagES tag;

    @Mock
    BulkProcessor bulkProcessor;

    @Mock
    Connector connector;

    @Before
    public void setup() {
        when(connector.getClient()).thenReturn(client);
        indices.add("c2mon_1");
        indices.add("c2mon_2");
        notIndexed = "c2mon_3";
        id = 1L;
        type = "tag_string";
        build = "";
        notExistingAlias = "tag_1";
        HashMap<String, Integer> bulkSettings = new HashMap<>();
        bulkSettings.put("bulkActions", 5600);
        bulkSettings.put("bulkSize", 5);
        bulkSettings.put("flushInterval", 1);
        bulkSettings.put("concurrent", 1);
        when(dataUtils.getBulkSettings()).thenReturn(bulkSettings);
        when(dataUtils.getMonthIndexSettings()).thenReturn(Settings.settingsBuilder());
        when(client.settings()).thenReturn(Settings.settingsBuilder().build());
        when(tag.toString()).thenReturn("tagES");
        when(tag.getTagValue()).thenReturn("value");
        when(dataUtils.getIndices()).thenReturn(indices);
        when(dataUtils.getConnector()).thenReturn(connector);
        when(tag.build()).thenReturn(build);
        when(tag.getTagId()).thenReturn(id);
        when(tag.getMapping()).thenReturn("");
        bulkLoader.init();
    }

    @Test
    public void testAdd() throws IOException {
        bulkLoader.add(notIndexed, type, tag.build(), tag);

        verify(dataUtils, times(2)).getIndices();
        indices.add(notIndexed);
        assertEquals(indices, dataUtils.getIndices());
    }

    @Test
    public void testInstantiateIndex() {
        when(tag.getMapping()).thenReturn("");
        boolean isInserted = bulkLoader.instantiateIndex(tag, notIndexed, type);

        verify(client).settings();
        verify(dataUtils).getMonthIndexSettings();
        //verify(connector).handleIndexQuery((QueryIndexBuilder) anyObject(), notIndexed, dataUtils.getMonthIndexSettings(), type, anyString());
        when(connector.handleIndexQuery((QueryIndexBuilder) anyObject(), anyString(), eq(dataUtils.getMonthIndexSettings()), anyString(), anyString())).thenReturn(true);
        assertTrue(isInserted);
    }

    @Test
    public void testAddAlias() {
        when(tag.getTagId()).thenReturn(1L);
        when(dataUtils.generateAliasName(anyLong())).thenReturn(notExistingAlias);
        when(dataUtils.getAliases()).thenReturn(indices); //does not contain the alias

        bulkLoader.addAlias(indices.iterator().next(), tag);

        verify(dataUtils.generateAliasName(tag.getTagId()));
        verify(dataUtils.getAliases(), times(2));
        verify(client.admin());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAliasNonExistingIndex() {
        when(tag.getTagId()).thenReturn(1L);
        when(dataUtils.generateAliasName(anyLong())).thenReturn(notExistingAlias);
        when(dataUtils.getAliases()).thenReturn(indices); //does not contain the alias
        when(dataUtils.getIndices()).thenReturn(indices); //does not contain the index

        bulkLoader.addAlias(notIndexed, tag);
    }

    @Test
    public void testAddExistingAlias() {
        when(tag.getTagId()).thenReturn(1L);
        when(dataUtils.generateAliasName(anyLong())).thenReturn(notExistingAlias);
        when(dataUtils.getAliases()).thenReturn(Collections.singleton(notExistingAlias)); //does contain the alias
        when(dataUtils.getIndices()).thenReturn(indices);

        boolean isInserted = bulkLoader.addAlias(indices.iterator().next(), tag);

        assertFalse(isInserted);
    }

    @Test
    public void testAddNewAlias() {
        when(tag.getTagId()).thenReturn(1L);
        when(dataUtils.generateAliasName(anyLong())).thenReturn(notExistingAlias);
        when(dataUtils.getAliases()).thenReturn(indices); //does not contain the alias
        when(dataUtils.getIndices()).thenReturn(indices);

        boolean isInserted = bulkLoader.addAlias(indices.iterator().next(), tag);

        assertTrue(isInserted);
    }
}
