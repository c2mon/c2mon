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
package cern.c2mon.server.cache.dbaccess.structure;

/**
 * Specifies a batch of records that needs loading.
 * All cache objects between the specified ids are
 * loaded.
 * 
 * @author Mark Brightwell
 *
 */
public class DBBatch {

  /**
   * First record to load (in an assumed ordered list of records 
   * that need loading from the DB).
   */
  private Long firstId;
  
  /**
   * Last cache object to load.
   */
  private Long lastId;

  /**
   * Unique constructor.
   * @param firstId the first record to load
   * @param lastId the last record to load
   */
  public DBBatch(final Long firstId, final Long lastId) {
    super();
    this.firstId = firstId;
    this.lastId = lastId;
  }

  /**
   * Getter method.
   * @return id of first cache object to be loaded
   */
  public Long getFirstId() {
    return firstId;
  }

  /**
   * Getter method.
   * @return id of last cache object to be loaded
   */
  public Long getLastId() {
    return lastId;
  }
  
}
