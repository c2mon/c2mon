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
package cern.c2mon.server.cache.loading.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.loading.ControlTagLoaderDAO;
import cern.c2mon.server.cache.loading.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;

/**
 * ControlTag loader DAO.
 * @author Mark Brightwell
 *
 */
@Service("controlTagLoaderDAO")
public class ControlTagLoaderDAOImpl extends AbstractDefaultLoaderDAO<ControlTag> implements ControlTagLoaderDAO {

  
  private ControlTagMapper controlTagMapper;
  
  private DataTagMapper dataTagMapper;
  
  @Value("${c2mon.jms.controltag.publication.topic}") 
  private String publicationTopic;
  
  @Autowired
  public ControlTagLoaderDAOImpl(ControlTagMapper controlTagMapper, DataTagMapper dataTagMapper) {
    super(10000, controlTagMapper);
    this.controlTagMapper = controlTagMapper;
    this.dataTagMapper = dataTagMapper;
  }

  @Override
  public void updateConfig(ControlTag controlTag) {    
    dataTagMapper.updateConfig(controlTag);
  }
  
  @Override
  public void deleteItem(Long controlTagId) {
    controlTagMapper.deleteControlTag(controlTagId);
  }

  @Override
  public void insert(ControlTag controlTag) {
    controlTagMapper.insertControlTag((ControlTagCacheObject) controlTag);
  }

  @Override
  protected ControlTag doPostDbLoading(ControlTag item) {
    item.setTopic(publicationTopic);
    return item;
  }
}
