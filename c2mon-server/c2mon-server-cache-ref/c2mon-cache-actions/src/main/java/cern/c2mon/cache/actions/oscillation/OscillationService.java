package cern.c2mon.cache.actions.oscillation;

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
public class OscillationService {

  private final C2monCache<OscillationTimestamp> lastAccessCache;

  @Inject
  public OscillationService(C2monCache<OscillationTimestamp> lastAccessCache) {
    this.lastAccessCache = lastAccessCache;
  }

  public long getLastOscillationCheck(){
    return lastAccessCache.get(OscillationTimestamp.DEFAULT_ID).getTimeInMillis();
  }

  void setLastOscillationCheck(long timestampMillis){
    lastAccessCache.put(OscillationTimestamp.DEFAULT_ID, new OscillationTimestamp(timestampMillis));
  }
}
