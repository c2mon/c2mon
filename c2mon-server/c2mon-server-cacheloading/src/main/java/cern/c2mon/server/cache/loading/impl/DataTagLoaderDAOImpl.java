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
  
  @Value("${c2mon.server.client.jms.topic.tag.trunk}")
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
    dataTagMapper.insertDataTag((DataTagCacheObject) dataTag);
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
