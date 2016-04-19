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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.eslog.connector.Connector;
import cern.c2mon.server.eslog.structure.mappings.EsBooleanTagMapping;
import cern.c2mon.server.eslog.structure.mappings.EsMapping;
import cern.c2mon.server.eslog.structure.mappings.EsNumericTagMapping;
import cern.c2mon.server.eslog.structure.mappings.EsStringTagMapping;
import cern.c2mon.server.eslog.structure.types.EsTagImpl;

/**
 * Used to write (a.k.a. index) the data to elasticSearch.
 * Makes use of the {@link Connector} connection to an ElasticSearch cluster.
 * @author Alban Marguet.
 */

@Component
@Qualifier("esTagIndexer")
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class EsTagIndexer extends EsIndexer {
  /** Contains in-memory the content of the Indices and types present in the cluster. */
  private final Map<String, Set<String>> cacheIndicesTypes = new ConcurrentHashMap<>();

  /**
   * @param connector handling the connection to ElasticSearch.
   */
  @Autowired
  public EsTagIndexer(final Connector connector) {
    super(connector);
  }

  /** make sure the connection is alive. */
  @PostConstruct
  public void init() throws IDBPersistenceException {
    super.init();
  }

  @Override
  public void storeData(IFallback fallbackObject) throws IDBPersistenceException {
    try {
      if (fallbackObject != null && fallbackObject instanceof EsTagImpl) {
        sendTagToBatch((EsTagImpl) fallbackObject);
      }
    }
    catch (ElasticsearchException e) {
      throw new IDBPersistenceException();
    }
  }

  @Override
  public void storeData(List data) throws IDBPersistenceException {
    try {
      if (data != null) {
        log.debug("storeData() - Try to send data by batch of size " + data.size());
        Collection<EsTagImpl> tags = new ArrayList<>();
        for (Object object : data) {
          if (object instanceof EsTagImpl) {
            log.debug("storeData() - type: " + object.getClass());
            tags.add((EsTagImpl) object);
          }
        }
        log.debug("storeData() - indexTags() a new Collection of " + tags.size() + " tags.");
        indexTags(tags);
      }
    }
    catch (ElasticsearchException e) {
      throw new IDBPersistenceException();
    }
  }

  /**
   * Index several {@link EsTagImpl} in the ElasticSearch cluster according to the BulkProcessor parameters.
   * @param tags to index.
   */
  public synchronized void indexTags(Collection<EsTagImpl> tags) {
    log.debug("indexTags() - Received a collection of " + tags.size() +  " tags to send by batch.");

    if (tags.size() == 0) {
      log.debug("indexTags() - Received a null List of tags to log to ElasticSearch.");
    }
    else {
      int counter = 0;
      updateCache();
      for (EsTagImpl tag : tags) {
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
  }

  /**
   * Initialize the JSON, indexName and type for the {@link EsTagImpl} and send it to the BulkProcessor.
   * @param tag to index.
   * @return true, if tag indexing was successful
   */
  private boolean sendTagToBatch(EsTagImpl tag) {
    String tagJson = tag.toString();
    String indexName = generateTagIndex(tag.getServerTimestamp());
    String type = generateTagType(tag.getDataType());

    if (log.isTraceEnabled()) {
      log.trace("sendTagToBatch() - Index a new tag.");
      log.trace("sendTagToBatch() - Index = " + indexName);
      log.trace("sendTagToBatch() - Type = " + type);
    }

    return indexByBatch(indexName, type, tagJson, tag);
  }

  private String generateTagIndex(long serverTime) {
    return retrieveIndexFormat(indexPrefix, serverTime);
  }

  private String generateTagType(String dataType) {
    return typePrefix + dataType.toLowerCase();
  }

  /**
   * Check in-memory if index and types are already present.
   * If yes, add to the batch directly,
   * otherwise, instantiate a new index with a new mapping.
   * @return if the cluster has acked the writing.
   */
  private boolean indexByBatch(String index, String type, String json, EsTagImpl tag) {
    if (tag == null) {
      log.warn("indexByBatch() - Error while indexing data. Tag has null value");
      return false;
    }
    else if (index == null || type == null || !checkIndex(index) || !checkType(type)) {
      log.warn("indexByBatch() - Error while indexing data. Bad index or type values: " + index + ", " + type + ". Tag #" + tag.getId() + " will not be sent to elasticsearch!");
      return false;
    }
    else {
      boolean indexIsPresent = createNotExistingIndex(index);
      boolean typeIsPresent = createNotExistingMapping(index, type);
      log.debug("indexByBatch() - New IndexRequest for index" + index + " and source " + json);
      if (indexIsPresent && typeIsPresent) {
        IndexRequest indexNewTag = new IndexRequest(index, type).source(json).routing(String.valueOf(tag.getId()));
        return connector.bulkAdd(indexNewTag);
      }
      return false;
    }
  }

  private boolean checkIndex(String index) {
    return index.matches("^" + indexPrefix + "\\d\\d\\d\\d-\\d\\d-?\\d?\\d?$");
  }

  private boolean checkType(String type) {
    String dataType = type.substring(typePrefix.length());

    return type.matches("^" + typePrefix + ".+$") && (EsMapping.ValueType.matches(dataType));
  }

  private boolean createNotExistingIndex(String index) {
    boolean indexExists = indexExists(index);
    if (!indexExists) {
      boolean isIndexed = instantiateIndex(index);
      if (isIndexed) {
        addIndex(index);
      }
      return isIndexed;
    }
    return indexExists;
  }

  /** Check if the index is present in-memory. */
  private boolean indexExists(String index) {
    return cacheIndicesTypes.containsKey(index);
  }

  /**
   * Try to add a new index with name {@param index} to ElasticSearch.
   * @return true if acked by the cluster.
   */
  private boolean instantiateIndex(String index) {
    if (cacheIndicesTypes.containsKey(index) || !checkIndex(index)) {
      log.debug("instantiateIndex() - Bad index: " + index + ".");
      return false;
    }
    return connector.handleIndexQuery(index, null, null);
  }

  private boolean createNotExistingMapping(String index, String type) {
    boolean mappingExists = mappingExists(index, type);
    if (!mappingExists) {
      boolean isInstantiated = instantiateType(index, type);
      if (isInstantiated) {
        addType(index, type);
      }
      return isInstantiated;
    }
    return mappingExists;
  }

  /** Check in memory Map to know if the type has been assigned to this index. */
  private boolean mappingExists(String index, String type) {
    boolean indexPresent = cacheIndicesTypes.containsKey(index);
    boolean typePresent = typeIsPresent(index, type);
    return (indexPresent && typePresent);
  }

  /** Check the in memory Map to know if a type is assigned to an index. */
  private boolean typeIsPresent(String index, String type) {
    Set<String> types = cacheIndicesTypes.get(index);
    return types!= null && types.contains(type);
  }

  /** Try to add a new type to ElasticSearch to the specified index with the specified mapping. */
  private boolean instantiateType(String index, String type) {
    if ((cacheIndicesTypes.containsKey(index) && cacheIndicesTypes.get(index).contains(type)) || !checkIndex(index) || !checkType(type)) {
      log.warn("instantiateType() - Bad type adding to index " + index + ", type: " + type);
    }

    String mapping = null;
    if (!typeIsPresent(index, type)) {
      mapping = chooseMapping(type.substring(typePrefix.length()));
      log.debug("instantiateIndex() - Adding a new mapping to index " + index + " for type " + type + ": " + mapping);
    }
    return connector.handleIndexQuery(index, type, mapping);
  }

  /** Choose a {@link EsMapping} (to index the data in the right way in ElasticSearch) according to the {@param dataType}. */
  private String chooseMapping(String dataType) {
    log.trace("chooseMapping() - Choose mapping for type " + dataType);
    if (EsMapping.ValueType.isBoolean(dataType)) {
      return new EsBooleanTagMapping(EsMapping.ValueType.boolType).getMapping();
    }
    else if (EsMapping.ValueType.isString(dataType)) {
      return new EsStringTagMapping(EsMapping.ValueType.stringType).getMapping();
    }
    else if (EsMapping.ValueType.isNumeric(dataType)) {
      return new EsNumericTagMapping(EsMapping.ValueType.doubleType).getMapping();
    }
    else {
      return null;
    }
  }


  /**
   * Add an index in-memory cache after it has been created in ElasticSearch. Called by the writing of a new Index if it was successful.
   * @param indexName name of the index created in ElasticSearch.
   */
  public synchronized void addIndex(String indexName) {
    if (checkIndex(indexName)) {
      cacheIndicesTypes.put(indexName, new HashSet<String>());
      log.debug("addIndex() - Added index " + indexName + " in memory list.");
    }
    else {
      throw new IllegalArgumentException("addIndex() - Index " + indexName + " does not follow the format \"indexPrefix_dateFormat\".");
    }
  }

  /**
   * Add a type in-memory cache. Called by the writing of a new Index if it was successful.
   * @param typeName type defined for the new document.
   */
  public synchronized void addType(String index, String typeName) {
    if (checkType(typeName) && cacheIndicesTypes.containsKey(index)) {
      cacheIndicesTypes.get(index).add(typeName);
      log.debug("addType() - Added type " + typeName + " in memory list.");
    }
    else {
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
      cacheIndicesTypes.put(index, new HashSet<String>());
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