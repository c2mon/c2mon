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
package cern.c2mon.server.configuration;

/**
 * Provides callbacks with progress of configuration application
 * on the server.
 * 
 * <p>The methods will be called in the order below. The serverTotalParts
 * and daqTotalParts are guaranteed to be called.
 * 
 * @author Mark Brightwell
 *
 */
public interface ConfigProgressMonitor {

  /**
   * Called initially with number of progress notifications expected until complete on server.
   * Is called with 0 if configuration is empty.
   * 
   * @param nbProgressParts number of callbacks expected on onServerProgress method
   */
  void serverTotalParts(int nbParts);
  
  /**
   * Called when server part is complete. If serverTotalParts(n) was called,
   * this method will be called n times.
   * 
   * @param partNb nb of part now completed (out of total parts)
   */
  void onServerProgress(int partNb);
  
  /**
   * Will be called when all changes are done on the server before sending
   * to the DAQ layer.
   * 
   * @param nbParts total number of DAQ callbacks
   */
  void daqTotalParts(int nbParts);
  
  /**
   * Called when DAQ part completed. If daqTotalParts(n) is called beforehand,
   * this method will be called n times.
   * 
   * @param partNb nb of part now completed
   */
  void onDaqProgress(int partNb);
}
