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
package cern.c2mon.daq.common;

import cern.c2mon.shared.common.datatag.SourceDataTag;

/**
 * Interface to expose the record method for dynamic deadband filtering
 * 
 * @author vilches
 *
 */
public interface IDynamicTimeDeadbandFilterer {
	 
  /**
   * TimeDeadband policy:
   * 
   * Static TimeDeadband has more priority than the Dynamic one. So if the Static TimeDeadband for the 
   * current Tag is disable and the DAQ has the Dynamic TimeDeadband enabled then the Tag will be 
   * recorded for dynamic time deadband filtering depending on the tag priority 
   * (only LOW and MEDIUM are used).
   * 
   * @param tag The tag to be recorded.
   */
  void recordTag(final SourceDataTag tag);
  
  /**
   * Checks if Dynamic Time deadband can be applied or not
   * 
   * @param tag The tag to be recorded.
   * @return True if the Dynamic Time deadband can be apply or false if not
   */
  public boolean isDynamicTimeDeadband(final SourceDataTag tag);
}
