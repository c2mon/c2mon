//package cern.c2mon.server.eslog.logger;
//
//import cern.c2mon.server.eslog.indexer.BulkLoader;
//import cern.c2mon.server.eslog.structure.Query;
//import cern.c2mon.server.eslog.structure.types.TagES;
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.xcontent.XContentBuilder;
//import org.elasticsearch.search.SearchHits;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNull;
//import static org.mockito.Mockito.*;
//import static org.mockito.MockitoAnnotations.initMocks;
//
///**
// * @author Alban Marguet.
// */
//@Slf4j
//@RunWith(MockitoJUnitRunner.class)
//public class NodeConnectorTest {
//    HashMap<String, Integer> expectedSettings;
//    String builder;
//    int collectionSize;
//    Settings settings;
//    List<TagES> tags;
//
//    NodeConnector node;
//
//    @Mock
//    TagES tag;
//
//    @Mock
//    DataUtils dataUtils;
//
//    @Mock
//    BulkLoader bulkLoader;
//
//    @Mock
//    Query query;
//
//    @Mock
//    SearchResponse response;
//
//    @Mock
//    SearchHits hits;
//
//    @Before
//    public void setup() throws IOException {
//        initMocks(this);
//        node = new NodeConnector(dataUtils, bulkLoader);
//
//        expectedSettings = new HashMap<>();
//        expectedSettings.put("bulkActions", 5600);
//        expectedSettings.put("bulkSize", 5);
//        expectedSettings.put("flashInterval", 1);
//        expectedSettings.put("concurrent", 1);
//
//        builder = jsonBuilder().startObject()
//                .field("tagId", 1)
//                .field("tagName", "name")
//                .field("tagTime", 123456L)
//                .field("dataType", "boolean")
//                .field("tagServerTime", 123456L)
//                .field("tagDaqTime", 123456L)
//                .field("tagStatus", 0)
//                .field("quality", "quality ok")
//                .field("tagValueDesc", "value desc")
//                .field("tagValue", true)
//                .endObject().string();
//        collectionSize = 5;
//
//        tags = new ArrayList<>();
//        for (int i = 0; i < collectionSize; i++) {
//            tags.add(tag);
//        }
//    }
//
//    /**
//     * Normal behaviour: should create a batch of "collectionSize" tags and send them as bulk with the right parameters.
//     * @throws IOException
//     */
//    @Test
//    public void testIndexTags() throws IOException {
//        assertEquals("elasticsearch", node.getCluster());
//
//        when(dataUtils.getBulkSettings()).thenReturn(expectedSettings);
//        when(dataUtils.getIndex(tag)).thenReturn("tim_123456L");
//        when(dataUtils.getType(tag)).thenReturn("tag_boolean");
//        when(tag.build()).thenReturn(builder);
//        when(tag.getTagValue()).thenReturn(true);
//
//        node.indexTags(tags);
//
//        verify(dataUtils).getBulkSettings();
//
//
//        assertEquals(expectedSettings.get("bulkActions"), dataUtils.getBulkSettings().get("bulkActions"));
//        assertEquals(expectedSettings.get("bulkSize"), dataUtils.getBulkSettings().get("bulkSize"));
//        assertEquals(expectedSettings.get("flushInterval"), dataUtils.getBulkSettings().get("flushInterval"));
//        assertEquals(expectedSettings.get("concurrent"), dataUtils.getBulkSettings().get("concurrent"));
//
//        verify(bulkLoader, times(collectionSize)).add("tim_123456L", "tag_boolean", tag.build(), tag);
//        verify(bulkLoader, times(collectionSize)).addAlias("tim_123456L", tag);
//    }
//
//    /**
//     * Should close/flush the bulkLoader because we try to send more than the size of the bulk (5600).
//     * @throws IOException
//     */
//    @Test
//    public void testCloseBulk() throws IOException {
//        tags = new ArrayList<>();
//        for (int i = 0; i < 5601; i++) {
//            tags.add(tag);
//        }
//        assertEquals(5601, tags.size());
//
//        when(dataUtils.getBulkSettings()).thenReturn(expectedSettings);
//        when(dataUtils.getIndex(tag)).thenReturn("tim_123456L");
//        when(dataUtils.getType(tag)).thenReturn("tag_boolean");
//        when(tag.build()).thenReturn(builder);
//        when(tag.getTagValue()).thenReturn(true);
//
//        node.indexTags(tags);
//
//        verify(dataUtils).getBulkSettings();
//        verify(bulkLoader, times(5600)).add("tim_123456L", "tag_boolean", tag.build(), tag);
//        verify(bulkLoader, times(5600)).addAlias("tim_123456L", tag);
//
//        verify(bulkLoader, after(10000)).close();
//
////        verify(bulkLoader, times(1)).add("tim_123456L", "tag_boolean", tag.build().string(), tag);
////        verify(bulkLoader, times(1)).addAlias("tim_123456L", tag);
//    }
//
//    /**
//     * normal behaviour if 1 index is retrieved.
//     * Should return the name of this index.
//     */
//    @Test
//    public void testGetIndexWithOne() {
//        query = query.INDICES;
//        when(query.getIndices(node.getClient())).thenReturn(new String[] {"tim_123456"});
//        when(query.getResponse(node.getClient(), new String[]{"tim_123456"}, anyBoolean(), null, new long[]{1L}
//                , anyInt(), anyInt(), anyInt(), anyInt()))
//                .thenReturn(response);
//        when(response.getHits()).thenReturn(hits);
//        when(hits.getTotalHits()).thenReturn(1L);
//        String index = node.getIndex(1L);
//        assertEquals("tim_123456", index);
//    }
//
//    /**
//     * Should return no index name if nothing is retrieved.
//     */
//    @Test
//    public void testGetIndexWithNone() {
//        query = query.INDICES;
//        when(query.getIndices(node.getClient())).thenReturn(new String[] {"tim_123456"});
//        when(query.getResponse(node.getClient(), null, anyBoolean(), null, new long[]{1L}
//                , anyInt(), anyInt(), anyInt(), anyInt()))
//                .thenReturn(response);
//        when(response.getHits()).thenReturn(hits);
//        when(hits.getTotalHits()).thenReturn(0L);
//        assertNull(node.getIndex(1L));
//    }
//}
