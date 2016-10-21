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
package cern.c2mon.shared.client.configuration;

import java.util.Properties;

import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;
import lombok.Data;

/**
 * Configuration element stored in the TIMCONFIGELT and TIMCONFIGVAL database tables.
 * Also has an attached report.
 *
 * <p>Notice that ConfigurationElements are only created for the
 * configuration events specified in the database. Other resulting
 * changes are not instantiated as separate elements.
 *
 * @author Mark Brightwell
 */
@Data
public class ConfigurationElement {

  /**
   * The element id.
   */
  private Long sequenceId;

  /**
   * The configuration id.
   */
  private Long configId;

  /**
   * The type of Action of this element.
   */
  private Action action;

  /**
   * The type of element that is being reconfigured.
   */
  private Entity entity;

  /**
   * The id of the entity that needs configuring.
   */
  private Long entityId;

  /**
   * Status of the element, set once it has been executed.
   */
  private Status status = Status.OK;

  /**
   * Status on DAQ layer: can also be RESTART.
   */
  private Status daqStatus = Status.OK;

  /**
   * Stores the properties for CREATE and UPDATE elements
   * (stays null for REMOVE elements).
   */
  private Properties elementProperties;

  /**
   * Builds a description what the current configuration is doing.
   *
   * @return A description what the current configuration is doing
   */
  public String buildDescription() {
    return action.name().toLowerCase().substring(0, action.name().length() - 1)
        + "ing " + entity.name().toLowerCase() + " " + entityId;
  }

}
