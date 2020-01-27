package cern.c2mon.server.configuration.impl;

import cern.c2mon.server.configuration.handler.BaseConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.*;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static cern.c2mon.shared.client.configuration.ConfigConstants.Entity.*;
import static java.util.Collections.unmodifiableMap;

@Named
@Slf4j
public class ConfigurationHandlerActor {

  private final Map<ConfigConstants.Entity, BaseConfigHandler> handlerMap;

  @Inject
  public ConfigurationHandlerActor(AlarmConfigHandler alarmConfigHandler,
                                   AliveTagConfigHandler aliveTagConfigHandler,
                                   CommandTagConfigHandler commandTagConfigHandler,
                                   CommFaultConfigHandler commFaultConfigHandler,
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

  public List<ProcessChange> doWithHandler(ConfigConstants.Entity entity, Function<BaseConfigHandler<?>, List<ProcessChange>> action) {
    if (!handlerMap.containsKey(entity)) {
      log.warn("Unrecognized reconfiguration entity: {} ", entity);
    }

    return action.apply(handlerMap.get(entity));
  }
}
