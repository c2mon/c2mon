package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.actions.alive.AliveTagCacheObjectFactory;
import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.server.cache.loading.AliveTagDAO;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.configuration.parser.factory.AliveTagFactory;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.common.PropertiesAccessor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@Slf4j
public class AliveTagConfigHandler extends AbstractControlTagConfigHandler<AliveTag, cern.c2mon.shared.client.configuration.api.tag.AliveTag> {

  @Inject
  public AliveTagConfigHandler(AliveTagService aliveTagService,
                               AliveTagDAO dao,
                               AliveTagCacheObjectFactory aliveTagCacheObjectFactory,
                               AliveTagFactory aliveTagFactory) {
    super(aliveTagService, dao, aliveTagCacheObjectFactory, aliveTagFactory);
  }

  @Override
  protected void doPostCreate(AliveTag aliveTag) {
    super.doPostCreate(aliveTag);

    ((AliveTagService) service).startOrUpdateTimestamp(aliveTag.getId(), System.currentTimeMillis());
  }

  @Override
  public List<ProcessChange> createBySupervised(ConfigurationElement configurationElement) {
    ConfigConstants.Entity entity = configurationElement.getEntity();
    String name = configurationElement.getElementProperties().getProperty("name");

    cern.c2mon.shared.client.configuration.api.tag.AliveTag.CreateBuilder aliveTagBuilder =
      cern.c2mon.shared.client.configuration.api.tag.AliveTag.create(name + ":ALIVE")
        .description("Alive tag for " + entity.toString() + " " + name);

    new PropertiesAccessor(configurationElement.getElementProperties())
      .getLong("aliveTagId").ifPresent(aliveTagBuilder::id);

    return super.createBySupervised(configurationElement, "aliveTagId", aliveTagBuilder::build);
  }

  /**
   * Given a ControlTag id, returns a create event for sending
   * to the DAQ layer if necessary. Returns null if no event needs
   * sending to the DAQ layer for this particular ControlTag.
   *
   * <p>Currently, only alive tags with a DataTagAddress are sent
   * to the DAQ layer. All other cases only need an update to the
   * Equipment itself.
   *
   * <p>Created ControlTags are only sent to the DAQ layer once they
   * are referenced by some Equipment (and hence also belong to a given
   * DAQ!). Updates to ControlTags can be sent immediately
   *
   * @param configId     the id of the configuration
   * @param controlTagId the id of the ControlTag that needs creating on the DAQ layer
   * @param equipmentId  the id of the Equipment this control tag is attached to (compulsory)
   * @param processId    the id of the Process to reconfigure
   * @return the change event including the process id
   */
  public ProcessChange getCreateEvent(final Long configId, final Long controlTagId, final Long equipmentId, final Long processId) {
    ProcessChange processChange = null;

    AliveTag aliveTimer = cache.get(controlTagId);

//    TODO (Alex) Turn this on when we have ControlTag events
//    if (aliveTimer.getAddress != null) {
//      DataTagAdd dataTagAdd = new DataTagAdd(configId, equipmentId, dataTagFacade.generateSourceDataTag(aliveTimer));
//      processChange = new ProcessChange(processId, dataTagAdd);
//    }

//        if (processFacade.getProcessIdFromControlTag(controlTag.getId()) != null) {
//          processChange = new ProcessChange(processFacade.getProcessIdFromControlTag(controlTag.getId()));
//        }

    return processChange;
  }
}
