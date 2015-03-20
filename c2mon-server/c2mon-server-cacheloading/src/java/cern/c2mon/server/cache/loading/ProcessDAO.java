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
package cern.c2mon.server.cache.loading;

import cern.c2mon.server.common.process.Process;

/**
 * Process DAO specification.
 *
 * @author Mark Brightwell
 *
 */
public interface ProcessDAO extends CacheLoaderDAO<Process>, ConfigurableDAO<Process> {

  void deleteProcess(Long processId);

  /**
   * Retrieve the number of tags currently configured for a given process.
   *
   * @param processId the ID of the process
   * @return the number of tags configured for the process
   */
  Integer getNumTags(Long processId);

  /**
   * Retrieve the number of currently configured tags that are invalid for a
   * process.
   *
   * @param processId the ID of the process
   * @return the number of invalid tags configured for the process
   */
  Integer getNumInvalidTags(Long processId);

}
