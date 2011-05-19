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

import cern.tim.shared.client.configuration.ConfigurationElement;

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
  
}
