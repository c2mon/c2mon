package cern.c2mon.client.apitest.db;

import java.util.List;

import cern.c2mon.client.apitest.EquipmentDef;
import cern.c2mon.client.apitest.MetricDef;

public interface C2MonClientApiTestDao {

    /**
     * This method returns all registered metrics for a given process
     * 
     * @return
     */

    List<MetricDef> getProcessMetrics(String processName);

    /**
     * This method returns all registered metrics for a given equipment
     * 
     * @return
     */
    List<MetricDef> getEquipmentMetrics(String equipmentName);

    /**
     * This method returns a list of equipments defined for a given list of processes
     * 
     * @param processNames
     * @return
     */
    List<EquipmentDef> getEquipments(String... processNames);

}
