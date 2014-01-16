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

import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.cache.loading.DataTagLoaderDAO;
import cern.c2mon.server.cache.loading.common.AbstractBatchLoaderDAO;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;

/**
 * DataTag loader DAO implementation.
 * 
 * @author Mark Brightwell
 *
 */
@Service("dataTagLoaderDAO")
public class DataTagLoaderDAOImpl extends AbstractBatchLoaderDAO<DataTag> implements DataTagLoaderDAO, ConfigurableDAO<DataTag> {
    
  /**
   * Reference to mapper.
   */
  private DataTagMapper dataTagMapper;
  
  @Value("${c2mon.jms.tag.publication.topic}") 
  private String publicationTrunk; 
  
  @Autowired
  public DataTagLoaderDAOImpl(final DataTagMapper dataTagMapper) {
    super(dataTagMapper);
    this.dataTagMapper = dataTagMapper;    
  }

  @Override
  public void deleteItem(Long id) {
    dataTagMapper.deleteDataTag(id);
  }

  @Override
  public void insert(DataTag dataTag) {
    dataTagMapper.testInsertDataTag((DataTagCacheObject) dataTag);
  }

  
  @Override
  public void updateConfig(DataTag dataTag) {
    dataTagMapper.updateConfig(dataTag);
  }

  @Override
  protected DataTag doPostDbLoading(final DataTag item) {
    item.setTopic(publicationTrunk + "." + item.getProcessId());
    return item;
  }
   
}
