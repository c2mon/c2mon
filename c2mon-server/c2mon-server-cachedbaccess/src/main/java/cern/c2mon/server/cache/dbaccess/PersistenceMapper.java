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
package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.shared.common.Cacheable;

/**
 * Interface that must be implemented by mappers that will be used for
 * automatically persisting cache updates to the database (corresponds 
 * to all tag caches in TIM).
 * 
 * !not used at the moment as use udpateCacheable(cacheable, mapper); may be able to remove this
 *  if never need this generic method on the simple mapper
 * 
 * @author mbrightw
 *
 */
public interface PersistenceMapper<T extends Cacheable> {

  void updateCacheable(T cacheable);
  
}
