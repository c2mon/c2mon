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
package cern.c2mon.server.cache.dbaccess;

import java.util.List;

import cern.c2mon.shared.common.Cacheable;

/**
 * Needs implementing by cache loaders that load all objects
 * on a single thread (for caches with few objects).
 * 
 * @author Mark Brightwell
 * 
 * @param <T> the type in the cache
 *
 */
public interface LoaderMapper<T extends Cacheable> extends SimpleLoaderMapper<T> {
  
  /**
   * Method responsible for fetching the DataTagCacheObjects from the DB
   * (DATATAG table in TIMPRO).
   * @return a list of cache objects
   */
  List<T> getAll();
  
}
