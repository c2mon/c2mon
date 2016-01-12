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
package cern.c2mon.server.eslog.logger;

/**
 * Handles the bulk indexing settings.
 * Default is %600 actions before flush OR 5 seconds. We have an interval of 1 second between the flushes and have at
 * most 1 concurrent bulkProcessors.
 * @author Alban Marguet.
 */
public enum BulkSettings {
  BULK_ACTIONS (5600),
  BULK_SIZE (5),
  FLUSH_INTERVAL (10),
  CONCURRENT (1);

  private final int setting;

  BulkSettings(int setting) {
    this.setting = setting;
  }

  public int getSetting() {
    return setting;
  }
}
