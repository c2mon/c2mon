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

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.*;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;

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
  public enum Entity {
    ALARM(Alarm.class),
    ALIVETAG(AliveTag.class),
    COMMANDTAG(CommandTag.class),
    COMMFAULTTAG(CommFaultTag.class),
    CONTROLTAG(ControlTag.class),
    DATATAG(DataTag.class),
    DEVICE(null),
    DEVICECLASS(null),
    EQUIPMENT(Equipment.class),
    MISSING(null),
    PROCESS(Process.class),
    RULETAG(RuleTag.class),
    STATETAG(StatusTag.class),
    SUBEQUIPMENT(SubEquipment.class);

    Class<? extends ConfigurationEntity> classRef;

    Entity(Class<? extends ConfigurationEntity> classRef) {
      this.classRef = classRef;
    }

    public Class<? extends ConfigurationEntity> getClassRef() {
      return classRef;
    }
  }

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
