package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.config.state.SupervisionStateTagCacheObjectFactory;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.configuration.parser.factory.SupervisionStateTagFactory;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.common.PropertiesAccessor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@Slf4j
public class StateTagConfigHandler extends AbstractControlTagConfigHandler<SupervisionStateTag, StatusTag> {

  @Inject
  protected StateTagConfigHandler(SupervisionStateTagService stateTagService,
                                  ConfigurableDAO<SupervisionStateTag> cacheLoaderDAO,
                                  SupervisionStateTagCacheObjectFactory factory,
                                  SupervisionStateTagFactory stateTagFactory) {
    super(stateTagService, cacheLoaderDAO, factory, stateTagFactory);
  }

  public List<ProcessChange> createBySupervised(ConfigurationElement element) {
    ConfigConstants.Entity entity = element.getEntity();
    String name = element.getElementProperties().getProperty("name");

    StatusTag.CreateBuilder builder = StatusTag.create(name + ":STATUS")
      .description("State tag for " + entity + " " + name);

    new PropertiesAccessor(element.getElementProperties())
      .getLong("stateTagId").ifPresent(builder::id);

    return super.createBySupervised(element, "stateTagId", builder::build);
  }
}
