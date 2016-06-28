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
package cern.c2mon.server.cache.common;

import java.util.Properties;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

public abstract class AbstractFacade<T extends Cacheable> implements ConfigurableCacheFacade<T> {
  
  //TODO remove this class as not used
  
  /**
   * 
   * @param cacheable
   * @return
   */ 
//  @Override
//  public boolean isFakeCacheObject(Cacheable cacheable) {
//    synchronized (cacheable) {
//      return cacheable.getId() == 0;
//    }    
//  }
  
  /**
   * Sets the fields of the cache object using the properties object.
   * Only expected fields are explicitly located in the properties
   * object.
   * @param cacheObject the object in the cache to configure
   * @param properties the properties containing the new field values
   * @throws IllegalAccessException 
   * @throws IllegalArgumentException 
   * @throws  
   */
  protected abstract Change configureCacheObject(T cacheObject, Properties properties) throws IllegalAccessException;
  
  /**
   * Validates the configuration of the cache object (in terms of field format etc.).
   * Throws a {@link ConfigurationException} if the validation fails.
   * 
   * @param cacheObject the object to validate
   */
  protected abstract void validateConfig(T cacheObject);
  
  @Override
  public Change updateConfig(T cacheable, Properties properties) throws IllegalAccessException {
    Change changeEvent = configureCacheObject(cacheable, properties);
    validateConfig(cacheable);
    return changeEvent;    
  }
  
  /**
   * If the String is "null", then set the return value
   * to null, else return the original value.
   * 
   * <p>Should be used on fields that accept configurations
   * resetting them to null.
   * 
   * @param fieldValue the field value, may be "null"
   * @return the fieldValue, or null if the fieldValue was "null"
   */
  protected String checkAndSetNull(String fieldValue) {
   return fieldValue.equals("null") ? null : fieldValue;
  }

  

  
  
}
