package cern.c2mon.cache.config.collections;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.set.CacheCollection;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Aggregates [Control,Data,Rule]tag caches
 *
 * @author Alexandros Papageorgiou Koufidis
 */
@Named
public class ControlCacheCollection extends CacheCollection<ControlTag> {

  @Inject
  public ControlCacheCollection(final C2monCache<AliveTag> aliveTagCache,
                                final C2monCache<CommFaultTag> commFaultTagCache,
                                final C2monCache<SupervisionStateTag> stateTagCache) {
    super(aliveTagCache, commFaultTagCache, stateTagCache);
  }
}
