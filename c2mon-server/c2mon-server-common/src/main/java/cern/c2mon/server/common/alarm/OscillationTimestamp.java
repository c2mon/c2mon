package cern.c2mon.server.common.alarm;

import cern.c2mon.server.common.AbstractCacheableImpl;
import cern.c2mon.shared.common.Cacheable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
@EqualsAndHashCode(callSuper = true)
public class OscillationTimestamp extends AbstractCacheableImpl implements Cacheable {

  public static final long DEFAULT_ID = 0;
  private long timeInMillis;
  private Long id;

  public OscillationTimestamp(long timeInMillis) {
    this(DEFAULT_ID, timeInMillis);
  }

  public OscillationTimestamp(long id, long timeInMillis) {
    this.id = id;
    this.timeInMillis = timeInMillis;
  }

}
