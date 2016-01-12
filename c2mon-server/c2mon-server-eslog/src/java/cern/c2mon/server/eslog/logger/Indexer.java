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
package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.structure.mappings.Mapping;
import cern.c2mon.server.eslog.structure.mappings.TagBooleanMapping;
import cern.c2mon.server.eslog.structure.mappings.TagNumericMapping;
import cern.c2mon.server.eslog.structure.mappings.TagStringMapping;
import cern.c2mon.server.eslog.structure.types.TagES;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Used to write (a.k.a. index) the data to elasticSearch.
 * Makes use of the Connector connection to an ElasticSearch cluster.
 * @author Alban Marguet.
 */
@Service
@Slf4j
@Data
public class Indexer {
  /** Prefix used for every index in the ElasticSearch cluster, e.g., c2mon_2015-11 is a valid index. */
  private final String INDEX_PREFIX = "c2mon_";
  /** Every tag or alias must begin with the same prefix, e.g., tag_string is a good type and tag_207807 is a good alias. */
  private final String TAG_PREFIX = "tag_";
  /** The first index in the cluster is c2mon_1970-01 which corresponds to the Epoch time (ES stocks timestamps in milliseconds since Epoch). */
  private final String FIRST_INDEX = INDEX_PREFIX + "1970-01";

  /** Contains in-memory the content of the Indices, types and aliases present in the cluster. */
  private final Map<String, Set<String>> indicesTypes = new HashMap<>();
  private final Map<String, Set<String>> indicesAliases = new HashMap<>();

  /**
   * Handles the connection with the ElasticSearch cluster.
   */
  private Connector connector;

  /**
   * Is available if the Connector has found a connection.
   */
  private boolean isAvailable;

  @Autowired
  public Indexer(final Connector connector) {
    this.connector = connector;
  }

  @PostConstruct
  public void init() {
    waitForConnection();
    log.info("init() - Indexer is ready to write data to ElasticSearch.");
  }

  public void waitForConnection() {
    while(!connector.isConnected()) {
      isAvailable = false;
    }
    isAvailable = true;
  }

  /**
   * Index several tags in the ElasticSearch cluster according to the
   * BulkProcessor parameters.
   *
   * @param tags to index.
   */
  public void indexTags(Collection<TagES> tags) {
    isAvailable = checkConnection();
    if (isAvailable) {
      processData(tags);
    }
    else {
      log.warn("indexTags() - Launching fallBackMechanism because the connection has been interrupted.");
      launchFallBackMechanism(tags);
    }
  }

  public boolean checkConnection() {
    return connector.waitForYellowStatus();
  }

  public void processData(Collection<TagES> tags) {
    log.trace("processData() - Received a collection of " + tags.size() +  " tags to send by batch.");
    Map<String, TagES> aliases = new HashMap<>();

    if (tags.size() == 0) {
      log.trace("processData() - Received a null List of tags to log to ElasticSearch.");
    }
    else {
      int counter = 0;
      updateLists();
      for (TagES tag : tags) {
        if (sendTagToBatch(tag)) {
          counter++;
          // 1 by 1 = long running
          aliases.put(generateAliasName(tag.getId()), tag);
        }
      }

      log.trace("processData() - Created a batch composed of " + counter + " tags.");

      // FLUSH
      log.trace("processData() - closing bulk.");
      connector.closeBulk();

      for (String alias : aliases.keySet()) {
        addAliasFromBatch(generateIndex(aliases.get(alias).getServerTimestamp()), aliases.get(alias));
      }

      updateLists();
    }
  }

  /**
   * Save data if cluster not available.
   * @param tags to be indexed.
   */
  public void launchFallBackMechanism(Collection<TagES> tags) {
    connector.findClusterAndLaunchBulk();
    saveDataToFile(tags);
  }

  private void saveDataToFile(Collection<TagES> tags) {
    //TODO
  }


  /**
   * Add 1 TagES to index to the ElasticSearch cluster thanks to the
   * BulkProcessor.
   *
   * @param tag to index.
   * @return true, if tag indexing was successful
   */
  protected boolean sendTagToBatch(TagES tag) {
    String tagJson = tag.build();
    String indexName = generateIndex(tag.getServerTimestamp());
    String type = generateType(tag.getDataType());

    if (log.isTraceEnabled()) {
      log.trace("sendTagToBatch() - Index a new tag.");
      log.trace("sendTagToBatch() - Index = " + indexName);
      log.trace("sendTagToBatch() - Type = " + type);
    }

    return indexByBatch(indexName, type, tagJson, tag);
  }

  public String generateIndex(long serverTime) {
    return INDEX_PREFIX + millisecondsToYearMonth(serverTime);
  }

  public String generateType(String dataType) {
    return TAG_PREFIX + dataType.toLowerCase();
  }

  protected boolean indexByBatch(String index, String type, String json, TagES tag) {
    if (tag == null) {
      log.warn("indexByBatch() - Error while indexing data. Tag has null value");
      return false;
    }
    else if (index == null || type == null || !checkIndex(index) || !checkType(type)) {
      log.warn("indexByBatch() - Error while indexing data. Bad index or type values: " + index + ", " + type + ". Tag #" + tag.getId() + " will not be sent to elasticsearch!");
      return false;
    }
    else {
      createNotExistingIndex(index);
      createNotExistingMapping(index, type);

      IndexRequest indexNewTag = new IndexRequest(index, type).source(json).routing(String.valueOf(tag.getId()));
      return connector.bulkAdd(indexNewTag);
    }
  }

  public boolean checkIndex(String index) {
    return index.matches("^" + INDEX_PREFIX + "\\d\\d\\d\\d-\\d\\d$");
  }

  public boolean checkType(String type) {
    String dataType = type.substring(TAG_PREFIX.length());

    return type.matches("^" + TAG_PREFIX + ".+$") && (Mapping.ValueType.matches(dataType));
  }

  private void createNotExistingIndex(String index) {
    if (!indexExists(index)) {
      boolean isIndexed = instantiateIndex(index);
      if (isIndexed) {
        addIndex(index);
      }
    }
  }

  /**
   * Check if the index is present in memory Map.
   */
  private boolean indexExists(String index) {
    return indicesTypes.containsKey(index);
  }

  public boolean instantiateIndex(String index) {
    if (indicesTypes.containsKey(index) || !checkIndex(index)) {
      log.debug("instantiateIndex() - Bad index: " + index + ".");
      return false;
    }

    Settings indexSettings = connector.getIndexSettings("INDEX_MONTH");
    return connector.handleIndexQuery(index, indexSettings, null, null);
  }

  private void createNotExistingMapping(String index, String type) {
    if (!mappingExists(index, type)) {
      boolean isInstantiated = instantiateType(index, type);
      if (isInstantiated) {
        addType(index, type);
      }
    }
  }

  /**
   * Check in memory Map to know if the type has been assigned to this index.
   */
  private boolean mappingExists(String index, String type) {
    boolean indexPresent = indicesTypes.containsKey(index);
    boolean typePresent = typeIsPresent(index, type);
    return (indexPresent && typePresent);
  }

  /**
   * Check the in memory Map to know if a type is assigned to an index.
   */
  private boolean typeIsPresent(String index, String type) {
    Set<String> types = indicesTypes.get(index);
    return types!= null && types.contains(type);
  }

  public boolean instantiateType(String index, String type) {
    if ((indicesTypes.containsKey(index) && indicesTypes.get(index).contains(type)) || !checkIndex(index) || !checkType(type)) {
      log.warn("instantiateType() - Bad type adding to index " + index + ", type: " + type);
    }

    String mapping = null;
    if (!typeIsPresent(index, type)) {
      mapping = chooseMapping(type.substring(TAG_PREFIX.length()));
      log.debug("instantiateIndex() - Adding a new mapping to index " + index + " for type " + type + ": " + mapping);
    }

    return connector.handleIndexQuery(index, null, type, mapping);
  }

  /**
   * Choose a Mapping (to index the data in the right way in ElasticSearch) according to the dataType.
   */
  private String chooseMapping(String dataType) {
    log.trace("chooseMapping() - Choose mapping for type " + dataType);
    if (Mapping.ValueType.isBoolean(dataType)) {
      return new TagBooleanMapping(Mapping.ValueType.boolType).getMapping();
    }
    else if (Mapping.ValueType.isString(dataType)) {
      return new TagStringMapping(Mapping.ValueType.stringType).getMapping();
    }
    else if (Mapping.ValueType.isNumeric(dataType)) {
      return new TagNumericMapping(Mapping.ValueType.doubleType).getMapping();
    }
    else {
      return null;
    }
  }

  /**
   * Requires to be called by indexTags since we need the index to be existing in the cluster to add the new alias.
   */
  public boolean addAliasFromBatch(String indexMonth, TagES tag) {
    if (tag == null || !checkIndex(indexMonth)) {
      throw new IllegalArgumentException("addAliasFromBatch() - IllegalArgument (tag = " + tag + ", index = " + indexMonth + ").");
    }

    long id = tag.getId();
    String aliasName = generateAliasName(id);

    boolean canBeAdded = indicesAliases.keySet().contains(indexMonth) && !indicesAliases.get(indexMonth).contains(aliasName) && checkIndex(indexMonth) && checkAlias(aliasName);
    return canBeAdded && connector.handleAliasQuery(indexMonth, aliasName);
  }

  /**
   * Utility method. Aliases have the following format: "tag_tagId".
   *
   * @param id tag of the TagES for which to create Alias.
   * @return name of the alias for a given id.
   */
  public String generateAliasName(long id) {
    return TAG_PREFIX + id;
  }

  /**
   * Check if an alias has the right format: tag_tagId.
   */
  public boolean checkAlias(String alias) {
    return alias.matches("^" + TAG_PREFIX + "\\d+$");
  }

  /**
   * Add an index to the Set indices. Called by the writing of a new Index if it was successful.
   * @param indexName name of the index created in ElasticSearch.
   */
  public void addIndex(String indexName) {
    if (checkIndex(indexName)) {
      indicesTypes.put(indexName, new HashSet<String>());
      indicesAliases.put(indexName, new HashSet<String>());
      log.debug("addIndex() - Added index " + indexName + " in memory list.");
    }
    else {
      throw new IllegalArgumentException("Indices must follow the format \"c2mon_YYYY_MM\".");
    }
  }

  /**
   * Add a type to the Set types. Called by the writing of a new Index if it was successful.
   * @param typeName type defined for the new document.
   */
  public void addType(String index, String typeName) {
    if (checkType(typeName) && indicesTypes.containsKey(index)) {
      indicesTypes.get(index).add(typeName);
      log.debug("addType() - Added type " + typeName + " in memory list.");
    }
    else {
      throw new IllegalArgumentException("Types must follow the format \"tag_dataType\".");
    }
  }

  /**
   * Add an alias to the Set aliases. Called by the writing of a new alias if it was successful.
   * @param aliasName name of the alias to give.
   */
  public void addAlias(String index, String aliasName) {
    if (checkAlias(aliasName) && indicesAliases.containsKey(index)) {
      indicesAliases.get(index).add(aliasName);
      log.debug("addAlias() - Added alias " + aliasName + " in memory list.");
    }
    else {
      throw new IllegalArgumentException("Aliases must follow the format \"tag_tagId\".");
    }
  }

  /**
   * Milliseconds since Epoch time to YYYY-MM.
   */
  public String millisecondsToYearMonth(long millis) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

    Date date = new Date(millis);
    String timestamp = sdf.format(date);
    return timestamp.substring(0, 7);
  }

  /**
   * Query the ElasticSearch cluster to retrieve all the indices, types and
   * aliases present already at startup. Store them in memory in the Sets:
   * indices, types and aliases.
   */
  public void updateLists() {
    clearLists();
    updateIndices();
    updateTypes();
    updateAliases();
  }

  private void updateIndices() {
    for (String index : connector.updateIndices()) {
      indicesTypes.put(index, new HashSet<String>());
      indicesAliases.put(index, new HashSet<String>());
    }
  }

  private void updateTypes() {
    for (String index : indicesTypes.keySet()) {
      indicesTypes.get(index).addAll(connector.updateTypes(index));
    }
  }

  private void updateAliases() {
    for (String index : indicesAliases.keySet()) {
      indicesAliases.get(index).addAll(connector.updateAliases(index));
    }
  }

  private void clearLists() {
    indicesTypes.clear();
    indicesAliases.clear();
  }

  private void displayLists() {
    if (log.isTraceEnabled()) {
      log.trace("displayLists():");

      for (String index : indicesTypes.keySet()) {
        log.trace("Index:");
        log.trace(index);

        log.trace("Has types:");
        Set<String> types = indicesTypes.get(index);
        for (String type : types) {
          log.trace(type);
        }

//      log.trace("and aliases:");
//      for (String alias : aliases) {
//        log.trace(alias);
//      }
      }
    }
  }
}
