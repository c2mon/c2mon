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
public class DynConfigService {

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	TagService tagService;

	Collection<IConfigurationStrategy> configurationStrategies = new ArrayList<>();

	public void setConfigurationStrategies(Collection<IConfigurationStrategy> configurationStrategies) {
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

		Collection<Tag> tags = tagService.findByName(SupportedProtocolsEnum.convertToTagName(uri));
		if (tags.isEmpty()) {
			// Lookup an applicable configuration strategy
			for (IConfigurationStrategy strategy : configurationStrategies) {
				MultiValueMap<String, DataTag> equipmentToTags = strategy
						.getConfigurations(Arrays.asList(new URI[] { uri }));
				if (equipmentToTags.size() != 0) {
					for (String eq : equipmentToTags.keySet()) {
						ConfigurationReport rep = configurationService.createDataTags(eq,
								new ArrayList<DataTag>(equipmentToTags.get(eq)));
					}

					// Lookup the tag name again, this time it should exist
					tags = tagService.findByName(SupportedProtocolsEnum.convertToTagName(uri));
				}
			}
		}
		return tags.iterator().next();
	}


}
