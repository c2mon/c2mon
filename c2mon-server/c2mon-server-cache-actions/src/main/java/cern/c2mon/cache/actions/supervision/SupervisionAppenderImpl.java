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
package cern.c2mon.cache.actions.supervision;

import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.SupervisionAppender;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Helper bean for adding the supervision status to
 * Tags.
 *
 * @author Mark Brightwell
 */
@Slf4j
@Named
@Singleton
public class SupervisionAppenderImpl implements SupervisionAppender {

  /**
   * Process and Equipment bean interfaces.
   */
  private ProcessService processService;
  private EquipmentService equipmentService;

  @Inject
  public SupervisionAppenderImpl(ProcessService processService, EquipmentService equipmentService) {
    this.processService = processService;
    this.equipmentService = equipmentService;
  }

  @Override
  public <T extends Tag> void addSupervisionQuality(final T tagCopy, final SupervisionEvent event) {
    TagQualityStatus tagSupervisionStatus = null;
    String message = event.getMessage(); //will be overwritten below if RUNNING

    switch (event.getEntity()) {

      case PROCESS:
        switch (event.getStatus()) {
          case DOWN:
            tagSupervisionStatus = TagQualityStatus.PROCESS_DOWN;
            break;
          case STOPPED:
            tagSupervisionStatus = TagQualityStatus.PROCESS_DOWN;
            break;
          case RUNNING:
            message = "DAQ process " + event.getEntityId() + " has recovered.";
            break;
          case RUNNING_LOCAL:
            message = "DAQ process " + event.getEntityId() + " has recovered and is running on a local configuration.";
            break;
          default:
            log.error("Unexpected supervision status: " + event.getEntity());
            break;
        }
        break;

      case EQUIPMENT:
        switch (event.getStatus()) {
          case DOWN:
            tagSupervisionStatus = TagQualityStatus.EQUIPMENT_DOWN;
            break;
          case STOPPED:
            tagSupervisionStatus = TagQualityStatus.EQUIPMENT_DOWN;
            break;
          case RUNNING:
            message = "Equipment " + event.getEntityId() + " has recovered.";
            break;
          default:
            log.error("Unexpected supervision status: " + event.getEntity());
            break;
        }
        break;

      case SUBEQUIPMENT:
        switch (event.getStatus()) {
          case DOWN:
            tagSupervisionStatus = TagQualityStatus.SUBEQUIPMENT_DOWN;
            break;
          case STOPPED:
            tagSupervisionStatus = TagQualityStatus.SUBEQUIPMENT_DOWN;
            break;
          case RUNNING:
            message = "SubEquipment " + event.getEntityId() + " has recovered.";
            break;
          default:
            log.error("Unexpected supervision status: " + event.getEntity());
            break;
        }
        break;

      default:
        break;
    }

    if (tagSupervisionStatus != null) {
      tagCopy.getDataTagQuality().addInvalidStatus(tagSupervisionStatus, message);
    }
  }
}
