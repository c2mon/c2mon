package cern.c2mon.server.cache;

import lombok.Getter;

public enum TimestampKeyEnum {
  ALARM(0)
  ;

  @Getter
  private long value;

  TimestampKeyEnum(long value) {
    this.value = value;
  }
}
