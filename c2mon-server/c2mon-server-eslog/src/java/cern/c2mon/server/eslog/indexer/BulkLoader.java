package cern.c2mon.server.eslog.indexer;

import cern.c2mon.server.eslog.logger.Connector;
import cern.c2mon.server.eslog.logger.DataUtils;
import cern.c2mon.server.eslog.structure.queries.QueryAliases;
import cern.c2mon.server.eslog.structure.queries.QueryIndexBuilder;
import cern.c2mon.server.eslog.structure.types.TagES;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Handles the bulk writing to the ElasticSearch cluster.
 * Uses the BulkSettings enum.
 * @author Alban Marguet.
 */
@Slf4j
@Service
public class BulkLoader {
    private BulkProcessor bulkProcessor;
    private DataUtils dataUtils;
    private Connector connector;

    @Autowired
    public BulkLoader(DataUtils dataUtils, Connector connector) {
        this.dataUtils = dataUtils;
        this.connector = connector;
    }

    @PostConstruct
    public void init() {
        this.bulkProcessor = BulkProcessor.builder(
                connector.getClient(),
                new BulkProcessor.Listener() {
                    public void beforeBulk(long executionId, BulkRequest request) {
                        log.info("Execute bulk " + executionId);
                    }

                    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                        if (response.hasFailures()) {
                            log.info(response.buildFailureMessage());
                        } else {
                            log.info("success");
                        }
                    }

                    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

                    }
                })
                .setName("bulkProcessor")
                .setBulkActions(dataUtils.getBulkSettings().get("bulkActions"))
                .setBulkSize(new ByteSizeValue(dataUtils.getBulkSettings().get("bulkSize"), ByteSizeUnit.GB))
                .setFlushInterval(TimeValue.timeValueSeconds(dataUtils.getBulkSettings().get("flushInterval")))
                .setConcurrentRequests(dataUtils.getBulkSettings().get("concurrent"))
                .build();
    }

    public void add(String index, String type, String json, TagES tag) throws IOException {

        for (String i: dataUtils.getIndices()) {
            log.info(i);
        }
        if (!dataUtils.getIndices().contains(index)) {
            boolean isIndexed = instantiateIndex(tag, index, type);
            if (isIndexed) {
                log.info("Indexed " + type);
                dataUtils.getIndices().add(type);
            }
        }
        bulkProcessor.add(new IndexRequest(index, type).source(json).routing(String.valueOf(tag.getTagId())));
    }

    public void close() {
        try {
            bulkProcessor.awaitClose(10, TimeUnit.SECONDS);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean instantiateIndex(TagES tag, String index, String type) {
        String mapping = tag.getMapping();
        return dataUtils.getConnector().handleIndexQuery(new QueryIndexBuilder(), index, dataUtils.getMonthIndexSettings(), type, mapping);
    }

    public boolean addAlias(String indexMonth, TagES tag) {
        if (!dataUtils.getIndices().contains(indexMonth)) {
            throw new IllegalArgumentException("Error: index must exist to create an alias");
        }
        long id = tag.getTagId();
        String aliasName = dataUtils.generateAliasName(id);

        if (!dataUtils.getAliases().contains(aliasName)) {
            log.info("Add alias: " + aliasName + " for index " + indexMonth);

            dataUtils.getConnector().handleAliasQuery(new QueryAliases(), indexMonth, aliasName);

            dataUtils.getAliases().add(aliasName);
            return false;
        }
        return true;
    }
}