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
 * Handles the settings of the indices in ElasticSearch.
 * By default: 10 shards and 0 replica for monthly indices.
 * By default: 1 shards and 0 replica for daily indices.
 * TODO: MUST give replica in order to insure consistency in the future.
 * @author Alban Marguet.
 */
public enum IndexSettings {

  INDEX_MONTH_SETTINGS(10, 0),
  INDEX_DAILY_SETTINGS(1, 0);

  private final int shards;
  private final int replica;

  IndexSettings(int shards, int replica) {
    this.shards = shards;
    this.replica = replica;
  }

  public int getShards() {
    return shards;
  }

  public int getReplica() {
    return replica;
  }

  public int[] getSettings() {
    return new int[]{shards, replica};
  }
}
