package cern.c2mon.server.elasticsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.core.io.ClassPathResource;

import cern.c2mon.server.elasticsearch.alarm.AlarmDocument;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocument;
import cern.c2mon.server.elasticsearch.tag.TagDocument;
import cern.c2mon.shared.common.type.TypeConverter;

/**
 * Factory for creating Elasticsearch mapping sources.
 *
 * @author Justin Lewis Salmon
 */
public class MappingFactory {

  private static final String TAG_MAPPING = "mappings/tag.json";
  private static final String ALARM_MAPPING = "mappings/alarm.json";
  private static final String SUPERVISION_MAPPING = "mappings/supervision.json";

  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * Create the Elasticsearch mapping for a {@link TagDocument} based on its
   * data type.
   *
   * @param dataType the type of the document
   *
   * @return the JSON mapping source
   */
  public static String createTagMapping(String dataType) {
    Map<String, Map<String, Object>> mapping;

    try {
      mapping = mapper.readValue(loadMapping(TAG_MAPPING), new TypeReference<HashMap<String, Object>>() {
      });
    } catch (IOException e) {
      throw new RuntimeException("Error reading tag mapping from JSON resource", e);
    }

    Class<?> clazz = TypeConverter.getType(dataType);
    Map<String, Object> properties = mapping.get("properties");

    // We need to store the tag values inside fields with different names
    // depending on their type, since Elasticsearch won't allow different types,
    // each with an identically named field but mapped differently (e.g. one is
    // a string, the other is a number)
    if (clazz == null) {
      properties.put("valueObject", ImmutableMap.of("type", "object", "index", "analyzed"));

    } else if (Number.class.isAssignableFrom(clazz)) {
      properties.put("value", ImmutableMap.of("type", "double"));

      if (Long.class.isAssignableFrom(clazz)) {
        properties.put("valueLong", ImmutableMap.of("type", "long"));
      }
    } else if (Boolean.class.isAssignableFrom(clazz)) {
      properties.put("value", ImmutableMap.of("type", "double"));
      properties.put("valueBoolean", ImmutableMap.of("type", "boolean"));

    } else if (String.class.isAssignableFrom(clazz)) {
      properties.put("valueString", ImmutableMap.of("type", "string", "index", "not_analyzed"));

    } else {
      properties.put("valueObject", ImmutableMap.of("type", "object", "dynamic", "true"));
    }

    try {
      return mapper.writeValueAsString(mapping);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error creating tag mapping", e);
    }
  }

  /**
   * Create the Elasticsearch mapping for an {@link AlarmDocument}.
   *
   * @return the JSON mapping source
   */
  public static String createAlarmMapping() {
    return loadMapping(ALARM_MAPPING);
  }

  /**
   * Create the Elasticsearch mapping for a {@link SupervisionEventDocument}.
   *
   * @return the JSON mapping source
   */
  public static String createSupervisionMapping() {
    return loadMapping(SUPERVISION_MAPPING);
  }

  private static String loadMapping(String location) {
    return new BufferedReader(new InputStreamReader(loadResource(location)))
        .lines()
        .collect(Collectors.joining("\n"));
  }

  private static InputStream loadResource(String location) {
    try {
      return new ClassPathResource(location).getInputStream();
    } catch (IOException e) {
      throw new RuntimeException("Error loading resource", e);
    }
  }
}
