package cern.c2mon.client.apitest.service.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<MetricDef> getAllMetrics() {
        log.debug("entering getAllMetrics()");
        return dao.getAllMetrics();
    }

    @Transactional(readOnly = true)
    @Override
    public List<MetricDef> getAllMetrics(String processName) {
        log.debug("entering getAllMetrics()");
        return dao.getAllMetrics(processName);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MetricDef> getAllDeviceRuleMetrics() {
        log.debug("entering getAllDeviceRuleMetrics()");
        return dao.getAllDeviceRuleMetrics();
    }

    @Transactional(readOnly = true)
    @Override
    public List<MetricDef> getAllRuleMetrics() {
        log.debug("entering getAllRuleMetrics()");
        return dao.getAllSimpleRuleMetrics();
    }

    @Transactional(readOnly = true)
    @Override
    public MetricDef getDeviceRuleMetric(String processName) {
        log.debug("entering getAllRuleMetrics()");
        return dao.getDeviceRuleMetric(processName);
    }

}
