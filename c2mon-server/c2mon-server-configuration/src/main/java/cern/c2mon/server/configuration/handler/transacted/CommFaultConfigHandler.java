package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.actions.commfault.CommFaultService;
import cern.c2mon.cache.actions.commfault.CommFaultTagCacheObjectFactory;
import cern.c2mon.server.cache.loading.CommFaultTagDAO;
import cern.c2mon.server.common.commfault.CommFaultTag;

import javax.inject.Named;

@Named
public class CommFaultConfigHandler extends AbstractControlTagConfigHandler<CommFaultTag> {

  protected CommFaultConfigHandler(CommFaultService commFaultService,
                                   CommFaultTagDAO commFaultTagDAO,
                                   CommFaultTagCacheObjectFactory factory) {
    super(commFaultService, commFaultTagDAO, factory);
  }
}
