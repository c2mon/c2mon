package cern.c2mon.client.core.elasticsearch;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.elasticsearch.Indices;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentConverter;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentIndexer;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentListener;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.Collection;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertEquals;

public class ElasticsearchServiceTest {

    @Test
    public void testSearchByMetadata() throws InterruptedException {
        C2monClientProperties properties = new C2monClientProperties();
        ElasticsearchProperties elasticsearchProperties = new ElasticsearchProperties();
        ElasticsearchClient client = new ElasticsearchClient();
        Whitebox.setInternalState(client,"properties", elasticsearchProperties);
        client.init();
        try {
            client.getClient().admin().indices().prepareDelete(properties.getElasticsearch().getTagConfigIndex()).execute().actionGet();
            client.getClient().admin().indices().flush(new FlushRequest()).actionGet();
        }catch(Exception e){
            // maybe index was not there yet
        }
        TagConfigDocumentIndexer indexer = new TagConfigDocumentIndexer(client, elasticsearchProperties);
        ProcessCache processCache = createNiceMock(ProcessCache.class);
        EquipmentCache equipmentCache = createNiceMock(EquipmentCache.class);
        SubEquipmentCache subequipmentCache = createNiceMock(SubEquipmentCache.class);
        Indices indices = new Indices(elasticsearchProperties, client);
        TagConfigDocumentConverter converter = new TagConfigDocumentConverter(processCache, equipmentCache, subequipmentCache);
        TagConfigDocumentListener tagDocumentListener = new TagConfigDocumentListener(indexer, converter);

        Long testUserTagId = Double.doubleToLongBits(Math.random())%10000;
        String testUser = Long.toHexString(Double.doubleToLongBits(Math.random()));
        String responsible = "responsible";
        DataTagCacheObject tag = new DataTagCacheObject(testUserTagId);
        tag.getMetadata().getMetadata().put(responsible, testUser);
        tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

        Long tag1234Id = Double.doubleToLongBits(Math.random())%10000;
        String value1234 = "1234";
        tag = new DataTagCacheObject(tag1234Id);
        String key1234 = "1234";
        tag.getMetadata().getMetadata().put(key1234, value1234);
        tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);
        client.getClient().admin().indices().flush(new FlushRequest()).actionGet();

        ElasticsearchService service = new ElasticsearchService(properties);

        assertEquals("There should be 2 tags, one for responsible and one for 1234", 2, service.getDistinctMetadataKeys().size());

        Collection<Long> tagsForResponsibleUser = service.findByMetadata(responsible, testUser);
        assertEquals("There should be one tag with responsible user set to requested value",1,tagsForResponsibleUser.size());
        assertEquals(testUserTagId, tagsForResponsibleUser.stream().findFirst().get());

        Collection<Long> tags1234 = service.findByMetadata(key1234, value1234);
        assertEquals("There should be one tag with 1234 parameter set to requested value",1,tags1234.size());
        assertEquals(tag1234Id, tags1234.stream().findFirst().get());
        client.close(client.getClient());
        client.closeEmbeddedNode();
    }
}