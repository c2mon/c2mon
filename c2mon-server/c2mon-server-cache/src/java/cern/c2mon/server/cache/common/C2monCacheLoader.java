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
package cern.c2mon.server.cache.common;


/**
 * Interface that must be implemented by all C2MON
 * cache loading mechanisms.
 * 
 * @author Mark Brightwell
 *
 */
public interface C2monCacheLoader {
  
  /**
   * Lock used for synchronising all servers at start up. This takes
   * place during the alive start up: read locks are used for each of
   * the cache loading mechanisms and a write lock is acquired at the
   * alive start up.
   * 
   * An associated Boolean flag in the ClusterCache is indicating if
   * the alive mechanisms was started for all the DAQs loaded into
   * the cache (performed once by a singleserver at start up).
   */
  String aliveStatusInitialized = "c2mon.cache.aliveStatusInitialized";

  /**
   * At server start-up, loads the cache from the DB into memory.
   * In distributed set-up, this is not performed once the TC
   * cache has already been loaded once (only performed if the disk 
   * store is cleaned).
   */
  void preload();

}
