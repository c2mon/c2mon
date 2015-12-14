package cern.c2mon.server.eslog.structure.types;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cern.c2mon.server.eslog.structure.mappings.TagESMapping;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Class that represents a Tag for ElasticSearch.
 * Used as "type" in ElasticSearch.
 * @author Alban Marguet.
 */
@Slf4j
@Data
public abstract class TagES implements TagESInterface {
  private Map<String, String> metadataProcess;
  private long tagId;
  private String tagName;
  private String dataType;
  private long tagTime;
  private long tagServerTime;
  private long tagDaqTime;
  private int tagStatus;
  private String quality; //tagstatusdesc
  protected Object tagValue;
  private String tagValueDesc;
  protected transient TagESMapping mapping;


  public TagES() {
    this.metadataProcess = new HashMap<>();
    this.mapping = new TagESMapping();
  }

  abstract public void setTagValue(Object tagValue);

  public String getMapping() {
    return mapping.getMapping();
  }

  public void setMapping(String tagValueType) {
    //TODO: check type
    mapping.setProperties(tagValueType);
  }

  /**
   * Return a JSON representing the TagES for indexing in ElasticSearch.
   * @return String representing the TagES in JSON.
   */
  public String build() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(this);

    log.debug(json);
    return json;
  }


  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append(getTagId());
    str.append('\t');
    str.append(getTagName());
    str.append('\t');
    str.append("tagValue");
    str.append('\t');
    str.append(getTagValueDesc());
    str.append('\t');
    str.append(getDataType());
    str.append('\t');
    str.append(getTagTime());
    str.append('\t');
    str.append(getTagDaqTime());
    str.append('\t');
    str.append(getTagServerTime());
    str.append('\t');
    str.append(getTagStatus());
    str.append('\t');
    if ((getQuality() != null) && (getQuality().equals(""))) {
      str.append("null");
    } else {
      str.append(getQuality());
    }
    str.append('\t');
    for (String metadata : getMetadataProcess().values()) {
      str.append(metadata);
      str.append('\t');
    }
    return str.toString();
  }
}