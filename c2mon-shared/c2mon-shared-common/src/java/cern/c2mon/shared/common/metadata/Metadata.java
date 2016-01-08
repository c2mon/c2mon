package cern.c2mon.shared.common.metadata;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by fritter on 30/11/15.
 */
@Data
@Slf4j
public class Metadata implements Serializable, Cloneable {

  private Map<String, Object> metadata = new HashMap<>();

  public static String toJSON(Metadata metadata) {
    return toJSON(metadata.getMetadata());
  }

  public static String toJSON(Map<String, Object> metadata) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(metadata);

    } catch (IOException e) {
      log.error("Exception caught while converting Metadata to a JSON String.", e);
    }
    return null;
  }

  public static Map<String, Object> fromJSON(String jsonString) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(jsonString, Map.class);

    } catch (IOException e) {
      log.error("fromJSON() method unable to create a Map", e);
    }
    return null;
  }

  @Builder
  public Metadata(@Singular("addMetadata") Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  public Metadata() {
  }

}
