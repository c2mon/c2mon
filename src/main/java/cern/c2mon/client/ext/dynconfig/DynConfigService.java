package cern.c2mon.client.ext.dynconfig;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.ext.dynconfig.strategy.DipConfigStrategy;
import cern.c2mon.client.ext.dynconfig.strategy.IConfigurationStrategy;
import cern.c2mon.client.ext.dynconfig.strategy.OpcUaConfigStrategy;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;

@Component
public class DynConfigService implements InitializingBean{
	
	@Autowired
	ConfigurationService configurationService;
	
	@Autowired
	TagService tagService;
	
	AtomicLong dataTagSequence = new AtomicLong(100000L);
	
	final Map<String, IConfigurationStrategy> configurationStrategies = new HashMap<>();
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public void setTagService(TagService tagService) {
		this.tagService = tagService;
	}
	
	/**
	 * Initialize the configuration service and all configuration strategies
	 * @return <b>true</b> if all went well
	 */
	public boolean init() {
		configurationStrategies.put(SupportedProtocolsEnum.PROTOCOL_DIP.getUrlScheme(), new DipConfigStrategy(this,configurationService ));
		configurationStrategies.put(SupportedProtocolsEnum.PROTOCOL_OPCUA.getUrlScheme(), new OpcUaConfigStrategy(this,configurationService));
		for(IConfigurationStrategy strategy : configurationStrategies.values()){
			if(!strategy.init()){
				throw new RuntimeException("Could not instantiate strategy "+strategy.getClass().getName());
			}
		}
		return true;
	}

	/**
	 * For a given URI, create or query the corresponding tag.
	 * @param uri
	 * @return A C2MON tag that can be used to subscribe to data.
	 */
	public Tag getTagForURI(URI uri){
		
		Collection<Tag> tags = tagService.findByName(SupportedProtocolsEnum.convertToTagName(uri));
		if(tags.isEmpty()){
			// Lookup an applicable configuration strategy
			IConfigurationStrategy strategy = configurationStrategies.get(uri.getScheme());
			
			MultiValueMap<String,DataTag> equipmentToTags = strategy.getConfigurations(Arrays.asList(new URI[]{uri}));
			
			for (String eq : equipmentToTags.keySet()) {
				ConfigurationReport rep = configurationService.createDataTags(eq, new ArrayList<DataTag>(equipmentToTags.get(eq)));
				// Test the result
			}
			
			// Lookup the tag name again, this time it should exist
			tags = tagService.findByName(SupportedProtocolsEnum.convertToTagName(uri));
		}	
			
		return tags.iterator().next();
	}
	
	public Long getNewDataTagId(){
		return dataTagSequence.getAndIncrement();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
		
	}


	
}
