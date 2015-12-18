package cern.c2mon.server.eslog;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.eslog.logger.TransportConnector;
import cern.c2mon.server.eslog.structure.DataTagESLogConverter;
import cern.c2mon.server.eslog.structure.queries.QueryIndices;
import cern.c2mon.server.eslog.structure.types.TagES;
import cern.c2mon.server.test.CacheObjectCreation;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Integration test with the core modules.
 * 
 * @author Alban Marguet.
 */
@Slf4j
@ContextConfiguration({"classpath:cern/c2mon/server/eslog/config/server-eslog-integration.xml" })
@RunWith(SpringJUnit4ClassRunner.class)

public class ESLogModuleIntegrationTest {
  private static String clusterName;
  private static String home;
  private static String host;
  private static String nodeName;
  private static Node clusterNode;
  private static Client clusterClient;
  @Autowired
  TransportConnector connector;
  @Autowired
  DataTagESLogConverter esLogConverter;
  @Mock
  private EquipmentCache equipmentCache;
  @Mock
  private ProcessCache processCache;



  @BeforeClass
  public static void initCluster() {
    log.info("@BeforeClass");
    clusterName = "testCluster";
    home = "../../resources/elasticsearch";
    host = "localhost";
    nodeName = "transportNode";

    clusterNode = nodeBuilder()
        .settings(Settings.settingsBuilder()
            .put("path.home", home)
            .put("cluster.name", clusterName)
            .put("node.local", true)
            .put("node.name", "ClusterNode")
            .put("node.data", true)
            .put("node.master", true)
            .put("http.enabled", false)
            .put("transport.host", "localhost")
            .put("transport.tcp.port", 8300)
            .build())
        .node();

    clusterNode.start();
    clusterClient = clusterNode.client();
    log.info("Node created with home " + home + " in cluster " + clusterName + ".");
    clusterClient.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
  }

  @Before
  public void setup() {
    when(processCache.get(anyLong())).thenReturn(CacheObjectCreation.createTestProcess1());
    when(equipmentCache.get(anyLong())).thenReturn(CacheObjectCreation.createTestEquipment());
  }

  @AfterClass
  public static void tidyUpCluster() {
    clusterClient.close();
    clusterNode.close();
  }

  @Test
  public void testModuleStartup() {
    String[] indices = clusterClient.admin().indices().prepareGetIndex().get().indices();
    log.info("indices in the cluster:");
    for (String index : indices) {
      log.info(index);
    }
  }

  @Test
  public void testAddAndRead() {
    DataTagCacheObject tagC2MON = CacheObjectCreation.createTestDataTag3();
    TagES tag = esLogConverter.convertToTagES(tagC2MON);
    String json = tag.build();
    connector.indexTags(Arrays.asList(tag));
    QueryIndices query = new QueryIndices(connector.getClient());

    List<String> indices = query.getListOfAnswer();
    for (String index : indices) {
      log.info(index);
    }
  }
}