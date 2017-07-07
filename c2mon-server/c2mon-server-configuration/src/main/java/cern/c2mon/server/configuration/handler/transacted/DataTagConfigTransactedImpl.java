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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.naming.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.c2mon.server.configuration.handler.RuleTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.DataTagAdd;
import cern.c2mon.shared.daq.config.DataTagRemove;
import cern.c2mon.shared.daq.config.DataTagUpdate;

/**
 * Implementation of transacted methods.
 *
 * @author Mark Brightwell
 */
@Service
public class DataTagConfigTransactedImpl extends TagConfigTransactedImpl<DataTag> implements DataTagConfigTransacted {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DataTagConfigTransactedImpl.class);

  /**
   * Reference to the equipment facade.
   */
  private EquipmentFacade equipmentFacade;

  /**
   * Reference to the subequipment facade.
   */
  private SubEquipmentFacade subEquipmentFacade;

  /**
   * For recursive deletion of rules.
   */
  @Autowired
  private RuleTagConfigHandler ruleTagConfigHandler;

  /**
   * For recursive deletion of alarms.
   */
  @Autowired
  private AlarmConfigHandler alarmConfigHandler;

  @Autowired
  public DataTagConfigTransactedImpl(final DataTagFacade dataTagFacade,
                                     final DataTagLoaderDAO dataTagLoaderDAO,
                                     final DataTagCache dataTagCache,
                                     final EquipmentFacade equipmentFacade,
                                     final SubEquipmentFacade subEquipmentFacade,
                                     final TagLocationService tagLocationService,
                                     final GenericApplicationContext context) {
    super(dataTagLoaderDAO, dataTagFacade, dataTagCache, tagLocationService, context);
    this.equipmentFacade = equipmentFacade;
    this.subEquipmentFacade = subEquipmentFacade;
  }

  /**
   * Create the cache objects, puts it in the DB, loads it into the cache, and returns the
   * change event for sending to the DAQ.
   * @param element the server configuration element
   * @return the change event to send to the DAQ
   * @throws IllegalAccessException
   * @throws RuntimeException       if any error occurs during reconfiguration; DB transaction is rolled back and cache elements are removed
   */
  @Override
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange doCreateDataTag(final ConfigurationElement element) throws IllegalAccessException {

    // TIMS-1048: Making check before write lock in order to avoid a deadlock situation
    checkId(element.getEntityId());

    tagCache.acquireWriteLockOnKey(element.getEntityId());
    try {
      LOGGER.trace("Creating DataTag " + element.getEntityId());
      DataTag dataTag = commonTagFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
      try {
        configurableDAO.insert(dataTag);
      } catch (Exception e) {
        LOGGER.error("Exception caught while inserting a new DataTag into the DB - rolling back changes", e);
        throw new UnexpectedRollbackException("Unexpected exception while creating a DataTag: rolling back the change", e);
      }
      try {
        for (ConfigurationEventListener listener : configurationEventListeners) {
          listener.onConfigurationEvent(dataTag, Action.CREATE);
        }

        tagCache.putQuiet(dataTag);

        if (dataTag.getEquipmentId() != null) {
          DataTagAdd dataTagAdd = new DataTagAdd(element.getSequenceId(), dataTag.getEquipmentId(),
              ((DataTagFacade) commonTagFacade).generateSourceDataTag(dataTag));
          return new ProcessChange(equipmentFacade.getProcessIdForAbstractEquipment(dataTag.getEquipmentId()), dataTagAdd);
        }

        if (dataTag.getSubEquipmentId() != null) {
          // TIMS-951: Allow attachment of DataTags to SubEquipments
          DataTagAdd dataTagAdd = new DataTagAdd(element.getSequenceId(), subEquipmentFacade.getEquipmentIdForSubEquipment(dataTag.getSubEquipmentId()),
              ((DataTagFacade) commonTagFacade).generateSourceDataTag(dataTag));
          return new ProcessChange(subEquipmentFacade.getProcessIdForAbstractEquipment(dataTag.getSubEquipmentId()), dataTagAdd);
        }

        throw new IllegalArgumentException("No (sub)equipment id set in datatag (" + dataTag.getId() + ") configuration.");


      } catch (Exception ex) {
        LOGGER.error("Exception caught when attempting to create a DataTag - rolling back the DB transaction and undoing cache changes.");
        tagCache.remove(dataTag.getId());

        throw new UnexpectedRollbackException("Unexpected exception while creating a DataTag: rolling back the change", ex);
      }
    } finally {
      tagCache.releaseWriteLockOnKey(element.getEntityId());
    }

  }

  /**
   * Updates the DataTag configuration in the cache and
   * database.
   *
   * <p>Throws an exception if an attempt is made to move
   * the tag to another Equipment: in this case the tag
   * should be removed and recreated from the DB: for this
   * reason this call requires a NEW TRANSACTION, so the calling
   * method can reload the object from a rolled back DB.
   *
   * @param id         the id of the tag
   * @param properties the properties containing the changes
   * @return an change event if action is necessary by the DAQ; otherwise null
   */
  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW) //("cacheTransactionManager")
  public ProcessChange doUpdateDataTag(final Long id, final Properties properties) {
    LOGGER.trace("Updating DataTag " + id);
    // Warn if trying to change equipment it is attached to - not currently allowed
    if (properties.containsKey("equipmentId") || properties.containsKey("subEquipmentId")) {
      LOGGER.warn("Attempting to change the equipment/subequipment to which a tag is attached - this is not currently supported!");
      properties.remove("equipmentId");
      properties.remove("subEquipmentId");
    }
    Change dataTagUpdate = null;
    tagCache.acquireWriteLockOnKey(id); // needed to avoid overwrite of incoming value updates
    try {
      DataTag dataTagCopy = tagCache.getCopy(id);
      dataTagUpdate = commonTagFacade.updateConfig(dataTagCopy, properties);

      configurableDAO.updateConfig(dataTagCopy);

      for (ConfigurationEventListener listener : configurationEventListeners) {
        listener.onConfigurationEvent(dataTagCopy, Action.UPDATE);
      }

      tagCache.putQuiet(dataTagCopy);
      if (!dataTagUpdate.hasChanged()) {
        return new ProcessChange();
      } else {
        if (dataTagCopy.getEquipmentId() != null) {
          return new ProcessChange(equipmentFacade.getProcessIdForAbstractEquipment(dataTagCopy.getEquipmentId()), dataTagUpdate);
        }
        else {
          return new ProcessChange(subEquipmentFacade.getProcessIdForAbstractEquipment(dataTagCopy.getSubEquipmentId()), dataTagUpdate);
        }
      }
    } catch (CacheElementNotFoundException ex) { //tag not found
      throw ex;
    } catch (Exception ex) {
      LOGGER.error("Exception caught while updating a datatag. Rolling back transaction and removing from cache.", ex);
      throw new UnexpectedRollbackException("Unexpected exception caught while updating a DataTag configuration.", ex);
    } finally {
      tagCache.releaseWriteLockOnKey(id);
    }
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
  public ProcessChange doRemoveDataTag(final Long id, final ConfigurationElementReport elementReport) {
    ProcessChange processChange = new ProcessChange();
    try {
      DataTag tagCopy = tagCache.getCopy(id);
      Collection<Long> ruleIds = tagCopy.getCopyRuleIds();
      if (!ruleIds.isEmpty()) {
        LOGGER.trace("Removing Rules dependent on DataTag " + id);
        for (Long ruleId : new ArrayList<Long>(ruleIds)) {
          if (tagLocationService.isInTagCache(ruleId)) { //may already have been removed if a previous rule in the list was used in this rule! {
            ConfigurationElementReport newReport = new ConfigurationElementReport(Action.REMOVE, Entity.RULETAG, ruleId);
            elementReport.addSubReport(newReport);
            ruleTagConfigHandler.removeRuleTag(ruleId, newReport);
          }
        }
      }
      tagCache.acquireWriteLockOnKey(id);
      try {
        Collection<Long> alarmIds = tagCopy.getCopyAlarmIds();
        if (!alarmIds.isEmpty()) {
          LOGGER.trace("Removing Alarms dependent on DataTag " + id);
          for (Long alarmId : new ArrayList<>(alarmIds)) {
            ConfigurationElementReport alarmReport = new ConfigurationElementReport(Action.REMOVE, Entity.ALARM, alarmId);
            elementReport.addSubReport(alarmReport);
            alarmConfigHandler.removeAlarm(alarmId, alarmReport);
          }
        }

        for (ConfigurationEventListener listener : configurationEventListeners) {
          listener.onConfigurationEvent(tagCopy, Action.REMOVE);
        }

        configurableDAO.deleteItem(tagCopy.getId());
      } catch (Exception ex) {
        //commonTagFacade.setStatus(dataTag, Status.RECONFIGURATION_ERROR);
        elementReport.setFailure("Exception caught while removing datatag", ex);
        LOGGER.error("Exception caught while removing datatag with id " + id + "; rolling back DB transaction.", ex);
        throw new UnexpectedRollbackException("Exception caught while removing datatag.", ex);
      } finally {
        if (tagCache.isWriteLockedByCurrentThread(id)) {
          tagCache.releaseWriteLockOnKey(id);
        }
      }

      // if successful so far add remove event for DAQ layer
      DataTagRemove removeEvent = new DataTagRemove();
      removeEvent.setDataTagId(id);

      if (tagCopy.getEquipmentId() != null) {
        removeEvent.setEquipmentId(tagCopy.getEquipmentId());
        processChange = new ProcessChange(equipmentFacade.getProcessIdForAbstractEquipment(tagCopy.getEquipmentId()), removeEvent);
      }
      // TIMS-951: Allow attachment of DataTags to SubEquipments
      else if (tagCopy.getSubEquipmentId() != null) {
        removeEvent.setEquipmentId(subEquipmentFacade.getEquipmentIdForSubEquipment(tagCopy.getSubEquipmentId()));
        processChange = new ProcessChange(subEquipmentFacade.getProcessIdForAbstractEquipment(tagCopy.getSubEquipmentId()), removeEvent);
      }
      else {
        LOGGER.warn("doRemoveDataTag() - data tag #" + tagCopy.getId() + " is not attached to any Equipment or Sub-Equipment. This should normally never happen.");
      }
    } catch (CacheElementNotFoundException e) {
      LOGGER.warn("doRemoveDataTag() - Attempting to remove a non-existent DataTag - no action taken.");
      throw new CacheElementNotFoundException("Attempting to remove a non-existent DataTag - no action taken", e);
    }
    return processChange;
  }

}
