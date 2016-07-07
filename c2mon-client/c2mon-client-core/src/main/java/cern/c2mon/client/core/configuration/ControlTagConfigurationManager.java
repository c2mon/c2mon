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
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;

import java.util.List;

/**
 * The ControlTagConfigurationManager allows to apply update configurations for
 * ControlTags (such as {@link AliveTag}, {@link CommFaultTag} or
 * {@link StatusTag}).
 *
 * @author Franz Ritter
 */
public interface ControlTagConfigurationManager {

  /**
   * Updates a existing {@link AliveTag} with the given parameters set in the
   * {@link AliveTag} object.
   * <p>
   * Note: You have to use one of the following methods to instantiate the
   * 'tag' parameter of this method.
   * <p>
   * {@link AliveTag#update(Long)}, {@link AliveTag#update(String)}
   *
   * @param tag The {@link AliveTag} configuration for the 'update'.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport updateAliveTag(AliveTag tag);

  /**
   * Updates multiple existing {@link AliveTag} with the given parameters set in
   * the {@link AliveTag} objects.
   * <p>
   * Note: You have to use one of the following methods to instantiate the
   * 'tag' parameter of this method.
   * <p>
   * {@link AliveTag#update(Long)}, {@link AliveTag#update(String)}
   *
   * @param tags The list of {@link AliveTag} configurations for the 'updates'.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   */
  ConfigurationReport updateAliveTags(List<AliveTag> tags);

  /**
   * Updates a existing {@link CommFaultTag} with the given parameters set in the
   * {@link CommFaultTag} object.
   * <p>
   * Note: You have to use one of the following methods to instantiate the
   * 'tag' parameter of this method.
   * <p>
   * {@link CommFaultTag#update(Long)}, {@link CommFaultTag#update(String)}
   *
   * @param tag The {@link CommFaultTag} configuration for the 'update'.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport updateCommFaultTag(CommFaultTag tag);

  /**
   * Updates multiple existing {@link CommFaultTag} with the given parameters
   * set in the {@link CommFaultTag} objects.
   * <p>
   * Note: You have to use one of the following methods to instantiate the
   * 'tag' parameter of this method.
   * <p>
   * {@link CommFaultTag#update(Long)}, {@link CommFaultTag#update(String)}
   *
   * @param tags The list of {@link CommFaultTag} configurations for the 'updates'.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   */
  ConfigurationReport updateCommFaultTags(List<CommFaultTag> tags);

  /**
   * Updates a existing {@link StatusTag} with the given parameters set in the
   * {@link StatusTag} object.
   * <p>
   * Note: You have to use one of the following methods to instantiate the
   * 'tag' parameter of this method.
   * <p>
   * {@link StatusTag#update(Long)}, {@link StatusTag#update(String)}
   *
   * @param tag The {@link StatusTag} configuration for the 'update'.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport updateStatusTag(StatusTag tag);

  /**
   * Updates multiple existing {@link StatusTag} with the given parameters
   * set in the {@link StatusTag} objects.
   * <p>
   * Note: You have to use one of the following methods to instantiate the
   * 'tag' parameter of this method.
   * <p>
   * {@link StatusTag#update(Long)}, {@link StatusTag#update(String)}
   *
   * @param tags The list of {@link StatusTag} configurations for the 'updates'.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   */
  ConfigurationReport updateStatusTags(List<StatusTag> tags);
}
