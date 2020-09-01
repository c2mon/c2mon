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

import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

/**
 * Implementation of the QualityConverter service.
 * @author mbrightw
 *
 */
@Service
public class QualityConverterImpl implements QualityConverter {

  @Override
  public DataTagQuality convert(final SourceDataTagQuality sourceDataTagQuality) {
    DataTagQuality newTagQuality;
    
    if (sourceDataTagQuality == null) {
      newTagQuality = new DataTagQualityImpl();
      newTagQuality.validate();
      return newTagQuality;
    }
      
    switch(sourceDataTagQuality.getQualityCode()) {
      case OK:
        newTagQuality = new DataTagQualityImpl();
        newTagQuality.validate();
        break;
      case OUT_OF_BOUNDS:
        newTagQuality = new DataTagQualityImpl(TagQualityStatus.VALUE_OUT_OF_BOUNDS, sourceDataTagQuality.getDescription());
        break;
      case DATA_UNAVAILABLE:
        newTagQuality = new DataTagQualityImpl(TagQualityStatus.INACCESSIBLE, sourceDataTagQuality.getDescription());
        break;
      default:
        newTagQuality = new DataTagQualityImpl(TagQualityStatus.UNKNOWN_REASON, sourceDataTagQuality.getDescription());
        break;
    }
            
    return newTagQuality;
  }

}
