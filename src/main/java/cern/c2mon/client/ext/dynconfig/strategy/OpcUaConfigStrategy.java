package cern.c2mon.client.ext.dynconfig.strategy;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.ext.dynconfig.DynConfigService;
import cern.c2mon.client.ext.dynconfig.SupportedProtocolsEnum;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.OPCHardwareAddress.ADDRESS_TYPE;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;

public class OpcUaConfigStrategy implements IConfigurationStrategy {
	DynConfigService dynConfigService;

	public static final Long OPCUA_PROCESS_ID = 40101L;
	
	public static final String PROCESS_NAME = "P_DYNOPCUA";
	public static final String EQUIPMENT_NAME = "dynopcua.equipment";
	
	private ConfigurationService configurationService;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public OpcUaConfigStrategy(DynConfigService dynConfServ, ConfigurationService configurationService){
		dynConfigService = dynConfServ;
		this.configurationService = configurationService;
	}
	
	Map<String, Long[]> opcUaNameToEqProcID = new HashMap<>();
	
	@Override
	public boolean init() {
		Collection<ProcessNameResponse> processes = configurationService.getProcessNames();
		if (! processes.contains(PROCESS_NAME)) {
			Process process = Process.create(PROCESS_NAME).id(OPCUA_PROCESS_ID).description("DYNOPCUA Process").build();
			
			configurationService.createProcess(process);
			
			Equipment equipment = Equipment.create(EQUIPMENT_NAME, "cern.c2mon.daq.opcua.OPCUAMessageHandler").description("DYNOPCUA Process").build();
			configurationService.createEquipment(PROCESS_NAME,  equipment);
			
		}
		return true;
	}
	
	
	
	@Override
	public MultiValueMap<String, DataTag> getConfigurations(Collection<URI> uris) {
		MultiValueMap<String, DataTag> dataTags = new LinkedMultiValueMap<String, DataTag>();
		for (URI uri : uris) {
			if (uri.getScheme().equals(SupportedProtocolsEnum.PROTOCOL_OPCUA.getUrlScheme())) {
				OPCHardwareAddressImpl hwAddr = new OPCHardwareAddressImpl(uri.getPath().substring(1) );
				hwAddr.setAddressType(ADDRESS_TYPE.STRING);
				hwAddr.setNamespace(1);
				DataTagAddress address = new DataTagAddress(hwAddr);
				
				DataTag tagToCreate = DataTag.create(uri.toString(), Object.class, address).description(uri.toString()).build();
				dataTags.add(EQUIPMENT_NAME, tagToCreate);
			}
		}
			
		return dataTags;
	}



}
