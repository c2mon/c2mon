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
package cern.c2mon.shared.client.configuration;

/**
 * Constants used for the server (re-)configuration functionality,.
 *
 * @author Mark Brightwell
 *
 */
public class ConfigConstants {

  /**
   * The configuration action to take.
   *
   * @author Mark Brightwell
   *
   */
  public enum Action { CREATE, REMOVE, UPDATE }

  /**
   * The server entity that is being reconfigured.
   * @author Mark Brightwell
   *
   */
  public enum Entity { DATATAG, CONTROLTAG, RULETAG, COMMANDTAG, PROCESS, EQUIPMENT, ALARM, SUBEQUIPMENT, DEVICECLASS, DEVICE, ALIVETAG, COMMFAULTTAG, STATETAG, MISSING }

  /**
   * The result of a reconfiguration action.
   *
   * @author Mark Brightwell
   *
   */
  public enum Status { OK(0), WARNING(1), FAILURE(3), RESTART(2);

    /** severity of failure; more severe status overrides less severe ones */
    int severity;

    /**
     * Constructor.
     * @param severity severity of failure
     */
    private Status(final int severity) {
      this.severity = severity;
    }

  }
}
