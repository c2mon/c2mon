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
package cern.c2mon.server.cache;

import cern.c2mon.server.common.datatag.DataTag;

/**
 * The module public interface that should be used to access the DataTag's
 * in the server cache. This cache contains only DataTag objects that are
 * not ControlTag's (and not RuleTag's).
 * 
 * <p>It provides methods for retrieving references to the objects in the
 * cache, which may be accessed by other threads concurrently. To guarantee 
 * exclusive access the thread must synchronise on the DataTag object in
 * the cache (this is taken care of if using the {@link DataTagFacade} to
 * perform updates).
 * 
 * <p>The getCopy method is available for all Tag caches for retrieving a copy
 * of the cache object.
 * 
 * <p>For accessing DataTag with supervision invalidation performed, use 
 * <code>getCopyWithSupervision(..)</code>
 * 
 * @author Mark Brightwell
 *
 */
public interface DataTagCache extends C2monCacheWithSupervision<Long, DataTag> {
 
  String cacheInitializedKey = "c2mon.cache.datatag.initialized";
}
