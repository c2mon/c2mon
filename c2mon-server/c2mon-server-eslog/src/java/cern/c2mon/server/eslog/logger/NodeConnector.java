//package cern.c2mon.server.eslog.logger;
//
//import cern.c2mon.server.eslog.indexer.BulkLoader;
//import cern.c2mon.server.eslog.structure.Query;
//import cern.c2mon.server.eslog.structure.types.TagES;
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.Client;
//import org.elasticsearch.common.Base64;
//import org.elasticsearch.common.io.stream.InputStreamStreamInput;
//import org.elasticsearch.common.io.stream.StreamInput;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.node.Node;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Path;
//import java.util.List;
//import java.util.Set;
//
//import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
//
///**
// * TODO: FIX IT
// * NOT ABLE TO USE IT AT THE MOMENT
// * Represents the connection to a node that is active within the ElasticSearch cluster.
// * This node is only a transmitter, it does not hold any data.
// * It forwards the queries (indexing or retrieval).
// * @author Alban Marguet.
// */
//@Slf4j
//public class NodeConnector implements Connector {
//    private Client client;
//    private Node node;
//    private final String cluster = "elasticsearch";
//    private DataUtils dataUtils;
//    private BulkLoader bulkLoader;
//    private final String home = "/home/albanmarguet/IdeaProjects/c2mon-server-eslog/src/java/cern/c2mon/server/eslog/config/elasticsearch";
//
//    //@Autowired
//    public NodeConnector(DataUtils dataUtils, BulkLoader bulkLoader) throws IOException {
//        String classPath = System.getProperty("java.class.path");
//        System.setProperty("java.class.path", "src/java/cern/c2mon/server/eslog/logger/NodeConnector.java");
//        Settings settings = Settings.settingsBuilder().put("path.home", home).build();
//
//        node = nodeBuilder().settings(settings).client(true).node();
//        //node.start();
//
//        client = node.client();
//
//        this.dataUtils = dataUtils;
//        this.bulkLoader = bulkLoader;
//        System.setProperty("java.class.path", classPath);
//    }
//
//    public String getCluster() {
//        return this.cluster;
//    }
//
//    @Override
//    public void close() {
//        client.close();
//        node.close();
//    }
//
//    @Override
//    public Client getClient() {
//        return client;
//    }
//
//    @Override
//    public void indexTags(List<TagES> tags) {
//        int bulkActions = dataUtils.getBulkSettings().get("bulkActions");
//        for (int i = 0; i < bulkActions && i < tags.size(); i++) {
//            indexTag(tags.get(i));
//
//            if (i > 0 && i % bulkActions == 0) {
//                log.info("close bulk: " + i);
//                bulkLoader.close();
//            }
//        }
//    }
//
//    public void indexTag(TagES tag) {
//        try {
//            String tagJson = tag.build();
//            String indexMonth = dataUtils.getIndex(tag);
//            String type = dataUtils.getType(tag);
//            log.info("index: " + indexMonth);
//            log.info("type: " + type);
//
//            bulkLoader.add(indexMonth, type, tagJson, tag);
//            bulkLoader.addAlias(indexMonth, tag);
//        } catch(IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public String[] getIndices() {
//        return Query.INDICES.getIndices(getClient());
//    }
//
//    @Override
//    public String getIndex(long tagId) {
//        SearchResponse response;
//        for (String index : getIndices()) {
//            response = Query.INDICES.getResponse(getClient(), new String[]{index}, false, null, new long[]{tagId}, -1, -1, -1, -1);
//            if (response.getHits().totalHits() > 0) {
//                return index;
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public SearchResponse queryES(Query query, String[] indexes, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
//        return query.getResponse(client, indexes, isTypeDefined, types, tagIds, from, size, min, max);
//    }
//
//    @Override
//    public DataUtils getUtils() {
//        return dataUtils;
//    }
//}
