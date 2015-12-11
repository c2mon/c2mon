package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.structure.types.TagES;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alban Marguet.
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DataUtilsTest {
    String INDEX_PREFIX = "c2mon_";
    String TAG_PREFIX = "tag_";
    String FIRST_INDEX = INDEX_PREFIX + "1970-01";
    Set<String> types = new HashSet<>();
    Set<String> aliases = new HashSet<>();
    Set<String> indices = new HashSet<>();
    List<String> listIndices = new ArrayList<>();
    List<String> listAliases = new ArrayList<>();
    List<String> listTypes = new ArrayList<>();
    HashMap<String, Integer> bulkSettings = new HashMap<>();


    @Mock
    Client client;

    @Mock
    Connector connector;

    @Mock
    TagES tag;

    @InjectMocks
    DataUtils dataUtils = new DataUtils();

    @Before
    public void setup() {
        indices.add(INDEX_PREFIX + "index1");
        listIndices.add(INDEX_PREFIX + "index1");
        aliases.add("alias1");
        listAliases.add("alias1");
        types.add("type1");
        listTypes.add("type1");

        bulkSettings.put("bulkActions", BulkSettings.BULK_ACTIONS.getSetting());
        bulkSettings.put("bulkSize", BulkSettings.BULK_SIZE.getSetting());
        bulkSettings.put("flushInterval", BulkSettings.FLUSH_INTERVAL.getSetting());
        bulkSettings.put("concurrent", BulkSettings.CONCURRENT.getSetting());

//        when(connector.getIndices()).thenReturn(listIndices);
//        when(connector.getAliases()).thenReturn(listAliases);
//        when(connector.).thenReturn(listTypes);
    }

    @Test
    public void testGetMonthIndexSettings() {
        Settings.Builder expected = Settings.settingsBuilder().put("number_of_shards", 10)
                .put("number_of_replicas", 0);
        assertEquals(expected.get("number_of_shards"), dataUtils.getMonthIndexSettings().get("number_of_shards"));
        assertEquals(expected.get("number_of_replicas"), dataUtils.getMonthIndexSettings().get("number_of_replicas"));
    }

    @Test
    public void testGetIndex() {
        String expected = "1970-01";
        when(tag.getTagServerTime()).thenReturn(123456L);
        String index = dataUtils.getIndex(tag);
        assertEquals(expected, index);
    }

    @Test
    public void testGetType() {
        when(tag.getDataType()).thenReturn("boolean");
        String expected = TAG_PREFIX + "boolean";
        assertEquals(expected, dataUtils.getType(tag));
    }

    @Test
    public void testGetLastIndex() {
        String result = dataUtils.getLastIndex();
        assertEquals(FIRST_INDEX, result);

        String added = INDEX_PREFIX +  "2010-01";
        indices.add(added);
        dataUtils.addIndex(added);
        result = dataUtils.getLastIndex();
        assertEquals(added, result);

        String older = INDEX_PREFIX +  "2009-10";
        indices.add(added);
        dataUtils.addIndex(added);
        result = dataUtils.getLastIndex();
        assertEquals(added, result);
    }

    @Test
    public void testGoodAddAlias() {
        Set<String> expected = new HashSet<>();
        expected.add("tag_123");
        dataUtils.addAlias("tag_123");
        assertEquals(expected, dataUtils.getAliases());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadAddAlias() {
        dataUtils.addAlias("tag_abc");
    }

    @Test
    public void testGoodAddIndex() {
        Set<String> expected = new HashSet<>();
        expected.add("c2mon_1234-01");
        dataUtils.addIndex("c2mon_1234-01");
        assertEquals(expected, dataUtils.getIndices());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadAddIndex() {
        dataUtils.addIndex("tim_test");
    }

    @Test
    public void testGoodAddType() {
        Set<String> expected = new HashSet<>();
        expected.add("tag_123");
        dataUtils.addAlias("tag_123");
        assertEquals(expected, dataUtils.getAliases());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadAddType() {
        dataUtils.addIndex("tag-boolean");
    }
}
