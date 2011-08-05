package cern.c2mon.client.apitest;

import java.util.List;

public interface C2MonClientApiTestService {
   
	List<MetricDef> getAllMetrics();
	
    List<MetricDef> getAllDeviceRuleMetrics();

    List<MetricDef> getAllRuleMetrics();	
    
    MetricDef getDeviceRuleMetric(final String processName);	
}
