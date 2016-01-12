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

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.shared.client.configuration.api.tag.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

/**
 * Configuration object for a SubEquipment.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a SubEquipment.
 * <p/>
 * For further information how to use instances of this for server configurations read <a
 * href="http://c2mon.web.cern.ch/c2mon/docs/#_offline_configuration_via_c2mon_database_test_purpose_only">this</a> documentation.
 * <p/>
 *
 * @author Franz Ritter
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SubEquipment extends Equipment {


  /**
   * Constructor for building a SubEquipment with all fields.
   * To build a SubEquipment with arbitrary fields use the builder pattern.
   *
   * @param id            Unique id of the SubEquipment.
   * @param name          Unique name the SubEquipment.
   * @param description   Describes the propose of the SubEquipment.
   * @param deleted       Determine if this object apply as deletion.
   * @param aliveInterval Defines the configuration of the alive interval of the AliveTag which is attached to this Process.
   * @param equipments    List of SubEquipment configuration objects for this tag. If the argument is null the field will be an empty List as default.
   * @param statusTag     Mandatory configuration object for an StatusTag which is attached to this process. If the configuration is not a delete this
   *                      field have to be null.
   * @param aliveTag      Mandatory configuration object for an AliveTag which is attached to this process. If the configuration is not a delete this field
   *                      have to be null.
   * @param handlerClass  Full path-class name of the handler class of the this SubEquipment.
   * @param address       Address parameter used by the handler class to connect to the SubEquipment.
   * @param commFaultTag  Mandatory configuration object for an CommFaultTag which is attached to this process. If the configuration is not a delete this
   *                      field have to be null.
   * @param dataTags      Optional list of DataTag configurations which are attached to this SubEquipment. If the argument is null the field will be an empty
   *                      List as default.
   * @param commandTags   Optional list of CommandTag configurations which are attached to this SubEquipment. If the argument is null the field will be an empty
   *                      List as default.
   */
  @Builder(builderMethodName = "builderSubEquipment")
  public SubEquipment(boolean deleted, Long id, String name, Integer aliveInterval, String description,
                      String handlerClass, StatusTag statusTag, AliveTag aliveTag, CommFaultTag commFaultTag, @Singular List<DataTag<Number>> dataTags) {
    super(deleted, id, name, aliveInterval, description, handlerClass, null, new ArrayList<SubEquipment>(), statusTag, commFaultTag, aliveTag, dataTags, null);
  }

  public SubEquipment() {
  }
}
