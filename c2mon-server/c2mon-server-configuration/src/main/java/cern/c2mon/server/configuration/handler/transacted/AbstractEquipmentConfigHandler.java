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

import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.actions.process.ProcessXMLProvider;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.EquipmentConfigurationUpdate;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Common functionalities for configuring Equipment and SubEquipment.
 *
 * @param <T> the type held in the cache
 * @author Mark Brightwell
 */
@Slf4j
public abstract class AbstractEquipmentConfigHandler<T extends AbstractEquipment> extends BaseConfigHandlerImpl<T> {

  protected final ProcessXMLProvider processXMLProvider;
  private final AliveTagConfigHandler aliveTagConfigEventHandler;
  protected final DataTagService dataTagService;
  protected final DataTagConfigHandler dataTagConfigHandler;
  protected final ControlTagHandlerCollection controlTagHandlerCollection;

  public AbstractEquipmentConfigHandler(
    final C2monCache<T> subEquipmentCache,
    final ConfigurableDAO<T> subEquipmentDAO,
    final AbstractCacheObjectFactory<T> subEquipmentCacheObjectFactory,
    final ProcessXMLProvider processXMLProvider,
    final AliveTagConfigHandler aliveTagConfigEventHandler,
    final DataTagService dataTagService,
    final DataTagConfigHandler dataTagConfigHandler,
    final ControlTagHandlerCollection controlTagHandlerCollection) {
    super(subEquipmentCache, subEquipmentDAO, subEquipmentCacheObjectFactory, ArrayList::new);
    this.processXMLProvider = processXMLProvider;
    this.aliveTagConfigEventHandler = aliveTagConfigEventHandler;
    this.dataTagService = dataTagService;
    this.dataTagConfigHandler = dataTagConfigHandler;
    this.controlTagHandlerCollection = controlTagHandlerCollection;
  }

  @Override
  protected List<ProcessChange> createReturnValue(T cacheable, ConfigurationElement element) {
    return updateControlTagInformation(element, cacheable);
  }

  @Override
  protected void doPostCreate(T cacheable) {
    super.doPostCreate(cacheable);
    cache.getCacheListenerManager().notifyListenersOf(CacheEvent.INSERTED, cacheable);
  }

  @Override
  public List<ProcessChange> update(Long id, Properties properties) {
    removeKeyIfExists(properties, "id");
    return super.update(id, properties);
  }

  @Override
  protected List<ProcessChange> updateReturnValue(T cacheable, Change change, Properties properties) {
    // create change event for DAQ layer
    Long processId = getProcessId(cacheable);

    ArrayList<ProcessChange> processChanges = new ArrayList<>();
    processChanges.add(new ProcessChange(processId, change));

    EquipmentConfigurationUpdate equipmentUpdate = (EquipmentConfigurationUpdate) change;
    // if alive tags associated to equipment are changed and have an address,
    // inform DAQ also (use same changeId so these become sub-reports of the
    // correct report)
    if (equipmentUpdate.getAliveTagId() != null) {
      ProcessChange processChange = aliveTagConfigEventHandler
        .getCreateEvent(equipmentUpdate.getChangeId(), cacheable.getAliveTagId(), cacheable.getId(), processId);
      // null if this alive does not have an Address -> is not in list of DataTags on DAQ
      if (processChange != null) {
        ConfigurationElementReport subReport = new ConfigurationElementReport(Action.CREATE, Entity.ALIVETAG, equipmentUpdate.getAliveTagId());
        processChange.setNestedSubReport(subReport);
        processChanges.add(processChange);
      }
    }
    return processChanges;
  }

  private List<ProcessChange> updateControlTagInformation(final ConfigurationElement element, T cacheable) {
    return new ArrayList<>();
  }

  protected abstract Long getProcessId(T cacheable);

  protected List<ProcessChange> cascadeRemoveDatatags(Collection<Long> dataTagIds, ConfigurationElementReport report) {
    List<ProcessChange> processChanges = new ArrayList<>();

    // TODO (Alex) Cascade delete through the control tags?

    for (Long dataTagId : dataTagIds) {
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.DATATAG, dataTagId);
      report.addSubReport(tagReport);

      dataTagConfigHandler.remove(dataTagId, tagReport).forEach(change -> {
        if (change.processActionRequired()) {
          change.setNestedSubReport(tagReport);
          processChanges.add(change);
        } else {
          report.addSubReport(tagReport);
        }
      });
    }
    return processChanges;
  }
}
