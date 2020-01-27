package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.common.commfault.CommFaultTag;

import javax.inject.Named;
import java.util.ArrayList;

@Named
public class CommFaultConfigHandler extends BaseConfigHandlerImpl<CommFaultTag> {

  protected CommFaultConfigHandler(C2monCache<CommFaultTag> cache,
                                   ConfigurableDAO<CommFaultTag> cacheLoaderDAO,
                                   AbstractCacheObjectFactory<CommFaultTag> factory) {
    super(cache, cacheLoaderDAO, factory, ArrayList::new);
  }
}
