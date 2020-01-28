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
package cern.c2mon.server.cache.loading.impl;

import cern.c2mon.server.cache.dbaccess.CommFaultTagMapper;
import cern.c2mon.server.cache.loading.CacheLoaderName;
import cern.c2mon.server.cache.loading.CommFaultTagDAO;
import cern.c2mon.server.cache.loading.ControlTagDAOImpl;
import cern.c2mon.server.common.commfault.CommFaultTag;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * CommfaultTag DAO.
 *
 * @author Mark Brightwell
 */
@Service(CacheLoaderName.Names.COMMFAULTTAG)
public class CommFaultTagDAOImpl extends ControlTagDAOImpl<CommFaultTag> implements CommFaultTagDAO {

  @Inject
  public CommFaultTagDAOImpl(CommFaultTagMapper commFaultTagMapper) {
    super(1000, commFaultTagMapper);
  }
}
