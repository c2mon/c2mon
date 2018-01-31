package cern.c2mon.client.ext.dynconfig;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.ext.dynconfig.configuration.DynConfigConfiguration;
import cern.c2mon.client.ext.dynconfig.configuration.ProcessEquipmentURIMapping;
import cern.c2mon.client.ext.dynconfig.strategy.AConfigStrategy;
import cern.c2mon.client.ext.dynconfig.strategy.ITagConfigurationStrategy;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;

@Component
@ConditionalOnProperty(prefix = "c2mon.dynconfig.component", name = "active", havingValue = "true", matchIfMissing = true)
public class DynConfigService {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	TagService tagService;

	@Autowired
	private Collection<ITagConfigurationStrategy> configurationStrategies = null;

	@Autowired
	DynConfigConfiguration config;

	public DynConfigService() {
		// Setup default configuration strategies

	}

	public void setConfigurationStrategies(Collection<ITagConfigurationStrategy> configurationStrategies) {
		this.configurationStrategies = configurationStrategies;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public void setTagService(TagService tagService) {
		this.tagService = tagService;
	}

	public boolean deleteTagForURI(URI uri) {
		Collection<Tag> tags = tagService.findByName(SupportedProtocolsEnum.convertToTagName(uri));
		if (!tags.isEmpty()) {
			for (Tag t : tags) {
				try{
				  ConfigurationReport rep = configurationService.removeDataTagById(t.getId());
				  if (rep.isErrorReport()) {
					logger.error("Could not delete data tag " + uri + " : " + rep.getErrorMessage());
				  }
				}catch(Exception e){
					logger.error("Exception while deleting data tag " + uri + " due to exception: ",e);
				}
			}
			return true;
		} else {
			return false;
		}
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
			ITagConfigurationStrategy appliedStrategy = null;
			// Lookup an applicable configuration strategy
			for (ITagConfigurationStrategy strategy : configurationStrategies) {
				if (strategy.test(uri)) {

					// Lazily inject a configuration service reference
					if (((AConfigStrategy) strategy).getConfigurationService() == null) {
						((AConfigStrategy) strategy).setConfigurationService(configurationService);
					}

					appliedStrategy = strategy;
					MultiValueMap<String, DataTag> equipmentToTags = strategy.getConfigurations(mapping,
							Arrays.asList(new URI[] { uri }));

					if (equipmentToTags.size() != 0) {
						for (String eq : equipmentToTags.keySet()) {
							ConfigurationReport rep = configurationService.createDataTags(eq,
									new ArrayList<DataTag>(equipmentToTags.get(eq)));
							// TODO: Inspect the report and propagate any errors
							// as exceptions (or a bulk report)
							if (rep.isErrorReport()) {
								logger.error("Could not create data tag " + uri + " : " + rep.getErrorMessage());
							}
						}

						// Lookup the tag name again, this time it should exist
						tags = tagService.findByName(SupportedProtocolsEnum.convertToTagName(uri));
					}
					break;
				}
			}

			if (appliedStrategy == null) {
				logger.error("Could not find applicable tag configuration strategy for URI " + uri);
			}
		} else {
			// Tag already exists, just log
			logger.debug("Tag already exists for " + uri + " - returning first match.");
		}
		return (tags.size() > 0 ? tags.iterator().next() : null);
	}

}
