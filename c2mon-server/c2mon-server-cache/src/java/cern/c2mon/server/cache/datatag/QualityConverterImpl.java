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

import org.springframework.stereotype.Service;

import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * Implementation of the QualityConverter service.
 * @author mbrightw
 *
 */
@Service
public class QualityConverterImpl implements QualityConverter {

  @Override
  public DataTagQuality convert(final SourceDataQuality sourceDataQuality) {
    DataTagQuality newTagQuality;
    if (sourceDataQuality != null && sourceDataQuality.isInvalid()) {
      if (sourceDataQuality.getQualityCode() == SourceDataQuality.OUT_OF_BOUNDS) {
        newTagQuality = new DataTagQualityImpl(TagQualityStatus.VALUE_OUT_OF_BOUNDS, sourceDataQuality.getDescription());                         
      } else if (sourceDataQuality.getQualityCode() == SourceDataQuality.DATA_UNAVAILABLE) {
        newTagQuality = new DataTagQualityImpl(TagQualityStatus.INACCESSIBLE, sourceDataQuality.getDescription());          
      } else {
        newTagQuality = new DataTagQualityImpl(TagQualityStatus.UNKNOWN_REASON, sourceDataQuality.getDescription());                    
      }      
    } else {
      newTagQuality = new DataTagQualityImpl();
      newTagQuality.validate();
    }
    return newTagQuality;
  }

}
