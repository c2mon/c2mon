/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.cache;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.process.Process;

/**
 * The module public interface that should be used to access the Equipment
 * in the server cache. 
 * 
 * <p>It provides methods for retrieving references to the objects in the
 * cache, which may be accessed by other threads concurrently. To guarantee 
 * exclusive access the thread must synchronize on the Equipment object in
 * the cache.
 * 
 * @author Mark Brightwell
 *
 */
public interface ProcessCache extends C2monCacheWithListeners<Long, Process> {
  
  String cacheInitializedKey = "c2mon.cache.process.initialized";
  
  /**
   * Retrieves a copy of the process in the cache given its name.
   * 
   * <p>Throws an {@link IllegalArgumentException} if called with a null key and
   * a {@link CacheElementNotFoundException} if the object was not found in the 
   * cache (both unchecked).
   * 
   * 
   * @param name the process name
   * @return a reference to the object in the cache
   */
  Process getCopy(String name);
  
  /**
   * Returns the process id given the name.
   * @param name the process name
   * @return the process id
   */
  
  Long getProcessId(String name);
}
