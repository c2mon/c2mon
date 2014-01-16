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

import cern.c2mon.server.cache.dbaccess.structure.DBBatch;
import cern.c2mon.shared.common.Cacheable;

/**
 * Required for caches that use the batch loading mechanism.
 * 
 * @author Mark Brightwell
 *
 */
public interface BatchLoaderMapper<T extends Cacheable> extends SimpleLoaderMapper<T> {

  /**
   * Return a list of records from the DB 
   * @param dbBatch specifies the batch of cache objects to load
   * @return the list of records
   */
  List<T> getRowBatch(DBBatch dbBatch);

  /**
   * Returns the number of Cacheable objects that need loading from the DB.
   * @return the # of records
   */
  int getNumberItems();
  
  /**
   * Returns the maximum id of the cache objects to load.
   * @return max id
   */
  Long getMaxId();
  
  /**
   * Returns the minimum id of the cache objects to load.
   * @return min id
   */
  Long getMinId();
  
}
