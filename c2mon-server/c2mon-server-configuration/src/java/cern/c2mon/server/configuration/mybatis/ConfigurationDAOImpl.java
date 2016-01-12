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

import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.configuration.dao.ConfigurationDAO;
import cern.c2mon.shared.client.configuration.ConfigurationDescriptor;
import cern.c2mon.shared.client.configuration.ConfigurationElement;

/**
 * Mybatis implementation of the ConfigurationDAO for the server
 * configuration module.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class ConfigurationDAOImpl implements ConfigurationDAO {

  /**
   * The Mybatis mapper.
   */
  @Autowired
  private ConfigurationMapper configurationMapper;
  
  @Override
  public String getConfigName(int configId) {
    return configurationMapper.getConfigName(configId);
  }
  
  @Override
  public ConfigurationDescriptor getConfiguration(int configId) {
    //TODO implement this method for retrieving a configuration from the DB
    throw new UnsupportedOperationException("Method not implemented yet.");
  }

  @Override
  public List<ConfigurationDescriptor> getConfigurations() {
    // TODO implement this method for retrieving a list of configurations from the DB
    throw new UnsupportedOperationException("Method not implemented yet.");
  }

  @Override
  public List<ConfigurationElement> getConfigElements(int configId) {
    try {
      return configurationMapper.getConfigElements(configId);
    } catch (PersistenceException e) {
      if (e.getCause() instanceof NullPointerException) {
        throw new PersistenceException("Nullpointer exception caught while loading configuration elements: "
            + "please check none of the element key-value pairs you are trying to load are null!", e);
      } else {
        throw e;
      }        
    }    
  }

  @Override
  public void saveStatusInfo(ConfigurationElement configurationElement) {
    configurationMapper.saveStatusInfo(configurationElement);
  }

  @Override
  public void markAsApplied(int id) {
    configurationMapper.markAsApplied(id);
  }

}
