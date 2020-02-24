package cern.c2mon.cache.actions.oscillation;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alarm.OscillationTimestamp;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * The oscillation cache is a support cache with only one key:
 * {@link OscillationTimestamp#DEFAULT_ID}
 *
 * @author Alexandros Papageorgiou Koufidis
 */
@Named
@Singleton
public class OscillationService extends AbstractCacheServiceImpl<OscillationTimestamp> {

  @Inject
  public OscillationService(C2monCache<OscillationTimestamp> lastAccessCache) {
    super(lastAccessCache, new OscillationC2monCacheFlow());
  }

  /**
   * Initialize the cache with a value
   */
  @PostConstruct
  public void init() {
    setLastOscillationCheck(0);
  }

  public long getLastOscillationCheck() {
    return cache.get(OscillationTimestamp.DEFAULT_ID).getTimeInMillis();
  }

  void setLastOscillationCheck(long timestampMillis) {
    cache.put(OscillationTimestamp.DEFAULT_ID, new OscillationTimestamp(timestampMillis));
  }
}
