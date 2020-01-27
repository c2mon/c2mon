/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.actions.datatag.DataTagCacheObjectFactory;
import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.server.configuration.handler.TagConfigHandler;
import cern.c2mon.server.configuration.impl.ConfigurationUpdateImpl;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.rule.RuleTagService;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.DataTagAdd;
import cern.c2mon.shared.daq.config.DataTagRemove;
import cern.c2mon.shared.daq.config.IChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.GenericApplicationContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * Implementation of transacted methods.
 *
 * @author Alexandros Papageorgiou
 */
@Named
@Slf4j
public class DataTagConfigHandler extends AbstractTagConfigHandler<DataTag> implements TagConfigHandler<DataTag> {

  private final DataTagService dataTagService;
  /**
   * Reference to the equipment facade.
   */
  private EquipmentService equipmentService;
  /**
   * Reference to the subequipment facade.
   */
  private SubEquipmentService subEquipmentService;
  /**
   * For recursive deletion of rules.
   */
  private RuleTagConfigHandler ruleTagConfigHandler;

  /**
   * For recursive deletion of alarms.
   */
  private AlarmConfigHandler alarmConfigHandler;

  /**
   * Helper class for accessing the List of registered listeners
   * for configuration updates.
   */
  private ConfigurationUpdateImpl configurationUpdateImpl;

  @Inject
  public DataTagConfigHandler(final DataTagService dataTagService,
                              final DataTagLoaderDAO dataTagLoaderDAO,
                              final DataTagCacheObjectFactory dataTagCacheObjectFactory,
                              final EquipmentService equipmentService,
                              final SubEquipmentService subEquipmentService,
                              final RuleTagService ruleTagService,
                              final GenericApplicationContext context,
                              final RuleTagConfigHandler ruleTagConfigHandler,
                              final AlarmConfigHandler alarmConfigHandler,
                              final ConfigurationUpdateImpl configurationUpdateImpl) {
    super(dataTagService.getCache(), dataTagLoaderDAO, dataTagCacheObjectFactory, ruleTagService, context);
    this.equipmentService = equipmentService;
    this.subEquipmentService = subEquipmentService;
    this.dataTagService = dataTagService;
    this.ruleTagConfigHandler = ruleTagConfigHandler;
    this.alarmConfigHandler = alarmConfigHandler;
    this.configurationUpdateImpl = configurationUpdateImpl;
  }

  @Override
  protected void doPostCreate(DataTag dataTag) {
    super.doPostCreate(dataTag);
    cache.getCacheListenerManager().notifyListenersOf(CacheEvent.UPDATE_ACCEPTED, dataTag);
    configurationUpdateImpl.notifyListeners(dataTag.getId());
  }

  @Override
  protected List<ProcessChange> createReturnValue(DataTag dataTag, ConfigurationElement element) {
    return Collections.singletonList(createIChange(dataTag,
      () -> new DataTagAdd(element.getSequenceId(), dataTag.getEquipmentId(), dataTagService.generateSourceDataTag(dataTag)),
      () -> new DataTagAdd(element.getSequenceId(), subEquipmentService.getEquipmentIdForSubEquipment(dataTag.getSubEquipmentId()), dataTagService.generateSourceDataTag(dataTag)))
    );
  }

  private ProcessChange createIChange(DataTag dataTag, Supplier<IChange> eqEventGenerator, Supplier<IChange> subeqEventGenerator) {
    if (dataTag.getEquipmentId() != null) {
      return new ProcessChange(equipmentService.getProcessId(dataTag.getEquipmentId()), eqEventGenerator.get());
    } else if (dataTag.getSubEquipmentId() != null) {
      return new ProcessChange(subEquipmentService.getProcessId(dataTag.getSubEquipmentId()), subeqEventGenerator.get());
    } else {
      log.warn("Data tag #" + dataTag.getId() + " is not attached to any Equipment or Sub-Equipment. This should normally never happen.");
      return new ProcessChange();
    }
  }

  /**
   * Updates the DataTag configuration in the cache and
   * database.
   * Will block any attempt made to move the tag to another Equipment
   * and emit a warning
   *
   * @param id         the id of the tag
   * @param properties the properties containing the changes
   * @return an change event if action is necessary by the DAQ; otherwise null
   */
  @Override
  public List<ProcessChange> update(final Long id, final Properties properties) {
    log.trace("Updating DataTag " + id);
    removeKeyIfExists(properties, "equipmentId");
    removeKeyIfExists(properties, "subEquipmentId");

    return super.update(id, properties);
  }

  @Override
  protected void doPostUpdate(DataTag dataTag) {
    super.doPostUpdate(dataTag);
    configurationUpdateImpl.notifyListeners(dataTag.getId());
  }

  @Override
  protected List<ProcessChange> updateReturnValue(DataTag dataTag, Change change, Properties properties) {
    if (change.hasChanged())
      return Collections.singletonList(dataTag.getEquipmentId() != null
        ? new ProcessChange(equipmentService.getProcessId(dataTag.getEquipmentId()), change)
        : new ProcessChange(subEquipmentService.getProcessId(dataTag.getSubEquipmentId()), change));
    return Collections.singletonList(new ProcessChange());
  }

  @Override
  protected void doPreRemove(DataTag dataTag, ConfigurationElementReport elementReport) {
    createConfigRemovalReportsFor(Entity.ALARM, dataTag.getAlarmIds(), alarmConfigHandler.getCache())
      .forEach(elementReport::addSubReport);

    createConfigRemovalReportsFor(Entity.RULETAG, dataTag.getRuleIds(), ruleTagConfigHandler.getCache())
      .forEach(elementReport::addSubReport);

    // Alert listeners
    for (ConfigurationEventListener listener : configurationEventListeners) {
      listener.onConfigurationEvent(dataTag, Action.REMOVE);
    }
  }

  @Override
  protected List<ProcessChange> removeReturnValue(DataTag dataTag, ConfigurationElementReport report) {
    return Collections.singletonList(createIChange(dataTag, DataTagRemove::new, DataTagRemove::new));
  }

}
