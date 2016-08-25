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

import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author Franz Ritter
 */
@Service
public class DataTagFactory extends EntityFactory<DataTag> {

  private EquipmentDAO equipmentDAO;
  private EquipmentCache equipmentCache;
  private SubEquipmentDAO subEquipmentDAO;
  private SubEquipmentCache subEquipmentCache;

  private DataTagCache dataTagCache;
  private TagFacadeGateway tagFacadeGateway;
  private SequenceDAO sequenceDAO;

  @Autowired
  public DataTagFactory(DataTagCache dataTagCache, TagFacadeGateway tagFacadeGateway, SequenceDAO sequenceDAO,
                        EquipmentDAO equipmentDAO, EquipmentCache equipmentCache, SubEquipmentDAO subEquipmentDAO,
                        SubEquipmentCache subEquipmentCache) {
    super(dataTagCache);
    this.dataTagCache = dataTagCache;
    this.tagFacadeGateway = tagFacadeGateway;
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
      if (dataTag.getSubEquipmentId() == null && dataTag.getSubEquipmentName() == null) {

        parentId = dataTag.getEquipmentId() != null ? dataTag.getEquipmentId() : equipmentDAO.getIdByName(dataTag
            .getEquipmentName());

        if (parentId == null || !equipmentCache.hasKey(parentId)) {
          throw new ConfigurationParseException("Error creating datatag #" + dataTag.getId() + ": " +
              "Specified parent equipment does not exist!");
        }

        dataTag.setEquipmentId(parentId);
      } else {
        throw new ConfigurationParseException("Error creating datatag #" + dataTag.getId() + ": " +
            "Cannot specify both equipment and subequipment as parent!");
      }
    } else if (dataTag.getSubEquipmentId() != null || dataTag.getSubEquipmentName() != null) {

      parentId = dataTag.getSubEquipmentId() != null ? dataTag.getSubEquipmentId() : subEquipmentDAO.getIdByName
          (dataTag.getSubEquipmentName());

      if (parentId == null || !subEquipmentCache.hasKey(parentId)) {
        throw new ConfigurationParseException("Error creating datatag #" + dataTag.getId() + ": " +
            "Specified parent subequipment does not exist!");
      }

      dataTag.setSubEquipmentId(parentId);
    } else {
      throw new ConfigurationParseException("Error creating datatag #" + dataTag.getId() + ": " +
          "No parent equipment or subequipment specified!");
    }

    return dataTag;
  }

  @Override
  Long createId(DataTag configurationEntity) {
    if (configurationEntity.getName() != null && dataTagCache.get(configurationEntity.getName()) != null) {
      throw new ConfigurationParseException("Error creating dataTag #" + configurationEntity.getName() + ": " +
          "Name already exists");
    } else {
      return configurationEntity.getId() != null ? configurationEntity.getId() : sequenceDAO.getNextTagId();
    }
  }

  @Override
  Long getId(DataTag entity) {
    Long id;

    if (entity.getId() != null) {
      id = entity.getId();
    } else {
      if (dataTagCache.get(entity.getName()) != null) {
        id = dataTagCache.get(entity.getName()).getId();
      } else {
        throw new ConfigurationParseException("Datatag " + entity.getName() + " does not exist!");
      }
    }
    return id;
  }

  @Override
  boolean hasEntity(Long id) {
    return id != null && tagFacadeGateway.isInTagCache(id);
  }

  @Override
  ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.DATATAG;
  }
}
