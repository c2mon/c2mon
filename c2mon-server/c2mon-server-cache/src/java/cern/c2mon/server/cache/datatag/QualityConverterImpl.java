/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cache.datatag;

import org.springframework.stereotype.Service;

import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

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
