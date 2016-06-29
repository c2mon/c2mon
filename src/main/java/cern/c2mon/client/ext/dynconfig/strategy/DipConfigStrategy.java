package cern.c2mon.client.ext.dynconfig.strategy;

import java.net.URI;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.accsoft.commons.util.collections.MultiValueMap;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.ConfigurationService;
import cern.c2mon.client.core.TagService;
import cern.c2mon.client.ext.dynconfig.DynConfigService;
import cern.c2mon.client.ext.dynconfig.SupportedProtocolsEnum;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.impl.DIPHardwareAddressImpl;

/**
 * Implements a configuration strategy for DIP.
 * 
 * @author CERN
 *
 */
public class DipConfigStrategy implements IConfigurationStrategy {

	public static final Long DIP_PROCESS_ID = 20101L;
	
	public static final String PROCESS_NAME = "P_DYNDIP";
	public static final String EQUIPMENT_NAME = "dyndip.equipment";
	

	DynConfigService dynConfigService;

	Logger logger = LoggerFactory.getLogger(this.getClass());
	private ConfigurationService configurationService;

	@Override
	public boolean init() {
		Collection<ProcessNameResponse> processes = C2monServiceGateway.getConfigurationService().getProcessNames();
		if (! processes.contains(PROCESS_NAME)) {
			Process process = Process.create(PROCESS_NAME).id(DIP_PROCESS_ID).description("DYNDIP Process").build();
			
			ConfigurationReport report = configurationService.createProcess(process);
			
			Equipment equipment = Equipment.create(EQUIPMENT_NAME, "cern.c2mon.daq.dip.DIPMessageHandler").description("DYNDIP Process").build();
			report = configurationService.createEquipment(PROCESS_NAME,  equipment);
			
		}
		return true;
	}

	public DipConfigStrategy(DynConfigService dynConfServ, ConfigurationService configurationService) {
		dynConfigService = dynConfServ;
		this.configurationService = configurationService;
	}

	/**
	 * Create DIP Data Tags out of the given URIs
	 * 
	 * @see cern.c2mon.client.ext.dynconfig.strategy.IConfigurationStrategy#getConfiguration(java.util.Collection)
	 */
	@Override
	public MultiValueMap<String, DataTag> getConfigurations(Collection<URI> uris) {
		MultiValueMap<String, DataTag> dataTags = new MultiValueMap<>();
		for (URI uri : uris) {
			if (uri.getScheme().equals(SupportedProtocolsEnum.PROTOCOL_DIP.getUrlScheme())) {
				DataTagAddress address = new DataTagAddress(new DIPHardwareAddressImpl(uri.getHost() + uri.getPath()));
				
				DataTag tagToCreate = DataTag.create(uri.toString(), Object.class, address).description(uri.toString()).build();
//				dataTags.add(DataTag.builder().id((dynConfigService.getNewDataTagId())).name(uri.toString())
//						.description(uri.toString()).address(address).dataType(DataType.STRING).build());
				dataTags.put(EQUIPMENT_NAME, tagToCreate);
			}
		}

//		configurationService.createDataTags(EQUIPMENT_NAME, dataTags);

		return dataTags;
	}
}
