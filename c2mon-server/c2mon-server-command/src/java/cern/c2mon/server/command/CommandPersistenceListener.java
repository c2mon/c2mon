/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.command;

import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.daq.command.CommandTag;

/**
 * Interface that must be implemented by the class dealing
 * with the logging of command tags.
 *
 * <p>This class should then register with the {@link CommandExecutionManager}
 * as listener. Only one such listener can be registered (previous is overwritten).
 * 
 * @author Mark Brightwell
 *
 */
public interface CommandPersistenceListener {
  
  /**
   * Copied from STL bean:
   * 
   * <p>Write the CommandTag and CommandReport object's info to the command
   * tag log. If the connectivity to the database is currently unavailable,
   * the object will be written to a fallback log It will also try to commit
   * those commandTags already logged into the fallback file (if any) back
   * into the DB
   * 
   * @param commandTag CommandTag object representing CommandTag data to be
   *                 logged, including the value
   * @param report CommandReport object containing some CommandTag info to be
   *                 logged
   * @param <T> the type of the command value
   */
  <T> void log(final CommandTag<T> commandTag, final CommandReport report);
  
}
