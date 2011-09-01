package cern.c2mon.client.apitest.db;

import java.util.List;

import cern.c2mon.client.apitest.MetricDef;

public interface C2MonClientApiTestDao {

	/**
	 * This method returns all registered metrics
	 * 
	 * @return
	 */

	List<MetricDef> getAllMetrics();
	
	
	/**
     * This method returns all registered metrics for a given process
     * 
     * @return
     */

    List<MetricDef> getAllMetrics(String processName);	
	

	/**
	 * This method returns all registered device-status rules
	 * 
	 * @return
	 */
	List<MetricDef> getAllDeviceRuleMetrics();
	
	
	/**
	 * This method returns all registered rules
	 * 
	 * @return
	 */
	List<MetricDef> getAllSimpleRuleMetrics();
	

    /**
     * This method returns device-status rule metric for given process name
     * 
     * @return
     */
    MetricDef getDeviceRuleMetric(final String processName);	
}
