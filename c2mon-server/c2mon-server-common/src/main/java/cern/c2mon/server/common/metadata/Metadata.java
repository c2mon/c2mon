package cern.c2mon.server.common.metadata;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Martin Flamm
 */
@Data
@Slf4j
public class Metadata implements Serializable, Cloneable {

  private Map<String, Object> metadata = new HashMap<>();
  private static transient ObjectMapper mapper = new ObjectMapper();

  public static String toJSON(Metadata metadata) {
    try {
      return mapper.writeValueAsString(metadata.getMetadata());
    }
    catch (IOException e) {
      log.error("Exception caught while serializing metatata to JSON", e);
    }

    return null;
  }

  public static Map<String, Object> fromJSON(String json) {
    try {
      TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
      };
      return mapper.readValue(json, typeRef);
    }
    catch (IOException e) {
      log.error("Exception caught while deserializing metatata from JSON", e);
    }

    return null;
  }

  public void addMetadata(String key, Object value) {
    metadata.put(key, value);
  }

  public void removeMetadata(String key) {
    metadata.remove(key);
  }
}
