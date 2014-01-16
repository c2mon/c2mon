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

import cern.c2mon.server.common.control.ControlTag;

/**
 * This interface is the TIM module public interface to the Control cache containing all
 * ControlTag objects. It should be used by external modules to access Control tags in
 * the server. Care must be taken that when references are returned, other threads may
 * be trying to access the object concurrently. To guarantee exclusive access the
 * thread must synchronize on the ControlTag object retrieved.
 * 
 * <p>Control tags are either Alive tags or Communication Fault tags. The current values of these
 * tags are held in this cache as ControlTagCacheObject's. Configuration information for these
 * tags are held in the AliverTimerCache and CommFaultTagCache.
 * 
 * <p>The getCopy method is available for all Tag caches for retrieving a copy
 * of the cache object.
 * 
 * @author Mark Brightwell
 *
 */
public interface ControlTagCache extends C2monCacheWithListeners<Long, ControlTag> {
  
  String cacheInitializedKey = "c2mon.cache.control.initialized";
  
}
