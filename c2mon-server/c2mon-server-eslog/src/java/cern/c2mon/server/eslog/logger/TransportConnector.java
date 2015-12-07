package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.indexer.BulkLoader;
import cern.c2mon.server.eslog.structure.queries.Query;
import cern.c2mon.server.eslog.structure.queries.QueryAliases;
import cern.c2mon.server.eslog.structure.queries.QueryIndexBuilder;
import cern.c2mon.server.eslog.structure.queries.QueryIndices;
import cern.c2mon.server.eslog.structure.types.TagES;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows to connect to the cluster via a transport client.
 * We create a new client every time we want to do a request, this is very light for the cluster.
 * @author Alban Marguet.
 */
@Slf4j
@Service
public class TransportConnector implements Connector {
    private DataUtils dataUtils;
    private int port;
    private String host;
    private String cluster;
    private Settings settings;
    private BulkLoader bulkLoader;
    private Client client;

    @Autowired
    public TransportConnector(DataUtils dataUtils, BulkLoader bulkLoader) {
        this.dataUtils = dataUtils;
        this.host = dataUtils.getHost();
        this.cluster = dataUtils.getClusterName();
        this.port = dataUtils.getPort();
        this.settings = dataUtils.getTransportSettings();
        this.client = createTransportClient();
        this.bulkLoader = bulkLoader;
    }

    public Client getClient() {
        return client;
    }

    /**
     * Called by each request to handle it: need a transportClient to communicate with the ElasticSearch cluster.
     * @return
     */
    public Client createTransportClient() {
        try {
            return TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
        } catch(UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Handles a query for the ElasticSearch cluster.
     * @param query type of query.
     * @param indices indices to check for the data.
     * @param isTypeDefined true if we know the type in ElasticSearch.
     * @param types list of types to look for the data if we know it.
     * @param tagIds list of tagIds to retrieve.
     * @param from from where to retrieve the data oin the response. (pagination)
     * @param size to where to retrieve the data. (pagination)
     * @param min smallest range side.
     * @param max biggest range size.
     * @return List of retrieved responses according to the query.
     */
    @Override
    public List<String> handleQuery(Query query, String[] indices, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
        List<String> queryResponse = new ArrayList<>();

        if (client == null) {
            log.error("Error: could not create Transport client.");
            return null;
        }

        if (query instanceof QueryIndices) {
            query.setParameters(client, indices, isTypeDefined, types, tagIds, from, size, min, max);
            queryResponse.addAll(((QueryIndices) query).getListOfAnswer());
        } else {
            log.error("Error: this type of query is not covered.");
            return null;
        }

        return queryResponse;
    }

    @Override
    public boolean handleIndexQuery(Query query, String indexName, Settings.Builder settings, String type, String mapping) {
        boolean queryResponse = false;

        if (client == null) {
            log.error("Error: could not create Transport client.");
            return queryResponse;
        }

        if (query instanceof QueryIndexBuilder) {
            queryResponse = ((QueryIndexBuilder) query).indexNew(indexName, settings, type, mapping);
        } else {
            log.error("Error: this type of query is not covered.");
        }
        return queryResponse;
    }

    @Override
    public void handleAliasQuery(Query query, String indexMonth, String aliasName) {
        if (client == null) {
            log.error("Error: could not create Transport client.");
            return;
        }

        if (query instanceof QueryAliases) {
            ((QueryAliases) query).addAlias(indexMonth, aliasName);
        } else {
            log.error("Error: this type of query is not covered.");
        }
    }

    /**
     * Add 1 TagES to index to the ElasticSearch cluster thanks to the BulkProcessor.
     * @param tag to index.
     */
    public void indexTag(TagES tag) {
        try {
            String tagJson = tag.build();
            String indexMonth = dataUtils.getIndex(tag);
            String type = dataUtils.getType(tag);
            log.info("index: " + indexMonth);
            log.info("type: " + type);

            bulkLoader.add(indexMonth, type, tagJson, tag);
            bulkLoader.addAlias(indexMonth, tag);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Index several tags in the ElasticSearch cluster according to the BulkProcessor parameters.
     * @param tags to index.
     */
    @Override
    public void indexTags(List<TagES> tags) {
        int bulkActions = dataUtils.getBulkSettings().get("bulkActions");
        for (int i = 0; i < bulkActions && i < tags.size(); i++) {
            indexTag(tags.get(i));

            if (i > 0 && i % bulkActions == 0) {
                log.info("close bulk: " + i);
                bulkLoader.close();
            }
        }
    }

    /**
     * Retrieve all the indices in the ElasticSearch cluster.
     * @return List of the names of the indices.
     */
    @Override
    public List<String> getIndices() {
        return handleQuery(new QueryIndices(), null, false, null, null, -1, -1, -1, -1);
    }

    /**
     * Method called to close the newly opened client.
     * @param client transportClient for the cluster.
     */
    @Override
    public void close(Client client) {
        if (client != null) {
            client.close();
        }
    }

    @Override
    public DataUtils getUtils() {
        return dataUtils;
    }
}
