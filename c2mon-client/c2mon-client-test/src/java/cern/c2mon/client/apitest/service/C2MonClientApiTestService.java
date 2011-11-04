package cern.c2mon.client.apitest.service;

import java.util.List;

import cern.c2mon.client.apitest.CommandDef;
import cern.c2mon.client.apitest.EquipmentDef;
import cern.c2mon.client.apitest.MetricDef;

public interface C2MonClientApiTestService {
   	   
	List<MetricDef> getProcessMetrics(String processName);
	
	List<MetricDef> getEquipmentMetrics(String equipmentName);
		
	List<EquipmentDef> getEquipments(String... processNames); 
	
	List<CommandDef> getRegisteredCommands(String computer);
}
