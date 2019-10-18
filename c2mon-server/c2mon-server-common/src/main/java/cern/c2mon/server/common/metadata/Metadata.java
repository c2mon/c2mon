package cern.c2mon.server.common.metadata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Martin Flamm
 */
@Data
@Slf4j
public class Metadata implements Serializable, Cloneable {

  /** Serial version UID */
  private static final long serialVersionUID = -4640805019166816224L;


  private Map<String, Object> metadata = new HashMap<>();
  private static transient ObjectMapper mapper = new ObjectMapper();

  public static String toJSON(Metadata metadata) {
    try {
      return mapper.writeValueAsString(metadata.getMetadata());
    } catch (IOException e) {
      log.error("Exception caught while serializing metatata to JSON", e);
    }

    return null;
  }

  public static Map<String, Object> fromJSON(String json) {
    try {
      TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
      return mapper.readValue(json, typeRef);
    } catch (IOException e) {
      log.error("Exception caught while deserializing metadata from JSON", e);
    }

    return null;
  }

  /**
   * @return a map representation of the metadata
   */
  public Map<String, Object> toMap() {
    return new HashMap<>(this.metadata);
  }

  public void addMetadata(String key, Object value) {
    metadata.put(key, value);
  }

  public void removeMetadata(String key) {
    metadata.remove(key);
  }

  @Override
  public Metadata clone() {
    Metadata clone = null;
    try {
      clone = (Metadata) super.clone();
      clone.metadata = toMap();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
