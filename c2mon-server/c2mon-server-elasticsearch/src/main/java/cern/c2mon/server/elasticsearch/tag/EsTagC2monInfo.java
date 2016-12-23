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
package cern.c2mon.server.elasticsearch.tag;

import lombok.Data;

import cern.c2mon.server.common.tag.Tag;

/**
 * Represents a detailed and very specific information
 * about the core functionality of C2MON framework.
 * The values under that information group are defined directly
 * on the core C2MON domain, and are extracted from the
 */
@Data
public class EsTagC2monInfo {

  /**
   * The name of the monitored process
   */
  private String process;

  /**
   * The name of the monitored equipment
   */
  private String equipment;

  /**
   * The name of the monitored sub-equipment
   */
  private String subEquipment;

  /**
   * The fully qualified value (classname) of a tag's
   * enclosed metric value
   */
  private final String dataType;

  /**
   * The time when the server received the {@link Tag}
   */
  private long serverTimestamp;

  /**
   * The time when the {@link Tag} value was collected.
   */
  private long sourceTimestamp;

  /**
   * The time when the DAQ received the {@link Tag}
   */
  private long daqTimestamp;

}
