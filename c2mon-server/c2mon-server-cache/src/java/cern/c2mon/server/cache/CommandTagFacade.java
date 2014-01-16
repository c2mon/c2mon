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
package cern.c2mon.server.cache;

import cern.c2mon.server.cache.common.ConfigurableCacheFacade;
import cern.c2mon.shared.daq.command.CommandTag;
import cern.c2mon.shared.daq.command.SourceCommandTag;

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
