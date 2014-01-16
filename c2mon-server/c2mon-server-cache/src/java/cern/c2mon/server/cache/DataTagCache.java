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
