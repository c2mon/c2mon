package cern.c2mon.server.elasticsearch;

import cern.c2mon.server.elasticsearch.alarm.AlarmDocument;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocument;
import cern.c2mon.server.elasticsearch.tag.TagDocument;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocument;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Factory for creating Elasticsearch mapping sources.
 *
 * @author Justin Lewis Salmon
 */
public class MappingFactory {

  private static final String TAG_MAPPING = "mappings/tag.json";
  private static final String TAG_CONFIG_MAPPING = "mappings/tag-config.json";
  private static final String ALARM_MAPPING = "mappings/alarm.json";
  private static final String SUPERVISION_MAPPING = "mappings/supervision.json";

  /**
   * Create the Elasticsearch mapping for a {@link TagConfigDocument}.
   *
   * @return the JSON mapping source
   */
  public static String createTagConfigMapping() {
    return loadMapping(TAG_CONFIG_MAPPING);
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
   * Create the Elasticsearch mapping for a {@link TagDocument}.
   *
   * @return the JSON mapping source
   */
  public static String createTagMapping() {
    return loadMapping(TAG_MAPPING);
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
