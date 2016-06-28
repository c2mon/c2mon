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
package cern.c2mon.shared.client.configuration.api.util;


import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface which defines objects which are used to create a
 * {@link cern.c2mon.shared.client.configuration.ConfigurationElement}.
 * Although the structure of the ConfigurationObjects are well defined each
 * object needs to provide the methods given in this interface.
 * <p/>
 * A ConfigurationObjects is a POJO which holds information for creating the
 * {@link cern.c2mon.shared.client.configuration.ConfigurationElement}.
 * <p/>
 * If the id of the object is known by the Server the
 * {@link cern.c2mon.server.configuration.parser.tasks.SequenceTaskFactory}
 * tries to create a UPDATE
 * {@link cern.c2mon.shared.client.configuration.ConfigurationElement}.
 *
 * If the id of the object is not known by the Server the
 * {@link SequenceTaskFactory} tries to create a CREATE
 * {@link cern.c2mon.shared.client.configuration.ConfigurationElement}.
 * If field deleted is set to true the  {@link SequenceTaskFactory} tries to
 * create a DELETE {@link cern.c2mon.shared.client.configuration.ConfigurationElement}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface ConfigurationEntity {

  /**
   * returns the id value of the Id field of the ConfigurationObject
   *
   * @return id value
   */
  Long getId();

  /**
   * Set the id to the configuration object
   * @param defaultId the id to set
   */
  void setId(Long defaultId);

  /**
   * determine if the configuration objects serves as create.
   *
   * @return true if its a create.
   */
  boolean isCreated();

  /**
   * determine if the configuration objects serves as update.
   *
   * @return true if its a update.
   */
  boolean isUpdated();

  /**
   * determine if the instance of the ConfigurationObject holds the information to create a DELETE {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
   *
   * @return boolean value if this object is a delete object
   */
  boolean isDeleted();

}
