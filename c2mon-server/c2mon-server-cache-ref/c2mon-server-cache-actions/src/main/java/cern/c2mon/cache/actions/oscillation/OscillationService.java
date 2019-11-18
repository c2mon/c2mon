package cern.c2mon.cache.actions.oscillation;

import cern.c2mon.cache.actions.AbstractCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alarm.OscillationTimestamp;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * The oscillation cache is a support cache with only one key:
 * {@link OscillationTimestamp#DEFAULT_ID}
 *
 * @author Alexandros Papageorgiou Koufidis
 */
@Service
public class OscillationService extends AbstractCacheService<OscillationTimestamp> {

  @Inject
  public OscillationService(C2monCache<OscillationTimestamp> lastAccessCache) {
    super(lastAccessCache, new OscillationC2monCacheFlow());
  }

  public long getLastOscillationCheck() {
    return cache.get(OscillationTimestamp.DEFAULT_ID).getTimeInMillis();
  }

  void setLastOscillationCheck(long timestampMillis) {
    cache.put(OscillationTimestamp.DEFAULT_ID, new OscillationTimestamp(timestampMillis));
  }
}
