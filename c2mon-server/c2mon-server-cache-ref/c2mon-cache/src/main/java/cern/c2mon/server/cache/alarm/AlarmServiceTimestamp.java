package cern.c2mon.server.cache.alarm;

import cern.c2mon.server.cache.TimestampKeyEnum;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;

public class AlarmServiceTimestamp implements Cacheable {
  @Getter
  private long timeInMillis;

  private long id;

  @Override
  public Long getId() {
    return id;
  }

  public AlarmServiceTimestamp(Long id, long timeInMillis) {
    this.id = id;
    this.timeInMillis = timeInMillis;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
