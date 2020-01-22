package cern.c2mon.server.configuration.junit;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.CacheListenerManagerImpl;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.common.tag.Tag;
import org.junit.rules.ExternalResource;

import javax.inject.Named;
import java.util.List;

import static cern.c2mon.server.common.util.Java9Collections.listOf;

@Named
class CloseRuleTagCacheListeners extends ExternalResource {

  private final List<C2monCache<? extends Tag>> caches;

  public CloseRuleTagCacheListeners(C2monCache<RuleTag> ruleTagCache,
                                    C2monCache<DataTag> dataTagCache,
                                    C2monCache<AliveTag> aliveTagCache,
                                    C2monCache<CommFaultTag> commFaultTagCache,
                                    C2monCache<SupervisionStateTag> supervisionStateTagCache) {
    caches = listOf(ruleTagCache, dataTagCache, aliveTagCache, commFaultTagCache, supervisionStateTagCache);
  }

  @Override
  protected void before() {
    caches.forEach(cache -> cache.setCacheListenerManager(new CacheListenerManagerImpl<>()));
  }
}
