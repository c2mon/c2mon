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
package cern.c2mon.server.eslog.indexer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.eslog.connector.Connector;
import cern.c2mon.server.eslog.structure.mappings.EsMapping;
import cern.c2mon.server.eslog.structure.mappings.EsTagMapping;
import cern.c2mon.server.eslog.structure.types.tag.EsTag;

/**
 * Used to write (a.k.a. index) the data to elasticSearch.
 * Makes use of the {@link Connector} connection to an ElasticSearch cluster.
 *
 * @author Alban Marguet.
 */

@Slf4j
@Qualifier("esTagIndexer")
@Component
@Data
@EqualsAndHashCode(callSuper = false)
public class EsTagIndexer<T extends EsTag> extends EsIndexer<T> {
  /**
   * Contains in-memory the content of the Indices and types present in the cluster.
   */
  private final Map<String, Set<String>> cacheIndicesTypes = new ConcurrentHashMap<>();

  /**
   * @param connector handling the connection to ElasticSearch.
   */
  @Autowired
  public EsTagIndexer(final Connector connector) {
    super(connector);
  }

  /**
   * make sure the connection is alive.
   */
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

    try {
        sendTagToBatch(esTag);
    } catch(Exception e) {
      throw new IDBPersistenceException(e);
    }
  }

  @Override
  public void storeData(List<T> data) throws IDBPersistenceException {
    if (data == null) {
      return;
    }
    try {
      log.debug("storeData() - Try to send data by batch of size " + data.size());

//      Collection<EsTag> tags = new ArrayList<>();
//
//      for (T esTag : data) {
//          log.debug("storeData() - type: " + esTag.getClass());
//          tags.add(esTag);
//      }

//      log.debug("storeData() - indexTags() a new Collection of " + tags.size() + " tags.");
      this.indexTags(data);
    } catch(ElasticsearchException e) {
      throw new IDBPersistenceException(e);
    }
  }

  /**
   * Index several {@link EsTag} in the ElasticSearch cluster according to the BulkProcessor parameters.
   *
   * @param tags to index.
   */
  public synchronized void indexTags(Collection<T> tags) {
    if(tags == null) {
      log.warn("indexTags() - Received a null collection of tags will do nothing!");
      return;
    }

    log.debug("indexTags() - Received a collection of " + tags.size() + " tags to send by batch.");

    if (CollectionUtils.isEmpty(tags)) {
      log.debug("indexTags() - Received a null List of tags to log to ElasticSearch.");
      return;
    }

    updateCache();
    int counter = 0;
    for (EsTag tag : tags) {
      if (sendTagToBatch(tag)) {
        counter++;
      }
    }

    log.debug("indexTags() - Created a batch composed of " + counter + " tags.");

    // FLUSH
    log.debug("indexTags() - closing bulk.");
    connector.getBulkProcessor().flush();
    connector.refreshClusterStats();

    updateCache();

  }

  /**
   * Initialize the JSON, indexName and type for the {@link EsTag} and send it to the BulkProcessor.
   * Scope is protected for testing
   * @param tag to index.
   * @return true, if tag indexing was successful
   */
  protected boolean sendTagToBatch(EsTag tag) {
    if (tag == null) {
      log.warn("indexByBatch() - Error while indexing data. Tag has null rawValue");
      return false;
    }

    String index = generateTagIndex(tag.getC2mon().getServerTimestamp());
    String type = generateTagType(tag.getC2mon().getDataType());

    if (log.isTraceEnabled()) {
      log.trace("Indexing a new tag (#{})", tag.getId());
      log.trace("Index = " + index);
      log.trace("Type = " + type);
    }


    if (index == null || type == null || !checkIndex(index)) {
      log.warn("indexByBatch() - Error while indexing tag #{}. Bad index {}  -> Tag will not be sent to elasticsearch!", tag.getId(), index);
      return false;
    }

    boolean indexIsPresent = createNonExistentIndex(index);
    boolean typeIsPresent = createNonExistentMapping(index, type, tag);

    String tagJson = tag.toString();
    log.debug("indexByBatch() - New IndexRequest for index" + index + " and source " + tagJson);
    if (indexIsPresent && typeIsPresent) {
      IndexRequest indexNewTag = new IndexRequest(index, type).source(tagJson).routing(tag.getId());
      return connector.bulkAdd(indexNewTag);
    }

    return false;
  }

  private String generateTagIndex(long serverTime) {
    return retrieveIndexFormat(indexPrefix + "-tag_", serverTime);
  }

  private String generateTagType(String dataType) {
    return typePrefix + "_" + getSimpleTypeName(dataType);
  }

  public static String getSimpleTypeName(String dataType) {
    String type = dataType.toLowerCase();

    if (dataType.contains(".")) {
      type = type.substring(type.lastIndexOf('.') + 1);
    }

    return type;
  }

  /**
   * Check in-memory if index and types are already present.
   * If yes, add to the batch directly,
   * otherwise, instantiate a new index with a new mapping.
   *
   * @return if the cluster has acked the writing.
   */
  private boolean indexByBatch(EsTag tag) {
    if (tag == null) {
      log.warn("indexByBatch() - Error while indexing data. Tag has null rawValue");
      return false;
    }

    String index = generateTagIndex(tag.getC2mon().getServerTimestamp());
    String type = generateTagType(tag.getC2mon().getDataType());

    if (log.isTraceEnabled()) {
      log.trace("Indexing a new tag (#{})", tag.getId());
      log.trace("Index = " + index);
      log.trace("Type = " + type);
    }


    if (index == null || type == null || !checkIndex(index)) {
      log.warn("indexByBatch() - Error while indexing tag #{}. Bad index {}  -> Tag will not be sent to elasticsearch!", tag.getId(), index);
      return false;
    }

    boolean indexIsPresent = createNonExistentIndex(index);
    boolean typeIsPresent = createNonExistentMapping(index, type, tag);

    String tagJson = tag.toString();
    log.debug("indexByBatch() - New IndexRequest for index" + index + " and source " + tagJson);
    if (indexIsPresent && typeIsPresent) {
      IndexRequest indexNewTag = new IndexRequest(index, type).source(tagJson).routing(String.valueOf(tag.getId()));
      return connector.bulkAdd(indexNewTag);
    }

    return false;
  }

  private boolean checkIndex(String index) {
    return index.matches("^" + indexPrefix + "-(alarm|supervision|tag)_\\d\\d\\d\\d-\\d\\d-?\\d?\\d?$");
  }

  private boolean createNonExistentIndex(String index) {
    if (cacheIndicesTypes.containsKey(index)) {
      return true;
    }

    boolean isIndexed = instantiateIndex(index);
    if (isIndexed) {
      addIndex(index);
    }
    return isIndexed;
  }

  /**
   * Try to add a new index with name {@param index} to ElasticSearch.
   *
   * @return true if acked by the cluster.
   */
  private boolean instantiateIndex(String index) {
    if (checkIndex(index)) {
      return connector.handleIndexQuery(index, null, null);
    }
    log.debug("instantiateIndex() - Bad index: " + index + ".");
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

  /**
   * Check in memory Map to know if the type has been assigned to this index.
   */
  private boolean mappingExists(String index, String type) {
    boolean indexPresent = cacheIndicesTypes.containsKey(index);
    boolean typePresent = typeIsPresent(index, type);
    return (indexPresent && typePresent);
  }

  /**
   * Check the in memory Map to know if a type is assigned to an index.
   */
  private boolean typeIsPresent(String index, String type) {
    Set<String> types = cacheIndicesTypes.get(index);
    return types != null && types.contains(type);
  }

  /**
   * Try to add a new type to ElasticSearch to the specified index with the specified mapping.
   */
  private boolean instantiateType(String index, String type, EsTag tag) {
    if ((cacheIndicesTypes.containsKey(index) && cacheIndicesTypes.get(index).contains(type)) || !checkIndex(index)) {
      log.warn("instantiateType() - Bad type adding to index " + index + ", type: " + type);
    }

    String mapping = null;
    if (!typeIsPresent(index, type)) {
      mapping = chooseMapping(tag);
      log.debug("instantiateIndex() - Adding a new mapping to index " + index + " for type " + type + ": " + mapping);
    }
    return connector.handleIndexQuery(index, type, mapping);
  }

  /**
   * Choose a {@link EsMapping} (to index the data in the right way in ElasticSearch) according to the {@param dataType}.
   */
  private String chooseMapping(EsTag tag) {
    log.trace("chooseMapping() - Choose mapping for data type " + tag.getC2mon().getDataType());

    return new EsTagMapping(tag.getType(), tag.getC2mon().getDataType()).getMapping();
  }


  /**
   * Add an index in-memory cache after it has been created in ElasticSearch. Called by the writing of a new Index if it was successful.
   *
   * @param indexName name of the index created in ElasticSearch.
   */
  public synchronized void addIndex(String indexName) {
    if (checkIndex(indexName)) {
      cacheIndicesTypes.put(indexName, new HashSet<>());
      log.debug("addIndex() - Added index " + indexName + " in memory list.");
    } else {
      throw new IllegalArgumentException("addIndex() - Index " + indexName + " does not follow the format \"indexPrefix_dateFormat\".");
    }
  }

  /**
   * Add a type in-memory cache. Called by the writing of a new Index if it was successful.
   *
   * @param typeName type defined for the new document.
   */
  protected synchronized void addType(String index, String typeName) {
    if (cacheIndicesTypes.containsKey(index)) {
      cacheIndicesTypes.get(index).add(typeName);
      log.debug("addType() - Added type " + typeName + " in memory list.");
    } else {
      throw new IllegalArgumentException("Types must follow the format \"tag_dataType\".");
    }
  }

  /**
   * Query the ElasticSearch cluster to retrieve all the indices and types,
   * that are present already at startup. Store them in-memory caches.
   */
  public void updateCache() {
    clearCache();
    updateCacheIndices();
    updateCacheTypes();
  }

  private synchronized void updateCacheIndices() {
    for (String index : connector.retrieveIndicesFromES()) {
      cacheIndicesTypes.put(index, new HashSet<>());
    }
  }

  private synchronized void updateCacheTypes() {
    for (String index : cacheIndicesTypes.keySet()) {
      cacheIndicesTypes.get(index).addAll(connector.retrieveTypesFromES(index));
    }
  }


  private synchronized void clearCache() {
    cacheIndicesTypes.clear();
  }

  private void displayCache() {
    if (log.isTraceEnabled()) {
      log.trace("displayLists():");

      for (String index : cacheIndicesTypes.keySet()) {
        log.trace("Index:");
        log.trace(index);

        log.trace("Has types:");
        Set<String> types = cacheIndicesTypes.get(index);
        for (String type : types) {
          log.trace(type);
        }
      }
    }
  }
}
