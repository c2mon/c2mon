//package cern.c2mon.server.eslog.logger;
//
//import cern.c2mon.server.eslog.structure.queries.Query;
//import cern.c2mon.server.eslog.structure.types.TagES;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.client.Client;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.node.Node;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
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
//@Data
//public class NodeConnector implements Connector {
//	private Client client;
//	private Node node;
//	private final String cluster = "elasticsearch";
//	private final String home = "";
//	private HashMap<String, Integer> bulkSettings;
//
//	@Autowired
//	public NodeConnector() throws IOException {
//		Settings settings = Settings.settingsBuilder().put("path.home", home).build();
//		this.bulkSettings = new HashMap<>();
//		node = nodeBuilder().settings(settings).client(true).node();
//	}
//
//	public String getCluster() {
//		return this.cluster;
//	}
//
//	@Override
//	public void close(Client client) {
//		client.close();
//		node.close();
//	}
//
//	public Client getClient() {
//		return client;
//	}
//
//	@Override
//	public Client createClient() {
//		return null;
//	}
//
//	@Override
//	public void indexTags(List<TagES> tags) {
//
//	}
//
//	@Override
//	public void updateLists() {
//
//	}
//
//	@Override
//	public List<String> handleListingQuery(Query query) {
//		return null;
//	}
//
//	@Override
//	public boolean handleIndexQuery(Query query, String indexName, Settings settings, String type, String mapping) {
//		return false;
//	}
//
//	@Override
//	public boolean handleAliasQuery(Query query, String indexMonth, String aliasName) {
//		return false;
//	}
//
//	@Override
//	public boolean bulkAdd(String index, String type, String json, TagES tag) {
//		return false;
//	}
//
//	@Override
//	public boolean bulkAddAlias(String indexMonth, TagES tag) {
//		return false;
//	}
//}