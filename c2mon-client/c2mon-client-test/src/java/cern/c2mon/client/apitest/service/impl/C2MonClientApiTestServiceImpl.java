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
package cern.c2mon.client.apitest.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.client.apitest.CommandDef;
import cern.c2mon.client.apitest.EquipmentDef;
import cern.c2mon.client.apitest.MetricDef;
import cern.c2mon.client.apitest.db.C2MonClientApiTestDao;
import cern.c2mon.client.apitest.service.C2MonClientApiTestService;

@Service
public class C2MonClientApiTestServiceImpl implements C2MonClientApiTestService {

    private static Logger log = LoggerFactory.getLogger(C2MonClientApiTestServiceImpl.class);

    private C2MonClientApiTestDao dao;

    @Autowired
    public void setDao(C2MonClientApiTestDao dao) {
        this.dao = dao;
    }

    @Transactional(readOnly = true)
    @Override
    public List<MetricDef> getProcessMetrics(String processName) {
        log.debug("entering getProcessMetrics()");
        return dao.getProcessMetrics(processName);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MetricDef> getEquipmentMetrics(String equipmentName) {
        log.debug("entering getEquipmentMetrics()");
        return dao.getEquipmentMetrics(equipmentName);
    }

    @Override
    public List<EquipmentDef> getEquipments(String... processNames) {
        log.debug("entering getEquipments()");
        return dao.getEquipments(processNames);
    }

    @Override
    public List<CommandDef> getRegisteredCommands(String computer) {
        log.debug("entering getRegisteredCommands()");
        return dao.getRegisteredCommands(computer);
    }

}
