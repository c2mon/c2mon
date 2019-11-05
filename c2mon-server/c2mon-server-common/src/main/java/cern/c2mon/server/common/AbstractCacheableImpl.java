package cern.c2mon.server.common;

import cern.c2mon.shared.common.Cacheable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.sql.Timestamp;

/**
 * Should be used as common base for all objects residing in caches
 *
 * @author Alexandros Papageorgiou Koufidis
 */
@Data
@NoArgsConstructor
public abstract class AbstractCacheableImpl implements Cacheable {

  protected Long id;

  @EqualsAndHashCode.Exclude
  protected Timestamp cacheTimestamp = new Timestamp(0);

  @Override
  public AbstractCacheableImpl clone() {
    AbstractCacheableImpl clone = null;
    try {
      clone = (AbstractCacheableImpl) super.clone();
      // We don't check if non null because it's guaranteed (init value + setter check)
      clone.setCacheTimestamp((Timestamp) cacheTimestamp.clone());
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sets a new cache timestamp value
   * <p>
   * Removing the NonNull annotation here should only be done in conjunction with updating
   * the {@link AbstractCacheableImpl#clone()}!
   *
   * @param cacheTimestamp
   */
  @Override
  public void setCacheTimestamp(@NonNull Timestamp cacheTimestamp) {
    this.cacheTimestamp = cacheTimestamp;
  }
}
