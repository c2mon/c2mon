package cern.c2mon.client.ext.dynconfig.strategy;

import java.net.URI;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.oracle.webservices.internal.api.databinding.DatabindingMode;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.ext.dynconfig.DynConfigService;
import cern.c2mon.client.ext.dynconfig.SupportedProtocolsEnum;
import cern.c2mon.client.ext.dynconfig.configuration.ProcessEquipmentURIMapping;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.impl.DIPHardwareAddressImpl;

/**
 * Implements a configuration strategy for DIP.
 * 
 * @author CERN
 *
 */
public class DipConfigStrategy extends AConfigStrategy  implements ITagConfigurationStrategy {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public DipConfigStrategy() {
	}

	/**
	 * Create DIP Data Tags out of the given URIs
	 * 
	 * @see cern.c2mon.client.ext.dynconfig.strategy.ITagConfigurationStrategy#getConfiguration(java.util.Collection)
	 */
	@Override
	public MultiValueMap<String, DataTag> getConfigurations(ProcessEquipmentURIMapping mapping, Collection<URI> uris) {
		String msgHandler = "cern.c2mon.daq.dip.DIPMessageHandler";
		
		createProcessIfRequired(mapping, msgHandler);

		MultiValueMap<String, DataTag> dataTags = new LinkedMultiValueMap<String, DataTag>();
		for (URI uri : uris) {
				DataTagAddress address = new DataTagAddress(new DIPHardwareAddressImpl(uri.getHost() + uri.getPath()));
				
				DataTag tagToCreate = DataTag.create(uri.toString(), Object.class, address).description(uri.toString()).build();
				dataTags.add(mapping.getEquipmentName(), tagToCreate);
		}

		return dataTags;
	}

	@Override
	public boolean test(URI uri) {
		return uri.getScheme().equals(SupportedProtocolsEnum.PROTOCOL_DIP.getUrlScheme());
	}


	
}
