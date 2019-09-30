/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.cache.oscillation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Emiliano Piselli
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.alarm.oscillation")
public class OscillationProperties {

  /** The time range in seconds for the {@link #oscNumbers} threshold */
  private int timeRange = 60;


  /** The maximum numbers of alarm state changes during the given time range */
  private int oscNumbers = 6;


  /** The time in seconds before the alarm oscillation flag gets removed once the flapping has stopped */
  private int timeOscillationAlive = 180;
}
