package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.server.common.util.KotlinAPIs;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.PropertiesAccessor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
@Slf4j
public class ControlTagHandlerCollection {

  private final AliveTagConfigHandler aliveTagConfigHandler;
  private final CommFaultConfigHandler commFaultConfigHandler;
  private final StateTagConfigHandler stateTagConfigHandler;

  @Inject
  public ControlTagHandlerCollection(AliveTagConfigHandler aliveTagConfigHandler, CommFaultConfigHandler commFaultConfigHandler, StateTagConfigHandler stateTagConfigHandler) {
    this.aliveTagConfigHandler = aliveTagConfigHandler;
    this.commFaultConfigHandler = commFaultConfigHandler;
    this.stateTagConfigHandler = stateTagConfigHandler;
  }

  public List<ProcessChange> createIfMissing(ConfigurationElement element) {
    List<ProcessChange> changes = new ArrayList<>();

    if(element.getElementProperties().getProperty("aliveTagId") != null)
      changes.addAll(aliveTagConfigHandler.createBySupervised(element));

    if(element.getElementProperties().getProperty("commFaultTagId") != null)
      changes.addAll(commFaultConfigHandler.createBySupervised(element));

    if(element.getElementProperties().getProperty("stateTagId") != null)
      changes.addAll(stateTagConfigHandler.createBySupervised(element));

    return changes;
  }

  public List<ProcessChange> cascadeRemove(Supervised supervised, ConfigurationElementReport report) {
    List<ProcessChange> changes = new ArrayList<>();

    KotlinAPIs.applyNotNull(supervised.getAliveTagId(), aliveTagId ->
      changes.addAll(aliveTagConfigHandler.remove(aliveTagId, report)
      ));

    if (supervised instanceof AbstractEquipment) {
      KotlinAPIs.applyNotNull(((AbstractEquipment) supervised).getCommFaultTagId(), commFaultTagId ->
        changes.addAll(commFaultConfigHandler.remove(commFaultTagId, report))
      );
    }

    KotlinAPIs.applyNotNull(supervised.getStateTagId(), stateTagId ->
      changes.addAll(stateTagConfigHandler.remove(stateTagId, report))
    );

    return changes;
  }
}
