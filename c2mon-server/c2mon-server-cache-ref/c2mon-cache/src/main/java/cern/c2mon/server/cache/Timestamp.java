package cern.c2mon.server.cache;

import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;

public class Timestamp implements Cacheable {
  @Getter
  private long timeInMillis;

  private long id;

  @Override
  public Long getId() {
    return id;
  }

  public Timestamp(String key, long timeInMillis) {
    this((long) key.hashCode(), timeInMillis);
  }

  public Timestamp(Long id, long timeInMillis) {
    this.id = id;
    this.timeInMillis = timeInMillis;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
