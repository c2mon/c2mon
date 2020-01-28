package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.actions.commfault.CommFaultTagCacheObjectFactory;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.loading.CommFaultTagDAO;
import cern.c2mon.server.common.commfault.CommFaultTag;

import javax.inject.Named;
import java.util.ArrayList;

@Named
public class CommFaultConfigHandler extends BaseConfigHandlerImpl<CommFaultTag> {

  protected CommFaultConfigHandler(C2monCache<CommFaultTag> cache,
                                   CommFaultTagDAO commFaultTagDAO,
                                   CommFaultTagCacheObjectFactory factory) {
    super(cache, commFaultTagDAO, factory, ArrayList::new);
  }
}
