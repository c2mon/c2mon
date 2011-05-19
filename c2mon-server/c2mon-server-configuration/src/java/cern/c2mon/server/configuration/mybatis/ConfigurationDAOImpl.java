/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.configuration.mybatis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.configuration.dao.ConfigurationDAO;
import cern.tim.shared.client.configuration.ConfigurationDescriptor;
import cern.tim.shared.client.configuration.ConfigurationElement;

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
    return null;
  }

  @Override
  public List<ConfigurationDescriptor> getConfigurations() {
    // TODO implement this method for retrieving a list of configurations from the DB
    return null;
  }

  @Override
  public List<ConfigurationElement> getConfigElements(int configId) {
    return configurationMapper.getConfigElements(configId);
  }

  

}
