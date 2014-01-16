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
package cern.c2mon.server.cache.tag;

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * Implementation of the common functionality for modifying Tag objects.
 * 
 * These facade objects should synchronize access to the Tag objects (which
 * will then be used by Terracotta to provide cluster-wide synchronization).
 * 
 * @param <T> the Tag class this Facade acts on
 */
public abstract class AbstractTagObjectFacade<T extends Tag> implements CommonTagObjectFacade<T> {

  /** Log4j logger instance */
  private static final Logger LOGGER = Logger.getLogger(AbstractTagObjectFacade.class); 
  
  @Override
  public void validate(T tag) {
    tag.getDataTagQuality().validate();
  }
  
  /**
   * Call within synch.
   * @param tag
   * @param value
   * @param valueDesc
   */
  @Override
  public void updateValue(final T tag, final Object value, final String valueDesc) {
    //cast the passed object to the module cache object implementation
    //to access setter methods (not provided in the common interface)
    AbstractTagCacheObject abstractTag = (AbstractTagCacheObject) tag;
    
    //update the value... need to adjust this for no obj creation
    abstractTag.setValue(value);
    
    /*
     * If the user did not specify a value description, try to get a value
     * description from the value dictionary.
     */
    if (valueDesc != null) {
      if (valueDesc.length() > MAX_DESC_LENGTH) {      
        LOGGER.warn("Detected oversized value description for tag " + tag.getId() + " - is being truncated (max size is set at " + MAX_DESC_LENGTH + ")");
        abstractTag.setValueDescription(valueDesc.substring(0, MAX_DESC_LENGTH));
      } else {
        abstractTag.setValueDescription(valueDesc);
      }
    } else {
      if (abstractTag.getValueDictionary() != null) {
        abstractTag.setValueDescription(abstractTag.getValueDictionary().getDescription(value));
      } else {
        abstractTag.setValueDescription(null);
      }
    }                          
  }
  
  @Override
  public void updateQuality(final T tag, final TagQualityStatus qualityStatusToAdd, final String description) { 
    tag.getDataTagQuality().addInvalidStatus(qualityStatusToAdd, description);   
  }
  
  @Override
  public void addQualityFlag(final T tag, final TagQualityStatus statusToAdd, final String description) {
    tag.getDataTagQuality().addInvalidStatus(statusToAdd, description);      
  }
  
  @Override
  public void setCacheTimestamp(final T tag, final Timestamp timestamp) {
    AbstractTagCacheObject abstractTagCacheObject = (AbstractTagCacheObject) tag;
    abstractTagCacheObject.setCacheTimestamp(timestamp);
  }
  
}
