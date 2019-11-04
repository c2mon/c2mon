package cern.c2mon.server.common.util;

import cern.c2mon.shared.client.metadata.Metadata;

import java.util.*;

public class MetadataUtils {

  public static cern.c2mon.server.common.metadata.Metadata parseMetadataConfiguration(Properties properties, cern.c2mon.server.common.metadata.Metadata currentMetadata) {
    return Optional.ofNullable(properties.getProperty("metadata"))
        .map(Metadata::fromJSON)
        .map(newMetadata -> parseNewMetadataConfiguration(currentMetadata, newMetadata))
        .orElse(currentMetadata);
  }

  private static cern.c2mon.server.common.metadata.Metadata parseNewMetadataConfiguration(cern.c2mon.server.common.metadata.Metadata currentMetadata, Metadata clientMetadata) {
    cern.c2mon.server.common.metadata.Metadata resultMetadata = new cern.c2mon.server.common.metadata.Metadata();
    if (clientMetadata.isUpdate()) {
      resultMetadata.setMetadata(parseUpdateRequest(currentMetadata, clientMetadata));
    } else {
      resultMetadata.setMetadata(clientMetadata.getMetadata());
    }
    return resultMetadata;
  }

  private static Map<String, Object> parseUpdateRequest(cern.c2mon.server.common.metadata.Metadata currentMetadata, Metadata clientMetadata) {
    Set<String> removeList = new HashSet<>(clientMetadata.getRemoveList());
    Map<String, Object> newMetadata = currentMetadata.getMetadata()
        .entrySet()
        .stream()
        .filter(entry -> !removeList.contains(entry.getKey()))
        .collect(HashMap::new,
            (map, entry) -> map.put(entry.getKey(), entry.getValue()),
            HashMap::putAll);
    clientMetadata.getMetadata()
        .forEach(newMetadata::put);
    return newMetadata;
  }
}

