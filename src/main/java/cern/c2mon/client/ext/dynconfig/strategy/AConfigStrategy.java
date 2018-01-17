package cern.c2mon.client.ext.dynconfig.strategy;

import java.util.Collection;

import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.ext.dynconfig.configuration.ProcessEquipmentURIMapping;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.process.ProcessNameResponse;

public class AConfigStrategy {

	protected ConfigurationService configurationService;

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public AConfigStrategy() {
		super();
	}

	protected void createProcessIfRequired(ProcessEquipmentURIMapping mapping, String msgHandler) {
		Collection<ProcessNameResponse> processes = configurationService.getProcessNames();
		if (! processes.contains(mapping.getProcessName())) {
			Process process = Process.create(mapping.getProcessName()).id(mapping.getProcessId()).description(mapping.getProcessDescription()).build();
			
			configurationService.createProcess(process);
			
			Equipment equipment = Equipment.create(mapping.getEquipmentName(), msgHandler).description(mapping.getEquipmentDescription()).build();
			configurationService.createEquipment(mapping.getProcessName(),  equipment);
			
		}
	}

}