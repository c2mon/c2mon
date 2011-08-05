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
	List<MetricDef> getAllRuleMetrics();
}
