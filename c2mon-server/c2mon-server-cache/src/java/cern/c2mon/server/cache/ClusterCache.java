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

import java.io.Serializable;


/**
 * This cache is used for custom synchronization actions across the cluster, and is available
 * for use by any module.
 * 
 * <p>For instance, it can be used to share a single timestamp between server nodes, or for sharing
 * a distributed lock. 
 * 
 * <p>In order to allow this cache to be used across modules, the following naming convention should
 * be followed: cache keys for a given module must follow the pattern:
 * 
 * <p><code> module-top-level-package-name.property-name </code>
 * 
 * <p>Note that for generating a shared lock, it is sufficient to enter a custom key and use the cache locking
 * methods. 
 * 
 * @author Mark Brightwell
 *
 */
public interface ClusterCache extends C2monCache<String, Serializable> {


}
