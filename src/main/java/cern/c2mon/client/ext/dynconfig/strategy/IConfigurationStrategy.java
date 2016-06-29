package cern.c2mon.client.ext.dynconfig.strategy;

import java.net.URI;
import java.util.Collection;

import cern.accsoft.commons.util.collections.MultiValueMap;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;

public interface IConfigurationStrategy {

	MultiValueMap<String, DataTag> getConfigurations(Collection<URI> uris);
	
	/**
	 * Initialize the configuration strategy, for instance by registering the required C2MON Equipments and Processes. 
	 */
	boolean init();

}