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
package cern.c2mon.server.configuration.mybatis;

import java.util.List;

import cern.c2mon.server.configuration.dao.ConfigurationDAO;
import cern.c2mon.shared.client.configuration.ConfigurationElement;

/**
 * Mybatis mapper for accessing configuration tables.
 * 
 * @author Mark Brightwell
 *
 */
public interface ConfigurationMapper {

  /**
   * Get the name of a configuration with a given id.
   * @param configId the id of the configuration
   * @return the name of the configuration
   */
  String getConfigName(int configId);
  
  /**
   * Returns the configuration elements store in the database
   * for a given configuration id.
   * @param configId the unique configuration id
   * @return a list of configuration elements containing all the details
   * needed to apply them
   */
  List<ConfigurationElement> getConfigElements(int configId);
  
  /**
   * See DAO description {@link ConfigurationDAO}. 
   * @param configurationElement element to save info for 
   */
  void saveStatusInfo(ConfigurationElement configurationElement);
  
  /**
   * See DAO description {@link ConfigurationDAO}
   * @param id the id of the configuration
   */
  void markAsApplied(int id);
  
}
