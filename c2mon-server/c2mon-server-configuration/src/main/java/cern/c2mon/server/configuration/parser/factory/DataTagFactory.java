/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.server.configuration.parser.factory;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.tag.UnifiedTagCacheFacade;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static cern.c2mon.cache.config.ClientQueryProvider.queryByClientInput;

/**
 * @author Franz Ritter
 */
@Service
class DataTagFactory extends EntityFactory<DataTag> {

  public static final String ERROR_CREATING_DATA_TAG = "Error creating data tag #";
  private EquipmentDAO equipmentDAO;
  private C2monCache<Equipment> equipmentCache;
  private SubEquipmentDAO subEquipmentDAO;
  private C2monCache<SubEquipment> subEquipmentCache;

  private C2monCache<cern.c2mon.server.common.datatag.DataTag> dataTagCache;
  private UnifiedTagCacheFacade unifiedTagCacheFacade;
  private SequenceDAO sequenceDAO;

  @Autowired
  public DataTagFactory(C2monCache<cern.c2mon.server.common.datatag.DataTag> dataTagCache, UnifiedTagCacheFacade unifiedTagCacheFacade, SequenceDAO sequenceDAO,
                        EquipmentDAO equipmentDAO, C2monCache<Equipment> equipmentCache, SubEquipmentDAO subEquipmentDAO,
                        C2monCache<SubEquipment> subEquipmentCache) {
    super(dataTagCache);
    this.dataTagCache = dataTagCache;
    this.unifiedTagCacheFacade = unifiedTagCacheFacade;
    this.sequenceDAO = sequenceDAO;
    this.equipmentDAO = equipmentDAO;
    this.equipmentCache = equipmentCache;
    this.subEquipmentDAO = subEquipmentDAO;
    this.subEquipmentCache = subEquipmentCache;
  }

  @Override
  public List<ConfigurationElement> createInstance(DataTag dataTag) {
    dataTag = getParentId(dataTag);

    return Collections.singletonList(doCreateInstance(dataTag));
  }

  private DataTag getParentId(DataTag dataTag) {
    Long parentId;

    if (dataTag.getEquipmentId() != null || dataTag.getEquipmentName() != null) {
      throwIfHasSubEquipmentId(dataTag);

      parentId = getOrFindByName(dataTag.getEquipmentId(), () -> equipmentDAO.getIdByName(dataTag
        .getEquipmentName()));

      if (parentId == null || !equipmentCache.containsKey(parentId)) {
        throw new ConfigurationParseException(ERROR_CREATING_DATA_TAG + dataTag.getId() + ": " +
            "Specified parent equipment does not exist!");
      }

      dataTag.setEquipmentId(parentId);
    } else if (dataTag.getSubEquipmentId() != null || dataTag.getSubEquipmentName() != null) {

      parentId = getOrFindByName(dataTag.getSubEquipmentId(), () ->
        subEquipmentDAO.getIdByName(dataTag.getSubEquipmentName()));

      if (parentId == null || !subEquipmentCache.containsKey(parentId)) {
        throw new ConfigurationParseException(ERROR_CREATING_DATA_TAG + dataTag.getId() + ": " +
            "Specified parent sub equipment does not exist!");
      }

      dataTag.setSubEquipmentId(parentId);
    } else {
      throw new ConfigurationParseException(ERROR_CREATING_DATA_TAG + dataTag.getId() + ": " +
          "No parent equipment or sub equipment specified!");
    }

    return dataTag;
  }

  private void throwIfHasSubEquipmentId(DataTag dataTag) {
    if (dataTag.getSubEquipmentId() != null || dataTag.getSubEquipmentName() != null) {
      throw new ConfigurationParseException(ERROR_CREATING_DATA_TAG + dataTag.getId() + ": " +
          "Cannot specify both equipment and sub equipment as parent!");
    }
  }

  private Long getOrFindByName(Long equipmentId, Supplier<Long> findIdByName) {
    return equipmentId != null ? equipmentId : findIdByName.get();
  }

  @Override
  Long createId(DataTag configurationEntity) {
    if (configurationEntity.getName() != null
      && !queryByClientInput(dataTagCache, Tag::getName, configurationEntity.getName()).isEmpty()) {
        throw new ConfigurationParseException("Error creating data tag " + configurationEntity.getName() + ": " +
            "Name already exists!");
    }

    return getOrFindByName(configurationEntity.getId(), () -> sequenceDAO.getNextTagId());
  }

  @Override
  Long getId(DataTag entity) {
    return entity.getId() != null
      ? entity.getId()
      : queryByClientInput(dataTagCache, Tag::getName, entity.getName())
        .stream().findAny()
        .orElseThrow(() -> new ConfigurationParseException("Data tag " + entity.getName() + " does not exist!"))
        .getId();
  }

  @Override
  boolean hasEntity(Long id) {
    return id != null && unifiedTagCacheFacade.containsKey(id);
  }

  @Override
  public ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.DATATAG;
  }
}
