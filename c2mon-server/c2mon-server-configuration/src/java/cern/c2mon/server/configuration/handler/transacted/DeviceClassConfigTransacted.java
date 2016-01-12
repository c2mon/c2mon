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

import java.util.Properties;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * For internal use only. Allows use of Spring AOP for transaction management.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceClassConfigTransacted {

  /**
   * Create a DeviceClass object in the C2MON server within a transaction.
   *
   * @param element contains details of the DeviceClass to be created
   *
   * @return the change event to send to the DAQ (none in this case)
   * @throws IllegalAccessException
   */
  public ProcessChange doCreateDeviceClass(final ConfigurationElement element) throws IllegalAccessException;

  /**
   * Updates a DeviceClass object in the C2MON server within a transaction.
   *
   * @param id the ID of the DeviceClass object to update
   * @param properties details of the fields to modify
   *
   * @return the change event to send to the DAQ (none in this case)
   */
  public ProcessChange doUpdateDeviceClass(final Long id, final Properties properties);

  /**
   * Removes a DeviceClass object from the C2MON server within a transaction.
   *
   * @param id the ID of the DeviceClass to remove
   * @param elementReport the report on success
   *
   * @return the change event to send to the DAQ (none in this case)
   */
  public ProcessChange doRemoveDeviceClass(final Long id, final ConfigurationElementReport elementReport);
}
