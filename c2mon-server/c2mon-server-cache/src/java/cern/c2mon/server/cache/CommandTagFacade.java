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
package cern.c2mon.server.cache;

import cern.c2mon.server.cache.common.ConfigurableCacheFacade;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.common.command.SourceCommandTag;

/**
 * Facade bean to interact with CommandTag cache objects residing
 * in the CommandTagCache.
 * 
 * @author Mark Brightwell
 *
 */
public interface CommandTagFacade extends ConfigurableCacheFacade<CommandTag> {

  /**
   * Generates the XML needed to send to the DAQ at start-up.
   * @param id the command id
   * @return the config XML
   */
  String getConfigXML(Long id);

  /**
   * Generates the corresponding {@link SourceCommandTag} used by
   * the DAQ process.
   * @param commandTag the command for which the {@link SourceCommandTag} should
   *  be generated
   * @return the source object
   */
  SourceCommandTag generateSourceCommandTag(CommandTag commandTag);

}
