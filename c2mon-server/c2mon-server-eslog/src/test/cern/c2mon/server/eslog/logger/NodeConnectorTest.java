//package cern.c2mon.server.eslog.logger;
//
//import cern.c2mon.server.eslog.structure.queries.Query;
//import cern.c2mon.server.eslog.structure.types.TagES;
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.common.settings.Settings;
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
//import static org.mockito.MockitoAnnotations.initMocks;
//
///**
// * @author Alban Marguet.
// */
//@Slf4j
//@RunWith(MockitoJUnitRunner.class)
//public class NodeConnectorTest {
//	HashMap<String, Integer> expectedSettings;
//	String builder;
//	int collectionSize;
//	Settings settings;
//	List<TagES> tags;
//
//	NodeConnector node;
//
//	@Mock
//	TagES tag;
//
//	@Mock
//	Query query;
//
//	@Mock
//	SearchResponse response;
//
//	@Mock
//	SearchHits hits;
//
//	@Before
//	public void setup() throws IOException {
//		initMocks(this);
//		node = new NodeConnector();
//
//		expectedSettings = new HashMap<>();
//		expectedSettings.put("bulkActions", 5600);
//		expectedSettings.put("bulkSize", 5);
//		expectedSettings.put("flashInterval", 1);
//		expectedSettings.put("concurrent", 1);
//
//		builder = jsonBuilder().startObject()
//				.field("tagId", 1)
//				.field("tagName", "name")
//				.field("tagTime", 123456L)
//				.field("dataType", "boolean")
//				.field("tagServerTime", 123456L)
//				.field("tagDaqTime", 123456L)
//				.field("tagStatus", 0)
//				.field("quality", "quality ok")
//				.field("tagValueDesc", "value desc")
//				.field("tagValue", true)
//				.endObject().string();
//
//		collectionSize = 5;
//
//
//		tags = new ArrayList<>();
//		for (int i = 0; i < collectionSize; i++) {
//			tags.add(tag);
//		}
//	}
//
//	/**
//	 * Normal behaviour: should create a batch of "collectionSize" tags and send them as bulk with the right parameters.
//	 * @throws IOException
//	 */
//	@Test
//	public void testIndexTags() throws IOException {
//
//	}
//
//	/**
//	 * Should close/flush the bulkLoader because we try to send more than the size of the bulk (5600).
//	 * @throws IOException
//	 */
//	@Test
//	public void testCloseBulk() throws IOException {
//
//	}
//
//	/**
//	 * normal behaviour if 1 index is retrieved.
//	 * Should return the name of this index.
//	 */
//	@Test
//	public void testGetIndexWithOne() {
//
//	}
//
//	/**
//	 * Should return no index name if nothing is retrieved.
//	 */
//	@Test
//	public void testGetIndexWithNone() {
//
//	}
//}