package cern.c2mon.server.eslog.structure.types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;
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
  private long id;
  private String name;
  private String dataType;
  private long sourceTimestamp;
  private long serverTimestamp;
  private long daqTimestamp;
  private int status;
  private String quality; //tagstatusdesc
  protected transient Object value;
  protected Boolean valueBoolean;
  protected String valueString;
  protected Number valueNumeric;
  private String valueDescription;
  
  private String process;
  private String equipment;
  private String subEquipment;

  abstract public void setValue(Object tagValue);

  public String build() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(this);
    log.debug(json);
    return json;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append(getId());
    str.append('\t');
    str.append(getName());
    str.append('\t');
    str.append("value");
    str.append('\t');
    str.append(getValueDescription());
    str.append('\t');
    str.append(getDataType());
    str.append('\t');
    str.append(getSourceTimestamp());
    str.append('\t');
    str.append(getDaqTimestamp());
    str.append('\t');
    str.append(getServerTimestamp());
    str.append('\t');
    str.append(getStatus());
    str.append('\t');
    str.append(getQualityAppend(str));
    str.append('\t');
    str.append(getProcess());
    str.append('\t');
    str.append(getEquipment());
    str.append('\t');
    str.append(getSubEquipment());
    return str.toString();
  }

  private String getQualityAppend(StringBuilder str) {
    boolean qualityIsEmpty = (getQuality() != null) && (getQuality().equals(""));
    if (qualityIsEmpty) {
      return "nullQuality";
    } else {
      return getQuality();
    }
  }
}