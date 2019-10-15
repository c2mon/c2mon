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
  public AbstractCacheableImpl clone() throws CloneNotSupportedException {
    AbstractCacheableImpl clone = (AbstractCacheableImpl) super.clone();
    // We don't check if non null because it's guaranteed (init value + setter check)
    clone.setCacheTimestamp((Timestamp) cacheTimestamp.clone());
    return clone;
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

  @Override
  public <T extends Cacheable> boolean isLaterThan(@NonNull T other) {
    // This check may turn out unnecessary, keep an eye on it and remove if a competing use case appears
    if (!canEqual(other))
      throw new IllegalArgumentException("Attempting to compare against class " + other.getClass()
        + " when own class is " + getClass());
    return true;
  }
}
