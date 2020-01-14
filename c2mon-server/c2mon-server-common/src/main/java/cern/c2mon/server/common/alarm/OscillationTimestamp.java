package cern.c2mon.server.common.alarm;

import cern.c2mon.server.common.AbstractCacheableImpl;
import cern.c2mon.shared.common.Cacheable;
import lombok.*;

@Getter
@Setter(AccessLevel.NONE)
@ToString
@EqualsAndHashCode(callSuper = true)
public class OscillationTimestamp extends AbstractCacheableImpl implements Cacheable {

  public static final long DEFAULT_ID = 0;
  private long timeInMillis;

  public OscillationTimestamp(long timeInMillis) {
    this(DEFAULT_ID, timeInMillis);
  }

  public OscillationTimestamp(long id, long timeInMillis) {
    super(id);
    this.timeInMillis = timeInMillis;
  }

}
