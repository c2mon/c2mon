package cern.c2mon.client.apitest.service;

import java.util.List;

import cern.c2mon.client.apitest.MetricDef;

public interface C2MonClientApiTestService {
   
	List<MetricDef> getAllMetrics();
	
    List<MetricDef> getAllDeviceRuleMetrics();

    List<MetricDef> getAllRuleMetrics();	
    
    MetricDef getDeviceRuleMetric(final String processName);	
}
