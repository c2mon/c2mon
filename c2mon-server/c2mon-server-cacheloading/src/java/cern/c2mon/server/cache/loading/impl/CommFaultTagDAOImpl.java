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
package cern.c2mon.server.cache.loading.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.CommFaultTagMapper;
import cern.c2mon.server.cache.loading.CommFaultTagDAO;
import cern.c2mon.server.cache.loading.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.commfault.CommFaultTag;

/**
 * CommfaultTag DAO.
 * @author Mark Brightwell
 *
 */
@Service("commFaultTagDAO")
public class CommFaultTagDAOImpl extends AbstractDefaultLoaderDAO<CommFaultTag> implements CommFaultTagDAO {

  /**
   * Reference to mapper.
   */
  private CommFaultTagMapper commFaultTagMapper;

  @Autowired
  public CommFaultTagDAOImpl(CommFaultTagMapper commFaultTagMapper) {
    super(1000, commFaultTagMapper); //initial buffer size
    this.commFaultTagMapper = commFaultTagMapper;
  }

  @Override
  protected CommFaultTag doPostDbLoading(CommFaultTag item) {
    //do nothing for this cache
    return item;
  }

}
