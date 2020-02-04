package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.actions.state.SupervisionStateTagCacheObjectFactory;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.common.supervision.SupervisionStateTag;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class StateTagConfigHandler extends AbstractControlTagConfigHandler<SupervisionStateTag> {

  @Inject
  protected StateTagConfigHandler(SupervisionStateTagService stateTagService,
                                  ConfigurableDAO<SupervisionStateTag> cacheLoaderDAO,
                                  SupervisionStateTagCacheObjectFactory factory) {
    super(stateTagService, cacheLoaderDAO, factory);
  }
}
