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
package cern.c2mon.server.configuration.parser;

import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;

import java.util.List;

/**
 * This class holds all information about a Configuration object to translate
 * this object in List of ConfigurationElement.
 *
 * @author Franz Ritter
 */
public interface ConfigurationParser {
  /**
   * Parses the {@link Configuration} and returns a List of {@link ConfigurationElement}s.
   *
   * @param configuration Object which holds all information for creating the list of {@link ConfigurationElement}
   * @return List of ConfigurationElement, which are used by the {@link cern.c2mon.server.configuration.ConfigurationLoader}
   */
  List<ConfigurationElement> parse(Configuration configuration);
}
