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
package cern.c2mon.server.cache.datatag;

import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.SourceDataQuality;

/**
 * Provides methods for calculating the quality of a DataTag
 * cache object based on the quality of the incoming source tag.
 * @author mbrightw
 *
 */
public interface QualityConverter {

  /**
   * Converts the {@link SourceDataQuality} into the correct {@link DataTagQuality}.
   * Not this method does not update the DataTag object, but returns a new {@link DataTagQuality} object. 
   * @param sourceDataQuality the incoming value quality
   * @return the new quality for the server object
   */
  DataTagQuality convert(SourceDataQuality sourceDataQuality);

}
