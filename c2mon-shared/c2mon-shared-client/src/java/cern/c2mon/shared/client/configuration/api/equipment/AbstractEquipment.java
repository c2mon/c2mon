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
package cern.c2mon.shared.client.configuration.api.equipment;

import cern.c2mon.shared.client.configuration.api.tag.*;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import lombok.Data;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;


/**
 * Configuration object for a Equipment.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to an Equipment.
 * <p/>
 * For further information how to use instances of this for server configurations read <a
 * href="http://c2mon.web.cern.ch/c2mon/docs/#_offline_configuration_via_c2mon_database_test_purpose_only">this</a> documentation.
 * <p/>
 *
 * @author Franz Ritter
 */
@Data
public abstract class AbstractEquipment implements ConfigurationEntity {

  @IgnoreProperty
  private boolean updated = false;

  @IgnoreProperty
  private boolean created = false;

  /**
   * Determine if the instance of this class defines a DELETE command
   */
  @IgnoreProperty
  private boolean deleted;

  /**
   * Unique identifier of the equipment.
   */
  @IgnoreProperty
  private Long id;

  /**
   * Unique name of the equipment.
   */
  private String name;

  /**
   * Interval in milliseconds at which the alive tag is expected to change.
   */
  @DefaultValue("60000")
  private Integer aliveInterval;

  /**
   * Free-text description of the equipment.
   */
  @DefaultValue("<no description provided>")
  private String description;

  /**
   * Address parameters used by the handler class to connect to the equipment.
   */
  private String address;

  /**
   * The StatusTag is one of three mandatory control tags which are needed to create an Equipment.
   * For further information of the tag read the documentation of {@link StatusTag}.
   */
  @IgnoreProperty
  private StatusTag statusTag;

  /**
   * The AliveTag is one of three mandatory control tags which are needed to create an Equipment.
   * For further information of the tag read the documentation of {@link AliveTag}.
   */
  @IgnoreProperty
  private AliveTag aliveTag;

  /**
   * The CommFaultTag is one of three mandatory control tags which are needed to create an Equipment.
   * For further information of the tag read the documentation of {@link CommFaultTag}.
   */
  @IgnoreProperty
  private CommFaultTag commFaultTag;

  /**
   * The list of CommandTags which are attached to the Equipment.
   * Each CommandTag holds a unique configuration information.
   */
  @IgnoreProperty
  @Singular
  private List<CommandTag> commandTags = new ArrayList<>();

  public AbstractEquipment() {
  }

}
