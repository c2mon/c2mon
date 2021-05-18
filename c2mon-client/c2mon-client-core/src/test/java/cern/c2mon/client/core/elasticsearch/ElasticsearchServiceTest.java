package cern.c2mon.client.core.elasticsearch;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.elasticsearch.IndexManager;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClientRest;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentConverter;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentIndexer;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentListener;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import org.easymock.Mock;
import org.junit.*;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collection;
import java.util.Collections;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ElasticsearchServiceTest {

    private static final DockerImageName ELASTICSEARCH_IMAGE = DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch");

    private static final String ELASTICSEARCH_TAG = "7.12.0";

    @ClassRule
    public static ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchContainer(ELASTICSEARCH_IMAGE.withTag(ELASTICSEARCH_TAG)).withExposedPorts(9200, 9300);

    private static ElasticsearchProperties elasticsearchProperties = new ElasticsearchProperties();

    private static TagConfigDocumentListener tagDocumentListener;
    private static C2monClientProperties properties = new C2monClientProperties();
    private static ElasticsearchClientRest client;
    private static IndexManager indexManager;

    @Mock
    private static TagFacadeGateway tagFacadeGateway;

    @BeforeClass
    public static void setUpClass() {
        elasticsearchProperties.setHost(elasticsearchContainer.getHost());
        elasticsearchProperties.setPort(elasticsearchContainer.getMappedPort(9200));

        C2monClientProperties.Elasticsearch testElasticsearch = new C2monClientProperties.Elasticsearch();
        testElasticsearch.setUrl("http://" + elasticsearchContainer.getHost() + ":" + elasticsearchContainer.getMappedPort(9200));

        properties.setElasticsearch(testElasticsearch);

        client = new ElasticsearchClientRest(elasticsearchProperties);
        indexManager = new IndexManager(client);
        ProcessCache processCache = createNiceMock(ProcessCache.class);
        EquipmentCache equipmentCache = createNiceMock(EquipmentCache.class);
        SubEquipmentCache subequipmentCache = createNiceMock(SubEquipmentCache.class);
        TagConfigDocumentConverter converter = new TagConfigDocumentConverter(processCache, equipmentCache, subequipmentCache);
        tagFacadeGateway = createNiceMock(TagFacadeGateway.class);
        TagConfigDocumentIndexer indexer = new TagConfigDocumentIndexer(elasticsearchProperties, indexManager, tagFacadeGateway, converter);
        tagDocumentListener = new TagConfigDocumentListener(elasticsearchProperties, indexer, converter, tagFacadeGateway);
    }

    @AfterClass
    public static void tearDownClass() {
        elasticsearchContainer.stop();
    }

    @Before
    public void resetMocks() {
        reset(tagFacadeGateway);
    }

    @Test
    public void testSearchByMetadata() throws InterruptedException {
        try {
            Long testUserTagId = Double.doubleToLongBits(Math.random()) % 10000;
            String testUser = Long.toHexString(Double.doubleToLongBits(Math.random()));
            String responsible = "responsible";
            DataTagCacheObject tag = new DataTagCacheObject(testUserTagId);
            tag.getMetadata().getMetadata().put(responsible, testUser);
            expect(tagFacadeGateway.getAlarms(anyObject(DataTagCacheObject.class))).andReturn(Collections.emptyList()).times(2);
            replay(tagFacadeGateway);

            tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);
            Long tag1234Id = Double.doubleToLongBits(Math.random()) % 10000;
            String value1234 = "1234";
            tag = new DataTagCacheObject(tag1234Id);
            String key1234 = "1234";
            tag.getMetadata().getMetadata().put(key1234, value1234);
            tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

            client.refreshIndices();
            Thread.sleep(10000);

            ElasticsearchService service = new ElasticsearchService(properties, "c2mon");

            assertEquals("There should be 2 tags, one for responsible and one for 1234", 2, service.getDistinctTagMetadataKeys().size());

            Collection<Long> tagsForResponsibleUser = service.findTagsByMetadata(responsible, testUser);
            assertEquals("There should be one tag with responsible user set to requested value", 1, tagsForResponsibleUser.size());
            assertEquals(testUserTagId, tagsForResponsibleUser.stream().findFirst().get());

            Collection<Long> tags1234 = service.findTagsByMetadata(key1234, value1234);
            assertEquals("There should be one tag with 1234 parameter set to requested value", 1, tags1234.size());
            assertEquals(tag1234Id, tags1234.stream().findFirst().get());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testSearchByNameAndMetadata() throws InterruptedException {
        try {
            Long testUserTagId = Double.doubleToLongBits(Math.random()) % 10000;
            String testUser = Long.toHexString(Double.doubleToLongBits(Math.random()));
            String metadataKey = "metadataKey";
            DataTagCacheObject tag = new DataTagCacheObject(testUserTagId);
            String tagname = "tagname";
            tag.setName(tagname);
            tag.getMetadata().getMetadata().put(metadataKey, testUser);
            expect(tagFacadeGateway.getAlarms(anyObject(DataTagCacheObject.class))).andReturn(Collections.emptyList()).times(3);
            replay(tagFacadeGateway);
            tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

            tag = new DataTagCacheObject(Double.doubleToLongBits(Math.random()) % 10000);
            tag.setName(tagname);
            tag.getMetadata().getMetadata().put(metadataKey, "some other metadata value");
            tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

            tag = new DataTagCacheObject(Double.doubleToLongBits(Math.random()) % 10000);
            tag.setName("other_tagname");
            tag.getMetadata().getMetadata().put(metadataKey, testUser);
            tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

            client.refreshIndices();
            Thread.sleep(10000);

            ElasticsearchService service = new ElasticsearchService(properties, "c2mon");

            Collection<Long> tagsForResponsibleUser = service.findTagsByNameAndMetadata(tagname, metadataKey, testUser);
            assertEquals("There should be one tag with given name and metadata", 1, tagsForResponsibleUser.size());
            assertEquals(testUserTagId, tagsForResponsibleUser.stream().findFirst().get());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testSearchByName() throws InterruptedException {
        try {
            ElasticsearchService service = new ElasticsearchService(properties, "c2mon");
            Collection<Long> tagsForResponsibleUser = service.findTagsByName("TEST");
            assertNotNull("The tags collection should not be null", tagsForResponsibleUser);
            assertEquals("There tags collection should be empty", 0, tagsForResponsibleUser.size());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}