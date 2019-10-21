package cern.c2mon.cache.config.oscillation;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.AbstractCacheCRUDTest;
import cern.c2mon.server.common.alarm.OscillationTimestamp;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class OscillationCacheCRUDTest extends AbstractCacheCRUDTest<OscillationTimestamp> {

  @Autowired
  private C2monCache<OscillationTimestamp> oscillationTimestampCache;

  /**
   * Guaranteed to run after parent before, so cache has been init'd
   *
   * @see <a href=https://junit.org/junit4/javadoc/latest/org/junit/Before.html>Junit 4 Before doc</a>
   */
  @Before
  public void init(){
    if (!oscillationTimestampCache.containsKey(OscillationTimestamp.DEFAULT_ID))
      oscillationTimestampCache.put(OscillationTimestamp.DEFAULT_ID, getSample());
  }

  @Override
  protected OscillationTimestamp getSample() {
    return new OscillationTimestamp(10000000);
  }

  @Override
  protected Long getExistingKey() {
    return OscillationTimestamp.DEFAULT_ID;
  }

  @Override
  protected C2monCache<OscillationTimestamp> getCache() {
    return oscillationTimestampCache;
  }
}
