/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.elasticsearch.indexer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.connector.Connector;
import cern.c2mon.server.elasticsearch.structure.mappings.EsTagMapping;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTag;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;

/**
 * @author Alban Marguet
 */
@Slf4j
@Component("esTagIndexer")
public class EsTagIndexer<T extends EsTag> extends EsIndexer<T> {

  /**
   * Contains in-memory the content of the Indices and types present in the cluster.
   */
  @Getter
  private final Map<String, Set<String>> cacheIndicesTypes = new ConcurrentHashMap<>();

  @Autowired
  public EsTagIndexer(final Connector connector, ElasticsearchProperties properties) {
    super(connector, properties);
  }

  @Override
  @PostConstruct
  public void init() throws IDBPersistenceException {
    super.init();
  }

  @Override
  public void storeData(T esTag) throws IDBPersistenceException {
    if (esTag == null) {
      return;
    }

    boolean successful = false;
    try {
      successful = sendTagToBatch(esTag);
    } catch(Exception e) {
      throw new IDBPersistenceException(e);
    }
    finally {
      clearCache();
    }

    if (!successful) {
      throw new IDBPersistenceException("Tag could not be stored in Elasticsearch");
    }
  }

  @Override
  public void storeData(List<T> data) throws IDBPersistenceException {
    if (data == null) {
      return;
    }

    try {
      log.debug("Trying to send a batch of size {}", data.size());

      this.indexTags(data);
    } catch(ElasticsearchException e) {
      throw new IDBPersistenceException(e);
    } finally {
      clearCache();
    }
  }

  /**
   * Index several {@link EsTag} in the Elasticsearch cluster.
   *
   * @param tags to index.
   * @throws IDBPersistenceException in case the list could not be saved to Elasticsearch
   */
  public synchronized void indexTags(Collection<T> tags) throws IDBPersistenceException {
    if(tags == null) {
      return;
    }

    log.debug("Received a collection of {} tags to send by batch", tags.size());

    if (CollectionUtils.isEmpty(tags)) {
      return;
    }

    int counter = 0;
    for (EsTag tag : tags) {
      if (sendTagToBatch(tag)) {
        counter++;
      }
    }

    if (counter == 0) {
      throw new IDBPersistenceException("Could not index batch of {} tags", tags.size());
    }

    log.debug("Created a batch of {} tags", counter);

    // FLUSH
    log.debug("Flushing bulk processor");
    connector.getBulkProcessor().flush();

    // TODO: Should be removed, but then test needs fixing
    connector.refreshClusterStats();
  }

  /**
   * Initialize the JSON, indexName and type for the {@link EsTag} and send it
   * to the BulkProcessor.
   *
   * @param tag to index.
   * @return true, if tag indexing was successful
   */
  protected boolean sendTagToBatch(EsTag tag) {
    if (tag == null) {
      log.warn("Error while indexing data: tag has null value");
      return false;
    }

    String index = generateTagIndex(tag.getC2mon().getServerTimestamp());
    String type = generateTagType(tag.getC2mon().getDataType());

    if (log.isTraceEnabled()) {
      log.trace("Indexing a new tag (#{}, type={}, index={})", tag.getId(), index, type);
    }

    if (index == null || type == null || !checkIndex(index)) {
      log.warn("Error while indexing tag #{}: bad index {}: tag will not be indexed!", tag.getId(), index);
      return false;
    }

    boolean indexIsPresent = createNonExistentIndex(index);
    boolean typeIsPresent = createNonExistentMapping(index, type, tag);

    String tagJson = tag.toString();
    if (indexIsPresent && typeIsPresent) {
      log.debug("New 'IndexRequest' for index {} and source {}", index, tagJson);
      IndexRequest indexNewTag = new IndexRequest(index, type).source(tagJson).routing(tag.getId());
      return connector.bulkAdd(indexNewTag);
    }

    return false;
  }

  protected String generateTagIndex(long serverTime) {
    return retrieveIndexFormat(properties.getIndexPrefix() + "-tag_", serverTime);
  }

  private String generateTagType(String dataType) {
    return "type_" + getSimpleTypeName(dataType);
  }

  public static String getSimpleTypeName(String dataType) {
    String type = dataType.toLowerCase();

    if (dataType.contains(".")) {
      type = type.substring(type.lastIndexOf('.') + 1);
    }

    return type;
  }

  private boolean checkIndex(String index) {
    return index.matches("^" + properties.getIndexPrefix() + "-(alarm|supervision|tag)_\\d\\d\\d\\d-\\d\\d-?\\d?\\d?$");
  }

  protected boolean createNonExistentIndex(String index) {
    if (cacheIndicesTypes.containsKey(index)) {
      return true;
    }

    boolean isIndexed = sendCreateIndex(index);
    if (isIndexed) {
      addIndex(index);
    }
    return isIndexed;
  }

  /**
   * Try to add a new index.
   *
   * @return true if the index was created successfully, false otherwise
   */
  private boolean sendCreateIndex(String index) {
    if (checkIndex(index)) {
      return connector.createIndex(index);
    }
    log.debug("Bad index: {}", index);
    return false;
  }

  private boolean createNonExistentMapping(String index, String type, EsTag tag) {
    if (mappingExists(index, type)) {
      return true;
    }

    boolean isInstantiated = instantiateType(index, type, tag);
    if (isInstantiated) {
      addType(index, type);
    }
    return isInstantiated;
  }

  private boolean mappingExists(String index, String type) {
    boolean indexPresent = cacheIndicesTypes.containsKey(index);
    boolean typePresent = typeIsPresent(index, type);
    return (indexPresent && typePresent);
  }

  private boolean typeIsPresent(String index, String type) {
    Set<String> types = cacheIndicesTypes.get(index);
    return types != null && types.contains(type);
  }

  private boolean instantiateType(String index, String type, EsTag tag) {
    if ((cacheIndicesTypes.containsKey(index) && cacheIndicesTypes.get(index).contains(type)) || !checkIndex(index)) {
      log.warn("Bad type adding to index {}, type: {}", index, type);
    }

    String mapping = null;
    if (!typeIsPresent(index, type)) {
      mapping = chooseMapping(tag);
      log.debug("Adding a new mapping to index {} for type {}: ", index, type, mapping);
    }
    return connector.createIndexTypeMapping(index, type, mapping);
  }

  private String chooseMapping(EsTag tag) {
    return new EsTagMapping(tag.getType(), tag.getC2mon().getDataType()).getMapping();
  }

  public synchronized void addIndex(String indexName) {
    if (checkIndex(indexName)) {
      cacheIndicesTypes.put(indexName, new HashSet<>());
      log.debug("Added index {}", indexName);
    } else {
      throw new IllegalArgumentException("Invalid index format: " + indexName);
    }
  }

  protected synchronized void addType(String index, String typeName) {
    if (cacheIndicesTypes.containsKey(index)) {
      cacheIndicesTypes.get(index).add(typeName);
      log.debug("Added type {}", typeName);
    } else {
      throw new IllegalArgumentException("Invalid type format: " + typeName);
    }
  }

  protected synchronized void clearCache() {
    cacheIndicesTypes.clear();
  }
}
