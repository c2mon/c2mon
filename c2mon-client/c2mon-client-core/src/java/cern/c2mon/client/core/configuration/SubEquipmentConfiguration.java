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
package cern.c2mon.client.core.configuration;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;

import java.util.List;

/**
 * @author Franz Ritter
 */
public interface SubEquipmentConfiguration {

  /**
   * Creates a new 'SubEquipment' on the server with the given name and handler class.
   * After a successful creation a DAQ which corresponds to the SubEquipment can be started.
   * <br/>
   * The SubEquipment is created with default parameters including the standard ControlTags.
   *
   * @param equipmentId  The id of the overlying Equipment.
   * @param name         The name of the Equipment to be created.
   * @param handlerClass The full class path of the Equipment which needs to be created.
   * @return A {@link ConfigurationReport} containing all details of the SubEquipment configuration,
   * including if it was successful or not.
   * @see SubEquipmentConfiguration#createSubEquipment(Long, SubEquipment)
   * @see SubEquipmentConfiguration#createSubEquipment(String, String, String)
   * @see SubEquipmentConfiguration#createSubEquipment(String, SubEquipment)
   */
  ConfigurationReport createSubEquipment(Long equipmentId, String name, String handlerClass);

  /**
   * Creates a new 'SubEquipment' on the server with the given name and handler class specified in the {@link SubEquipment} object.
   * After a successful creation a DAQ which corresponds to the SubEquipment can be started.
   * <br/>
   * Next to the specified parameters the SubEquipment is created with default parameters including the standard ControlTags.
   * <p>
   * Note: You have to use {@link SubEquipment#create(String, String)} to instantiate the 'subEquipment' parameter of this method.
   * </p>
   *
   * @param equipmentId  The id of the overlying Equipment.
   * @param subEquipment The {@link Equipment} configuration for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the SubEquipment configuration,
   * including if it was successful or not.
   * @see SubEquipmentConfiguration#createSubEquipment(Long, String, String)
   * @see SubEquipmentConfiguration#createSubEquipment(String, String, String)
   * @see SubEquipmentConfiguration#createSubEquipment(String, SubEquipment)
   */
  ConfigurationReport createSubEquipment(Long equipmentId, SubEquipment subEquipment);

  /**
   * Creates multiple 'SubEquipment' on the server with the given name and handler class specified in the {@link SubEquipment} objects.
   * After a successful creation a DAQ which corresponds to the SubEquipments can be started.
   * <br/>
   * Next to the specified parameters the SubEquipments are created with default parameters including the standard ControlTags.
   * <p>
   * Note: You have to use {@link SubEquipment#create(String, String)} to instantiate the 'subEquipment' parameter of this method.
   * </p>
   *
   * @param equipmentId  The id of the overlying Equipment.
   * @param subEquipment The list of {@link Equipment} configurations for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the SubEquipment configuration,
   * including if it was successful or not.
   * @see SubEquipmentConfiguration#createSubEquipments(String, List)
   */
  ConfigurationReport createSubEquipments(Long equipmentId, List<SubEquipment> equipments);

  /**
   * Creates a new 'SubEquipment' on the server with the given name and handler class.
   * After a successful creation a DAQ which corresponds to the SubEquipment can be started.
   * <br/>
   * The SubEquipment is created with default parameters including the standard ControlTags.
   *
   * @param equipmentName    The name of the overlying Equipment.
   * @param subEquipmentName The name of the SubEquipment to be created.
   * @param handlerClass     The full class path of the SubEquipment which needs to be created.
   * @return A {@link ConfigurationReport} containing all details of the Process configuration,
   * including if it was successful or not.
   * @see SubEquipmentConfiguration#createSubEquipment(Long, String, String)
   * @see SubEquipmentConfiguration#createSubEquipment(Long, SubEquipment)
   * @see SubEquipmentConfiguration#createSubEquipment(String, SubEquipment)
   */
  ConfigurationReport createSubEquipment(String equipmentName, String subEquipmentName, String handlerClass);

  /**
   * Creates a new 'SubEquipment' on the server with the given name and handler class specified in the {@link SubEquipment} object.
   * After a successful creation a DAQ which corresponds to the SubEquipment can be started.
   * <br/>
   * Next to the specified parameters the SubEquipment is created with default parameters including the standard ControlTags.
   * <p>
   * Note: You have to use {@link SubEquipment#create(String, String)} to instantiate the 'equipment' parameter of this method.
   * </p>
   *
   * @param equipmentName The name of the overlying Process.
   * @param subEquipment  The {@link SubEquipment} configuration for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the Process configuration,
   * including if it was successful or not.
   * @see SubEquipmentConfiguration#createSubEquipment(Long, String, String)
   * @see SubEquipmentConfiguration#createSubEquipment(Long, SubEquipment)
   * @see SubEquipmentConfiguration#createSubEquipment(String, String, String)
   */
  ConfigurationReport createSubEquipment(String equipmentName, SubEquipment subEquipment);

  /**
   * Creates multiple 'SubEquipment' on the server with the given name and handler class specified in the {@link SubEquipment} objects.
   * After a successful creation a DAQ which corresponds to the SubEquipments can be started.
   * <br/>
   * Next to the specified parameters the SubEquipments are created with default parameters including the standard ControlTags.
   * <p>
   * Note: You have to use {@link SubEquipment#create(String, String)} to instantiate the 'subEquipment' parameter of this method.
   * </p>
   *
   * @param equipmentName The name of the overlying Process.
   * @param subEquipment  The list of {@link Equipment} configurations for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the SubEquipment configuration,
   * including if it was successful or not.
   * @see SubEquipmentConfiguration#createSubEquipments(Long, List)
   */
  ConfigurationReport createSubEquipments(String equipmentName, List<SubEquipment> equipments);

  /**
   * Updates a existing 'SubEquipment' with the given parameters in the {@link SubEquipment} object.
   * <br/>
   * <p>
   * Note: You have to use {@link SubEquipment#update(Long)} or {@link Process#update(String)} to instantiate the 'equipment' parameter of this method.
   * </p>
   *
   * @param subEquipment The {@link SubEquipment} configuration for the 'update'.
   * @return A {@link ConfigurationReport} containing all details of the SubEquipment configuration,
   * including if it was successful or not.
   */
  ConfigurationReport updateSubEquipment(SubEquipment subEquipment);

  /**
   * Updates a list of existing 'SubEquipments' with the given parameters in the {@link SubEquipment} objects.
   * <br/>
   * <p>
   * Note: You have to use {@link SubEquipment#update(Long)} or {@link Process#update(String)} to instantiate the 'equipment' parameter of this method.
   * </p>
   *
   * @param subEquipments The list of {@link SubEquipment} configurations for the 'update'.
   * @return A {@link ConfigurationReport} containing all details of the SubEquipment configuration,
   * including if it was successful or not.
   */
  ConfigurationReport updateSubEquipments(List<SubEquipment> subEquipments);

  /**
   * Removes a existing 'SubEquipment' with the given id.
   *
   * @param id The id of the SubEquipment which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the SubEquipment configuration,
   * including if it was successful or not.
   */
  ConfigurationReport removeSubEquipment(Long id);

  /**
   * Removes a list of existing 'SubEquipments' with the given ids.
   *
   * @param ids The list of ids of the SubEquipments which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the SubEquipment configuration,
   * including if it was successful or not.
   */
  ConfigurationReport removeSubEquipments(List<Long> ids);

  /**
   * Removes a existing 'SubEquipment' with the given name.
   *
   * @param id The name of the SubEquipment which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the SubEquipment configuration,
   * including if it was successful or not.
   */
  ConfigurationReport removeSubEquipment(String name);

  /**
   * Removes a list of existing 'SubEquipment' with the given names.
   *
   * @param ids The list of names of the SubEquipment which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the SubEquipment configuration,
   * including if it was successful or not.
   */
  ConfigurationReport removeSubEquipmentsByName(List<String> names);


}
