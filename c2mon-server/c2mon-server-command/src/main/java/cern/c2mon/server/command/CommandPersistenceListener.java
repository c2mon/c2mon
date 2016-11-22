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
package cern.c2mon.server.command;

import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.common.command.CommandTag;

/**
 * Interface that must be implemented by the class dealing
 * with the logging of command tags.
 * <p>
 * <p>This class should then register with the {@link CommandExecutionManager}
 * as listener. Only one such listener can be registered (previous is overwritten).
 *
 * @author Mark Brightwell
 */
public interface CommandPersistenceListener {

  /**
   * Write the CommandTag and CommandReport object's info to the command
   * tag log. If the connectivity to the database is currently unavailable,
   * the object will be written to a fallback log I
   * <p>
   * t will also try to commit those commandTags already logged into the
   * fallback file (if any) back into the DB
   *
   * @param commandTag CommandTag object representing CommandTag data to be
   *                   logged, including the value
   * @param report     CommandReport object containing some CommandTag info to be
   *                   logged
   * @param <T>        the type of the command value
   */
  <T> void log(final CommandTag<T> commandTag, final CommandReport report);

}
