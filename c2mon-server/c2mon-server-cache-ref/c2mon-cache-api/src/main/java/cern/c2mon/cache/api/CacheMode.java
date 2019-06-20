package cern.c2mon.cache.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum CacheMode {

  REPLICATED("REPLICATED"), PARTITIONED("PARTITIONED");

  @Getter
  private final String mode;
}
