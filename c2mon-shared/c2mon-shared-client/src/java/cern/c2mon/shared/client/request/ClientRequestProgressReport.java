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
package cern.c2mon.shared.client.request;


/**
 * This interface is used to indicate the progress of a <code>ClientRequest</code>
 * in the server side. 
 * 
 * @author ekoufaki
 */
public interface ClientRequestProgressReport extends ClientRequestResult {
  
  /**
   * Every progress report consists of a number of operations.
   * @return How many operations to expect for this progress report.
   */
  int getTotalOperationsCount();
  
  /**
   * @return The current operation.
   */
  int getCurrentOperation();
  
  /**
   * @return How many parts to expect for this progress report.
   * Refers to the current operation.
   */
  int getTotalProgressParts();
  
  /**
   * @return The current progress
   * Refers to the current operation.
   */
  int getCurrentProgressPart();
  
  /**
   * Optional.
   * @return a description of the current progress stage
   */
  String getProgressDescription();
}
