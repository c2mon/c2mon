package cern.c2mon.server.elasticsearch.structure.mappings;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;

/**
 * @author Justin Lewis Salmon
 */
@Getter
public class DynamicTemplate {
  Map nested_metadata_template = ImmutableMap.of(
      "path_match", "metadata.*",
      "match_mapping_type", "string",
      "mapping", ImmutableMap.of(
          "index", "not_analyzed"
      )
  );
}
