package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.configuration.impl.ProcessChange;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.function.Supplier;

@Named
public class StateTagConfigHandler extends BaseConfigHandlerImpl<SupervisionStateTag, ProcessChange> {

  @Inject
  protected StateTagConfigHandler(C2monCache<SupervisionStateTag> cache,
                                  ConfigurableDAO<SupervisionStateTag> cacheLoaderDAO,
                                  AbstractCacheObjectFactory<SupervisionStateTag> factory,
                                  Supplier<ProcessChange> defaultValue) {
    super(cache, cacheLoaderDAO, factory, defaultValue);
  }
}
