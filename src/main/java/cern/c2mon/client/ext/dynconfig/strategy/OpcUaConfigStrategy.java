package cern.c2mon.client.ext.dynconfig.strategy;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.accsoft.commons.util.collections.MultiValueMap;
import cern.c2mon.client.core.ConfigurationService;
import cern.c2mon.client.ext.dynconfig.DynConfigService;
import cern.c2mon.client.ext.dynconfig.SupportedProtocolsEnum;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.OPCHardwareAddress.ADDRESS_TYPE;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;

public class OpcUaConfigStrategy implements IConfigurationStrategy {
	DynConfigService dynConfigService;

	public static final Long OPCUA_EQUIPMENT_ID = 20101L;
	public static final Long OPCUA_EQUIPMENT_ALIVE_TAG_ID = 20111L;
	public static final Long OPCUA_EQUIPMENT_STATUS_TAG_ID = 20112L;
	public static final Long OPCUA_EQUIPMENT_COMMFAULT_TAG_ID = 20113L;

	public static final Long OPCUA_PROCESS_ID = 40101L;
	public static final Long OPCUA_PROCESS_ALIVE_TAG_ID = 40111L;
	public static final Long OPCUA_PROCESS_STATUS_TAG_ID = 40112L;
	private static final Long OPCUA_CONFIG_ID = 20L;
	
	public static final String PROCESS_NAME = "P_DYNOPCUA";
	public static final String EQUIPMENT_NAME = "dynopcua.equipment";

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public OpcUaConfigStrategy(DynConfigService dynConfServ, ConfigurationService configurationService){
		dynConfigService = dynConfServ;
		this.configurationService = configurationService;
	}
	
	Map<String, Long[]> opcUaNameToEqProcID = new HashMap<>();
	
	@Override
	public boolean init() {
//		Tag opcuaProcessAliveTag = C2monServiceGateway.getTagService().get(OPCUA_PROCESS_ALIVE_TAG_ID);
//		if (!opcuaProcessAliveTag.isValid()) {
//			StatusTag eqStatusTag = StatusTag.builder().id(OPCUA_EQUIPMENT_STATUS_TAG_ID).name("dynopcua.equipment.status")
//					.description("DYN OPCUA Equipment status tag").build();
//			CommFaultTag eqCommFaultTag = CommFaultTag.builder().id(OPCUA_EQUIPMENT_COMMFAULT_TAG_ID)
//					.name("dynopcua.equipment.commfault").description("DYN DIP Equipment Comm Fault Tag").build();
//
//			Equipment equipment = Equipment.builder().id(OPCUA_EQUIPMENT_ID).name("dynopcua.equipment")
//					.description("DYNOPCUA Equipment").handlerClass("cern.c2mon.daq.opcua.OPCUAMessageHandler")
//			//		.address("URI=opc.tcp://pitrafficlight.dyndns.cern.ch:4841/open62541;serverTimeout=5000;serverRetryTimeout=10000;aliveWriter=false")
//					.address("URI=opc.tcp://pitrafficlighteth.cern.ch:4841/open62541;serverTimeout=5000;serverRetryTimeout=10000;aliveWriter=false")
//					.statusTag(eqStatusTag).commFaultTag(eqCommFaultTag).build();
//
//			StatusTag pStatusTag = StatusTag.builder().id(OPCUA_PROCESS_STATUS_TAG_ID).name("dynopcua.process.status")
//					.description("DYNOPCUA status tag").build();
//			AliveTag pAliveTag = AliveTag.builder().id(OPCUA_PROCESS_ALIVE_TAG_ID).name("dynopcua.process.alive")
//					.description("DYNOPCUA alive tag").build();
//
//			Process process = Process.builder().id(OPCUA_PROCESS_ID).name("P_DYNOPCUA").description("DYNOPCUA Process")
//					.equipment(equipment).statusTag(pStatusTag).aliveTag(pAliveTag).build();
//			Configuration config = Configuration.builder().confId(OPCUA_CONFIG_ID).name("DYNOPCUA Configuration")
//					.application("DYNOPCUA").process(process).build();
//			ConfigurationReport report = C2monServiceGateway.getConfigurationService().applyConfiguration(config,
//					new ClientRequestReportListener() {
//
//						@Override
//						public void onProgressReportReceived(ClientRequestProgressReport progressReport) {
//							logger.info(progressReport.getProgressDescription());
//						}
//
//						@Override
//						public void onErrorReportReceived(ClientRequestErrorReport errorReport) {
//							logger.error("Error while configuration DYNOPCUA config " + errorReport.getErrorMessage());
//						}
//					});
//
////			if (!report.executedSuccessfully()) {
////				return false;
////			}
//		}
//		opcUaNameToEqProcID.put("pitrafficlight", new Long[]{OPCUA_CONFIG_ID, OPCUA_PROCESS_ID, OPCUA_EQUIPMENT_ID});
		return true;
	}
	
	
	
	@Override
	public MultiValueMap<String, DataTag> getConfigurations(Collection<URI> uris) {
		MultiValueMap<String, DataTag> dataTags = new MultiValueMap<>();
		for (URI uri : uris) {
			if (uri.getScheme().equals(SupportedProtocolsEnum.PROTOCOL_OPCUA.getUrlScheme())) {
				OPCHardwareAddressImpl hwAddr = new OPCHardwareAddressImpl(uri.getPath().substring(1) );
				hwAddr.setAddressType(ADDRESS_TYPE.STRING);
				hwAddr.setNamespace(1);
				DataTagAddress address = new DataTagAddress(hwAddr);
				
				DataTag tagToCreate = DataTag.create(uri.toString(), Object.class, address).description(uri.toString()).build();
				dataTags.put(EQUIPMENT_NAME, tagToCreate);
			}
		}
			
		return dataTags;
				
//		List<DataTag> dataTags = new ArrayList<>();
//				
//				
//		List<Configuration> configs = new ArrayList<>();
//		
//		// Find each different host
//		Map<String, List<URI>> hosts = uris.stream().collect(Collectors.groupingBy(URI::getHost));
//		
//		for(String host: hosts.keySet()){
//			List<URI> hostUris = hosts.get(host);
//			for (URI uri : hostUris) {
//				if (uri.getScheme().equals(SupportedProtocolsEnum.PROTOCOL_OPCUA.getUrlScheme())) {
//					// TODO : Parse the URI path to capture : 
//					//     - Namespace index
//					//     - Address type (GUID, String or Numeric)
//					OPCHardwareAddressImpl hwAddr = new OPCHardwareAddressImpl(uri.getPath().substring(1) );
//					hwAddr.setAddressType(ADDRESS_TYPE.STRING);
//					hwAddr.setNamespace(1);
//					DataTagAddress address = new DataTagAddress(hwAddr);
//					dataTags.add(DataTag.builder().id((dynConfigService.getNewDataTagId())).name(uri.toString())
//							.description(uri.toString()).address(address).dataType(DataType.INTEGER).build());
//				}
//			}
//			Long[] eqProcPair = opcUaNameToEqProcID.get(host);
//			
//			Equipment equipment = Equipment.builder().id(eqProcPair[2]).dataTags(dataTags).build();
//			Process process = Process.builder().id(eqProcPair[1]).equipment(equipment).build();
//			configs.add(Configuration.builder().confId(eqProcPair[0]).process(process).build());
//		}
//
//		return configs;
	}



}
