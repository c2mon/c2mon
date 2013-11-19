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
package cern.c2mon.shared.daq.command;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.command.AuthorizationDetails;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;

/**
 * Interface of the CommandTagCacheObject. Is currently used
 * in SourceCommandTag in c2mon-shared-daq (a SourceCommandTag
 * XML can be generated from a CommandTag object), although this
 * code is actually only used on the server (TODO move it to a server
 * class - CommandTagCacheObject for instance!).
 * 
 * @param <T> the type of value this command can be set to
 * 
 * @author Mark Brightwell
 *
 */
public interface CommandTag<T> extends Cacheable {

  String getName();

  int getSourceTimeout();

  int getSourceRetries();

  HardwareAddress getHardwareAddress();

  String toConfigXML();

  Long getEquipmentId();

  short getMode();

  int getExecTimeout();

  Long getProcessId();

  String getDataType();

  int getClientTimeout();

  String getDescription();

  Comparable<T> getMinimum();

  Comparable<T> getMaximum();

  AuthorizationDetails getAuthorizationDetails();

  /**
   * Returns the details of a given command execution.  
   * @return details with value etc
   */
  CommandExecutionDetails<T> getCommandExecutionDetails();

  /**
   * Sets details for a given execution of the command. Use
   * on a copy of the cache object when executing a command.
   * @param commandExecutionDetails the details to set
   */
  void setCommandExecutionDetails(CommandExecutionDetails<T> commandExecutionDetails);

}
