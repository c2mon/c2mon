package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.actions.commfault.CommFaultService;
import cern.c2mon.cache.actions.commfault.CommFaultTagCacheObjectFactory;
import cern.c2mon.server.cache.loading.CommFaultTagDAO;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.configuration.parser.factory.CommFaultTagFactory;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.common.PropertiesAccessor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
@Slf4j
public class CommFaultConfigHandler extends AbstractControlTagConfigHandler<CommFaultTag, cern.c2mon.shared.client.configuration.api.tag.CommFaultTag> {

  protected CommFaultConfigHandler(CommFaultService commFaultService,
                                   CommFaultTagDAO commFaultTagDAO,
                                   CommFaultTagCacheObjectFactory factory,
                                   CommFaultTagFactory commFaultTagFactory) {
    super(commFaultService, commFaultTagDAO, factory, commFaultTagFactory);
  }

  @Override
  public List<ProcessChange> createBySupervised(ConfigurationElement configurationElement) {

    ConfigConstants.Entity entity = configurationElement.getEntity();
    String name = configurationElement.getElementProperties().getProperty("name");

    if (!(entity== ConfigConstants.Entity.EQUIPMENT || entity == ConfigConstants.Entity.SUBEQUIPMENT)) {
      log.debug("Cannot create commFaultTag for object that isn't an Equipment: {} {} #{}",
        entity, name, configurationElement.getEntityId());
      return new ArrayList<>();
    }

    cern.c2mon.shared.client.configuration.api.tag.CommFaultTag.CreateBuilder builder =
      cern.c2mon.shared.client.configuration.api.tag.CommFaultTag.create(name + ":COMM_FAULT")
        .description("Communication fault tag for " + entity + " " + name);

    new PropertiesAccessor(configurationElement.getElementProperties())
      .getLong("commFaultTagId").ifPresent(builder::id);

    return super.createBySupervised(configurationElement, "commFaultTagId", builder::build);
  }
}
