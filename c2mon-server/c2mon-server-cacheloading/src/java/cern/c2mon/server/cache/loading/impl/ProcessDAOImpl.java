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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.cache.loading.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.process.Process;

/**
 * Process DAO implementation.
 * @author Mark Brightwell
 *
 */
@Service("processDAO")
public class ProcessDAOImpl extends AbstractDefaultLoaderDAO<Process> implements ProcessDAO {
	/**
	 * LOG4J Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ProcessDAOImpl.class);

	private ProcessMapper processMapper;

	@Value("${c2mon.jms.process.listener.trunk}") 
	private String processListenerTrunk;

	@Autowired
	public ProcessDAOImpl(ProcessMapper processMapper) {
		super(500, processMapper); //initial buffer size
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
		process.setJmsListenerTopic(processListenerTrunk + ".command." + process.getCurrentHost() + "." 
				+ process.getName() + "." + process.getProcessPIK());
		LOGGER.debug("doPostDbLoading - jmsListenerQueue: " + process.getJmsListenerTopic());

		return process;
	}
}
