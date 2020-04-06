package cern.c2mon.client.ext.dynconfig;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.ext.dynconfig.config.DynConfiguration;
import cern.c2mon.client.ext.dynconfig.config.ProcessEquipmentURIMapping;
import cern.c2mon.client.ext.dynconfig.factories.ProtocolSpecificFactory;
import cern.c2mon.client.ext.dynconfig.query.IQueryObj;
import cern.c2mon.client.ext.dynconfig.strategy.ITagConfigStrategy;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.request.ClientRequestReport;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Setter
@Component
@ConditionalOnProperty(prefix = "c2mon.dynconfig.component", name = "active", havingValue = "true", matchIfMissing = true)
@EnableAutoConfiguration
@NoArgsConstructor
public class DynConfigService {

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	TagService tagService;

	@Autowired
	private DynConfiguration config;

	/**
	 * Delete the C2MON tag corresponding to a given URI if it exists.
	 * @param uri the uri describing the address for which the corresponding C2MON tag shall be deleted
	 * @throws DynConfigException if the Tag exists but could not be deleted.
	 */
	public void deleteTagForURI(URI uri) throws DynConfigException {
		ProtocolSpecificFactory factory = ProtocolSpecificFactory.of(uri);
		Collection<Tag> tags = findExistingTag(factory.createQueryObj());
		if (!tags.isEmpty()) {
			deleteTags(tags);
			log.info("Deleting data tags {} completed.", tags);
		} else {
			log.info("No data tags to delete");
		}
	}

	/**
	 *
	 /**
	 * For a given URI, query the corresponding tag. Create tag if not found.
	 *
	 * @param uri describes hardware address for which a C2MON tag shall be fetched or created.
	 * @return A C2MON tag that can be used to subscribe to data.
	 * @throws DynConfigException if the URI cannot be parsed or the tag cannot be created.
	 */
	public Tag getTagForURI(URI uri) throws DynConfigException {
		return getTagsForURI(Collections.singletonList(uri)).iterator().next();
	}

	/**
	 * For a set of URIs, query the corresponding tag. Create tags if not found.
	 *
	 * @param uris a collection of uris each describing a hardware address for which a C2MON tag shall be fetched or created
	 * @return A C2MON tag per uri that can be used to subscribe to data.
	 * @throws DynConfigException if an URI cannot be parsed or a tag cannot be created.
	 */
	public Collection<Tag> getTagsForURI(Collection<URI> uris) throws DynConfigException {
		Collection<Tag> tags = new ArrayList<>();
		Collection<ConfigurationReport> reports = new ArrayList<>();

		for (URI uri : uris) {
			ProtocolSpecificFactory factory = ProtocolSpecificFactory.of(uri);
			IQueryObj queryObj = factory.createQueryObj();
			Collection<Tag> tagsForUri = findExistingTag(queryObj);
			if (tagsForUri.isEmpty()) {
				log.info("No data tag could be found for {}. Creating.. ", uri);
				ITagConfigStrategy strategy = factory.createStrategy(queryObj);
				ConfigurationReport report = createTagsAndReport(queryObj, strategy);
				reports.add(report);
				tagsForUri = findExistingTag(queryObj);
				log.info("Data tag {} has been created ", tagsForUri);
			} else {
				log.info("Data tag {} already exists", tagsForUri);
			}
			tags.add(tagsForUri.iterator().next());
		}

		handleConfigurationReport(reports, DynConfigException.Context.CREATE_TAG);
		log.info("Getting data tags {} completed.", tags);
		return tags;
	}

	private Collection<Tag> findExistingTag(IQueryObj queryObj) {
		return tagService.findByName(queryObj.getTagName());
	}

	private void deleteTags(Collection<Tag> dataTags) throws DynConfigException {
		Set<Long> tagIds = extractIds(dataTags);
		ConfigurationReport rep = configurationService.removeDataTagsById(tagIds);
		tagService.refresh(tagIds);
		handleConfigurationReport(Collections.singletonList(rep), DynConfigException.Context.DELETE_TAG);

	}

	private static Set<Long> extractIds(Collection<Tag> tags) {
		return tags.stream().map(Tag::getId).collect(Collectors.toSet());
	}

	private ConfigurationReport createTagsAndReport(IQueryObj queryObj, ITagConfigStrategy strategy) throws DynConfigException {
		ProcessEquipmentURIMapping mapping = findMappingFor(queryObj);
		if (!isCompatibleProcessRunning(mapping)) {
			log.info("No compatible process is running. Create process and equipment. ");
			createProcessAndEquipment(mapping, strategy);
		}
		DataTag dataTag = strategy.prepareTagConfigurations();
		return configurationService.createDataTag(mapping.getEquipmentName(), dataTag);
	}

	private boolean isCompatibleProcessRunning(ProcessEquipmentURIMapping mapping) {
		return configurationService.getProcessNames().stream()
				.anyMatch(response -> response.getProcessName().contains(mapping.getProcessName()));
	}

	private void createProcessAndEquipment(ProcessEquipmentURIMapping mapping, ITagConfigStrategy strategy) {
		Process process = Process.create(mapping.getProcessName())
				.id(mapping.getProcessId())
				.description(mapping.getProcessDescription())
				.build();
		configurationService.createProcess(process);
		log.info("Process created: {}", process.getName());

		Equipment equipment = strategy.prepareEquipmentConfiguration(mapping);
		configurationService.createEquipment(mapping.getProcessName(), equipment);
		log.info("Equipment created: {}", equipment.getName());
	}

	private ProcessEquipmentURIMapping findMappingFor(IQueryObj queryObj) throws DynConfigException {
		return config.getMappings().stream()
				.filter(m -> queryObj.matches(m.getUriPattern()))
				.findFirst()
				.orElseThrow(() -> new DynConfigException(DynConfigException.Context.NO_MAPPING, queryObj.getTagName()));
	}

	private void handleConfigurationReport(Collection<ConfigurationReport> reports, DynConfigException.Context context) throws DynConfigException {
		String errorMessages = reports.stream()
				.filter(ClientRequestReport::isErrorReport)
				.map(ConfigurationReport::getErrorMessage)
				.collect(Collectors.joining(", "));
		if (!errorMessages.isEmpty()) {
			throw new DynConfigException(context, errorMessages);
		}
		String reportStatus = reports.stream().map(ConfigurationReport::getStatusDescription).collect(Collectors.joining(", "));
		log.info("Processed configuration reports with status: {}", reportStatus);
	}
}
