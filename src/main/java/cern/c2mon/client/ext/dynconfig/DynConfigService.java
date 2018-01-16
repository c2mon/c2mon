package cern.c2mon.client.ext.dynconfig;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.ext.dynconfig.configuration.DynConfigConfiguration;
import cern.c2mon.client.ext.dynconfig.configuration.ProcessEquipmentURIMapping;
import cern.c2mon.client.ext.dynconfig.strategy.ITagConfigurationMapping;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;

@Component
public class DynConfigService {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	ConfigurationService configurationService;

	@Autowired
	TagService tagService;

	Collection<ITagConfigurationMapping> configurationStrategies = new ArrayList<>();

	@Autowired
	DynConfigConfiguration config;

	public void setConfigurationStrategies(Collection<ITagConfigurationMapping> configurationStrategies) {
		this.configurationStrategies = configurationStrategies;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public void setTagService(TagService tagService) {
		this.tagService = tagService;
	}

	/**
	 * For a given URI, create or query the corresponding tag.
	 * 
	 * @param uri
	 * @return A C2MON tag that can be used to subscribe to data.
	 */
	public Tag getTagForURI(URI uri) {

		Collection<ProcessEquipmentURIMapping> mappings = config.getMappings();
		ProcessEquipmentURIMapping mapping = null;
		for (ProcessEquipmentURIMapping m : mappings) {
			if (uri.toString().matches(m.uriPattern)) {
				mapping = m;
				continue;
			}
		}
		
		if (mapping == null) {
			throw new UnsupportedOperationException("URI " + uri + " does not match any known DAQ process");
		}
		
		Collection<Tag> tags = tagService.findByName(SupportedProtocolsEnum.convertToTagName(uri));

		if (tags.isEmpty()) {
			// Lookup an applicable configuration strategy
			for (ITagConfigurationMapping strategy : configurationStrategies) {
				MultiValueMap<String, DataTag> equipmentToTags = strategy.getConfigurations(mapping,
						Arrays.asList(new URI[] { uri }));
				if (equipmentToTags.size() != 0) {
					for (String eq : equipmentToTags.keySet()) {
						ConfigurationReport rep = configurationService.createDataTags(eq,
								new ArrayList<DataTag>(equipmentToTags.get(eq)));
						// TODO: Inspect the report and propagate any errors as exceptions (or a bulk report) 
					}

					// Lookup the tag name again, this time it should exist
					tags = tagService.findByName(SupportedProtocolsEnum.convertToTagName(uri));
				}
			}
		}else{
			// Tag already exists, just log
			logger.debug("Tag already exists for "+uri+" - returning first match.");
		}
		return tags.iterator().next();
	}

}
