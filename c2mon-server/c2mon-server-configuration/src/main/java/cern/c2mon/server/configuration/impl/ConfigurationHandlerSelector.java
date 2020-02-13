package cern.c2mon.server.configuration.impl;

import cern.c2mon.server.configuration.handler.BaseConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.*;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static cern.c2mon.server.common.util.KotlinAPIs.applyNotNull;
import static cern.c2mon.shared.client.configuration.ConfigConstants.Entity.*;
import static java.util.Collections.unmodifiableMap;

@Named
@Slf4j
class ConfigurationHandlerSelector {

  private final Map<ConfigConstants.Entity, BaseConfigHandler> handlerMap;
  /**
   * Unique id for all generated changes (including those recursive ones during removal)
   */
  private AtomicInteger changeId = new AtomicInteger(0);

  @Inject
  public ConfigurationHandlerSelector(AlarmConfigHandler alarmConfigHandler,
                                      AliveTagConfigHandler aliveTagConfigHandler,
                                      CommandTagConfigHandler commandTagConfigHandler,
                                      CommFaultConfigHandler commFaultConfigHandler,
                                      ControlTagHandlerSelector controlTagHandlerSelector,
                                      DataTagConfigHandler dataTagConfigHandler,
                                      DeviceClassConfigHandler deviceClassConfigHandler,
                                      DeviceConfigHandler deviceConfigHandler,
                                      EquipmentConfigHandler equipmentConfigHandler,
                                      ProcessConfigHandler processConfigHandler,
                                      RuleTagConfigHandler ruleTagConfigHandler,
                                      StateTagConfigHandler stateTagConfigHandler,
                                      SubEquipmentConfigHandler subEquipmentConfigHandler) {
    handlerMap = unmodifiableMap(
      apply(new HashMap<>(), map -> {
        map.put(ALARM, alarmConfigHandler);
        map.put(ALIVETAG, aliveTagConfigHandler);
        map.put(COMMANDTAG, commandTagConfigHandler);
        map.put(COMMFAULTTAG, commFaultConfigHandler);
        map.put(CONTROLTAG, controlTagHandlerSelector);
        map.put(DATATAG, dataTagConfigHandler);
        map.put(DEVICE, deviceConfigHandler);
        map.put(DEVICECLASS, deviceClassConfigHandler);
        map.put(EQUIPMENT, equipmentConfigHandler);
        map.put(PROCESS, processConfigHandler);
        map.put(RULETAG, ruleTagConfigHandler);
        map.put(STATETAG, stateTagConfigHandler);
        map.put(SUBEQUIPMENT, subEquipmentConfigHandler);
      }));
  }

  /**
   * Applies a single configuration element. On the DB level, this action should
   * either be entirely applied or the transaction rolled back. In the case of
   * a rollback, the cache should also reflect this rollback (emptied and reloaded
   * for instance).
   *
   * @param element       the details of the configuration action
   * @param elementReport report that should be set to failed if there is a problem
   * @return list of DAQ configuration events; is never null but may be empty
   * @throws IllegalAccessException
   **/
  public List<ProcessChange> applyConfigElement(final ConfigurationElement element,
                                                final ConfigurationElementReport elementReport) {

    //initialize the DAQ config event
    final List<ProcessChange> daqConfigEvents = new ArrayList<>();
    log.trace(element.getConfigId() + " Applying configuration element with sequence id " + element.getSequenceId());

    if (element.getAction() == null || element.getEntity() == null || element.getEntityId() == null) {
      elementReport.setFailure("Parameter missing in configuration line with sequence id " + element.getSequenceId());
      return Collections.emptyList();
    }

    List<ProcessChange> processChanges = chooseHandlerThenDo(
      element.getEntity(),
      handler -> {
        switch (element.getAction()) {
          case CREATE:
            return handler.create(element);
          case UPDATE:
            return handler.update(element.getEntityId(), element.getElementProperties());
          case REMOVE:
            return handler.remove(element.getEntityId(), elementReport);
          default:
            elementReport.setFailure("Unrecognized reconfiguration action: " + element.getAction());
            log.warn("Unrecognized reconfiguration action: {} - see reconfiguration report for details.", element.getAction());
            return null;
        }
      }
    );

    applyNotNull(processChanges, daqConfigEvents::addAll);

    //set *unique* change id (single element may trigger many changes e.g. rule removal)
    for (ProcessChange processChange : daqConfigEvents) {
      if (processChange.processActionRequired()) {
        processChange.getChangeEvent().setChangeId(changeId.getAndIncrement());
      }
    }

    return daqConfigEvents;
  }

  private List<ProcessChange> chooseHandlerThenDo(ConfigConstants.Entity entity, Function<BaseConfigHandler, List<ProcessChange>> action) {
    if (!handlerMap.containsKey(entity)) {
      log.warn("Unrecognized reconfiguration entity: {} ", entity);
    }

    return action.apply(handlerMap.get(entity));
  }
}
