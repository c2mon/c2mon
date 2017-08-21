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
package cern.c2mon.cache.api.exception;

/**
 * Unchecked exception thrown when a query to a cache is unsuccessful.
 * If it is not certain that a cache contains the sought-after element,
 * preferably use the hasKey() cache method before attempting to retrieve
 * the cache element.
 * 
 * @author mbrightw
 *
 */
public class CacheElementNotFoundException extends RuntimeException {

  /**
   * Calls corresponding RuntimeException constructor.
   */
  public CacheElementNotFoundException() {
    super();    
  }

  /**
   * Calls corresponding RuntimeException constructor.
   */
  public CacheElementNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Calls corresponding RuntimeException constructor.
   */
  public CacheElementNotFoundException(String message) {
    super(message);
  }

  /**
   * Calls corresponding RuntimeException constructor.
   */
  public CacheElementNotFoundException(Throwable cause) {
    super(cause);
  }

}
