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
package cern.c2mon.shared.common.process;

import java.util.Map;

import cern.c2mon.shared.common.command.ISourceCommandTag;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTag;

/**
 * The equipment configuration interface covers all for the implementation
 * part of the DAQ unnecessary informations.
 *
 * @author Andreas Lang
 *
 */
public interface IEquipmentConfiguration {

    /**
     * This method gets the Equipment identifier
     *
     * @return The id of this equipment.
     */
    long getId();

    /**
     * This method gets the Equipment Unit's name.
     *
     * @return The name of this equipment.
     */
    String getName();

    /**
     * Gets the alive tag interval.
     * @return The alive tag interval.
     */
    long getAliveTagInterval();

    /**
     * Returns the id of the Equipment alive tag
     * (required for instance if the MessageHandler
     * needs to publish the alive and read it back
     * through a subscription mechanism).
     * @return the alive id
     */
    long getAliveTagId();

    /**
     * This method sets the Equipment unit's address
     *
     * @return The address of this equipment. (key value pairs separated with ';')
     */
    String getAddress();

    /**
     * Checks if this equipment configuration contains the source data tag
     * with the provided id.
     * @param tagID The tag id to check.
     * @return True if the source data tag is part of this equipment unit else false.
     */
    boolean hasSourceDataTag(final Long tagID);

    /**
     * Checks if this equipment configuration contains the command tag
     * with the provided id.
     * @param tagID The tag id to check.
     * @return True if the command tag is part of this equipment unit else false.
     */
    boolean hasSourceCommandTag(final Long tagID);

    /**
     * Retrieve all SubEquipment configurations.
     *
     * @return the map of SubEquipment configuration
     */
    Map<Long, SubEquipmentConfiguration> getSubEquipmentConfigurations();

    /**
     * Retrieve a single SubEquipment configuration.
     *
     * @param subEquipmentId the id of the SubEquipment configuration
     * @return the SubEquipment configuration
     */
    SubEquipmentConfiguration getSubEquipmentConfiguration(Long subEquipmentId);

    /**
     * Returns a copy of the map of data tags. Adding tags to this
     * map will not affect the rest of the application.
     * @return SourceCommandTag map (dataTagId -> SourceCommandTag)
     */
    Map<Long, ISourceDataTag> getSourceDataTags();

    /**
     * Returns a copy of the map of command tags. Adding tags to this
     * map will not affect the rest of the application.
     * @return SourceCommandTag map (commandTagId -> SourceCommandTag)
     */
    Map<Long, ISourceCommandTag> getSourceCommandTags();

    /**
     * @param dataTagId The id of the data tag to look for.
     * @return <code>true</code>, if a configuration exists for the given tag id.
     * @see #getSourceDataTag(Long)
     */
    boolean isSourceDataTagConfigured(final Long dataTagId);

    /**
     * Checks all {@link SourceDataTag} which are attached to the equipment for the given name and returns the id of the tag.
     * Retruns the id of the found datatag otherwise
     *
     * @param name The name of the tag to look for.
     * @return Retruns the id of the found tag otherwise throws an {@link IllegalArgumentException}.
     */
    Long getSourceDataTagIdByName(String name);

    /**
     * Gets the SourceDataTag with the specified id.
     * @param dataTagId The id of the data tag to look for.
     * @return The searched data tag or null if there is no data tag for the id.
     */
    ISourceDataTag getSourceDataTag(final Long dataTagId);

    /**
     * Gets the SourceCommandTag with the specified id.
     * @param commandTagId The id of the command tag to look for.
     * @return The searched data tag or null if there is no command tag for the id.
     */
    ISourceCommandTag getSourceCommandTag(final Long commandTagId);

}
