package cern.c2mon.server.common.alarm;

import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;

public class AlarmServiceTimestamp implements Cacheable {
  @Getter
  private long timeInMillis;

  private long id;

  public AlarmServiceTimestamp(Long id, long timeInMillis) {
    this.id = id;
    this.timeInMillis = timeInMillis;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public AlarmServiceTimestamp clone() throws CloneNotSupportedException {
    return (AlarmServiceTimestamp) super.clone();
  }
}
