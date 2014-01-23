/******************************************************************************
 * This file is part of the CERN Control and Monitoring (C2MON) platform.
 * 
 * See http://cern.ch/c2mon
 * 
 * Copyright (C) 2005-2013 CERN.
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
 * Author: C2MON team, c2mon-support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common;

import cern.c2mon.shared.daq.datatag.SourceDataTag;

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
