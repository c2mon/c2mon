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
import cern.c2mon.client.ext.dynconfig.configuration.ProcessEquipmentURIMapping;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.OPCHardwareAddress.ADDRESS_TYPE;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;

public class OpcUaConfigStrategy extends AConfigStrategy implements ITagConfigurationMapping {

	private ConfigurationService configurationService;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public OpcUaConfigStrategy(DynConfigService dynConfServ, ConfigurationService configurationService){
		this.configurationService = configurationService;
	}
	
	
	@Override
	public MultiValueMap<String, DataTag> getConfigurations(ProcessEquipmentURIMapping mapping, Collection<URI> uris) {
		MultiValueMap<String, DataTag> dataTags = new LinkedMultiValueMap<String, DataTag>();
		
 		String msgHandler = "cern.c2mon.daq.dip.DIPMessageHandler";

        createProcessIfRequired(mapping, msgHandler);

		Collection<ProcessNameResponse> processes = configurationService.getProcessNames();
		if (! processes.contains(mapping.getProcessName())) {
			Process process = Process.create(mapping.getProcessName()).id(mapping.getProcessId()).description(mapping.getProcessDescription()).build();
			
			configurationService.createProcess(process);
			
			Equipment equipment = Equipment.create(mapping.getEquipmentName(), "cern.c2mon.daq.opcua.OPCUAMessageHandler").description(mapping.getEquipmentDescription()).build();
			configurationService.createEquipment(mapping.getProcessName(),  equipment);
		}
		
		for (URI uri : uris) {
			if (uri.getScheme().equals(SupportedProtocolsEnum.PROTOCOL_OPCUA.getUrlScheme())) {
				OPCHardwareAddressImpl hwAddr = new OPCHardwareAddressImpl(uri.getPath().substring(1) );
				hwAddr.setAddressType(ADDRESS_TYPE.STRING);
				hwAddr.setNamespace(1);
				DataTagAddress address = new DataTagAddress(hwAddr);
				
				DataTag tagToCreate = DataTag.create(uri.toString(), Object.class, address).description(uri.toString()).build();
				dataTags.add(mapping.getEquipmentName(), tagToCreate);
			}
		}
			
		return dataTags;
	}


	@Override
	public boolean test(URI uri) {
		return uri.getScheme().equals(SupportedProtocolsEnum.PROTOCOL_OPCUA.getUrlScheme());
	}





}
