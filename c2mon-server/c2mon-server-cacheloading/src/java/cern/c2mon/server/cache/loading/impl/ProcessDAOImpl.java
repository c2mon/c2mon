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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.cache.loading.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.process.Process;

/**
 * Process DAO implementation.
 *
 * @author Mark Brightwell
 *
 */
@Service("processDAO")
public class ProcessDAOImpl extends AbstractDefaultLoaderDAO<Process> implements ProcessDAO {
  /**
   * LOG4J Logger for this class.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDAOImpl.class);

  private ProcessMapper processMapper;

  @Value("${c2mon.server.daqcommunication.jms.queue.trunk}")
  private String jmsDaqQueueTrunk;

  @Autowired
  public ProcessDAOImpl(ProcessMapper processMapper) {
    super(500, processMapper); // initial buffer size
    this.processMapper = processMapper;
  }

  @Override
  public void deleteProcess(Long processId) {
    processMapper.deleteProcess(processId);
  }

  @Override
  public void deleteItem(Long id) {
    processMapper.deleteProcess(id);
  }

  @Override
  public void insert(Process process) {
    processMapper.insertProcess(process);
  }

  @Override
  public void updateConfig(Process process) {
    processMapper.updateProcessConfig(process);
  }

  @Override
  protected Process doPostDbLoading(Process process) {
    process.setJmsDaqCommandQueue(jmsDaqQueueTrunk + ".command." + process.getCurrentHost() + "." + process.getName() + "." + process.getProcessPIK());

    if (process.getProcessPIK() != null) {
      LOGGER.debug("doPostDbLoading - jmsDaqCommandQueue: " + process.getJmsDaqCommandQueue());
    } else {
      LOGGER.warn("doPostDbLoading - Null PIK registered. Probably empty Data Base info. Waiting to fill it up through the cache persistence");
    }

    return process;
  }

  @Override
  public Integer getNumTags(Long processId) {
    return processMapper.getNumTags(processId);
  }

  @Override
  public Integer getNumInvalidTags(Long processId) {
    return processMapper.getNumInvalidTags(processId);
  }
}
