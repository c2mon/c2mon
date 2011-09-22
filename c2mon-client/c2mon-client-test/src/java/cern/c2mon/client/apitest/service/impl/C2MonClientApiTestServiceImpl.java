package cern.c2mon.client.apitest.service.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.client.apitest.EquipmentDef;
import cern.c2mon.client.apitest.MetricDef;
import cern.c2mon.client.apitest.db.C2MonClientApiTestDao;
import cern.c2mon.client.apitest.service.C2MonClientApiTestService;

@Service
public class C2MonClientApiTestServiceImpl implements C2MonClientApiTestService {

    private static Logger log = Logger.getLogger(C2MonClientApiTestServiceImpl.class);

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

}
