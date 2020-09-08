package cern.c2mon.client.ext.dynconfig;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.ext.dynconfig.config.DynConfiguration;
import cern.c2mon.client.ext.dynconfig.config.ProcessEquipmentURIMapping;
import cern.c2mon.client.ext.dynconfig.strategy.ITagConfigStrategy;
import cern.c2mon.client.ext.dynconfig.strategy.TagConfigStrategy;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.request.ClientRequestReport;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Setter
@Component
@ConditionalOnProperty(prefix = "c2mon.dynconfig.component", name = "active", havingValue = "true", matchIfMissing = true)
@EnableAutoConfiguration
@NoArgsConstructor
public class DynConfigService {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private TagService tagService;

    @Autowired
    private DynConfiguration config;

    /**
     * Delete the C2MON tag corresponding to a given URI if it exists.
     * @param uri the uri describing the address for which the corresponding C2MON tag shall be deleted
     * @throws DynConfigException if the Tag exists but could not be deleted.
     */
    public void deleteTagForURI(URI uri) throws DynConfigException {
        String tagName = URIParser.toTagName(uri);
        Collection<Tag> tags = tagService.findByName(tagName);
        if (!tags.isEmpty()) {
            deleteTags(tags);
            log.info("Deleting data tags {} completed.", tags);
        } else {
            log.info("No data tags to delete");
        }
    }

    /**
     * /** For a given URI, query the corresponding tag. Create tag if not found.
     * @param uri describes hardware address for which a C2MON tag shall be fetched or created.
     * @return A C2MON tag that can be used to subscribe to data.
     * @throws DynConfigException if the URI cannot be parsed or the tag cannot be created.
     */
    public Tag getTagForURI(URI uri) throws DynConfigException {
        final List<Tag> tagsForURI = getTagsForURI(Collections.singletonList(uri));
        if (tagsForURI.isEmpty()) {
            throw new DynConfigException(DynConfigException.Context.CREATE_TAG);
        }
        return tagsForURI.get(0);
    }

    /**
     * For a set of URIs, query the corresponding tag. Create tags if not found.
     * @param uris a collection of uris each describing a hardware address for which a C2MON tag shall be fetched or
     *             created
     * @return A C2MON tag per uri that can be used to subscribe to data.
     * @throws DynConfigException if an URI cannot be parsed or a tag cannot be created.
     */
    public List<Tag> getTagsForURI(Collection<URI> uris) throws DynConfigException {
        List<Tag> tags = new ArrayList<>();
        Collection<ConfigurationReport> reports = new ArrayList<>();

        for (URI uri : uris) {
            String tagName = URIParser.toTagName(uri);
            Collection<Tag> tagsForUri = tagService.findByName(tagName);
            if (tagsForUri.isEmpty()) {
                log.info("No tag could be found for  URI {}. Creating... ", uri);
                ConfigurationReport report;
                try {
                    final Map.Entry<Long, ConfigurationReport> idReport = createTagsAndReport(uri, tagName);
                    report = idReport.getValue();
                } catch (DynConfigException e) {
                    log.error("Could not associate the URI {} to a mapping. Proceed to next URI...", uri.toString(), e);
                    continue;
                }
                reports.add(report);
                logReports(report, tagsForUri);
                tagsForUri = tagService.findByName(tagName);
            } else {
                log.info("Tag {} already exists", tagsForUri);
            }
            if (tagsForUri.iterator().hasNext()) {
                tags.add(tagsForUri.iterator().next());
            }
        }
        handleConfigurationReport(reports, DynConfigException.Context.CREATE_TAG);
        log.info("Completed creating tags {}.", tags);
        return tags;
    }

    private void logReports(ConfigurationReport report, Collection<Tag> tags) {
        if (report.getStatus() == ConfigConstants.Status.RESTART) {
            log.info("Tag created successfully, process restart required. Report \"{}\" with ID \"{}\", status \"{}\", description \"{}\", elements: ",
                    report.getName(), report.getId(), report.getStatus(), report.getStatusDescription());
            report.getElementReports().forEach(r -> log.info("ID {}, Status \"{}\", StatusMessage \"{}\".", r.getId(), r.getStatus(), r.getStatusMessage()));
        } else if (report.getStatus() != ConfigConstants.Status.OK) {
            log.info("Creation of tag {} yielded report \"{}\" with ID \"{}\", status \"{}\", description \"{}\", elements: .",
                    tags, report.getName(), report.getId(), report.getStatus(), report.getStatusDescription());
            report.getElementReports().forEach(r -> log.info("ID {}, Status \"{}\", StatusMessage \"{}\".", r.getId(), r.getStatus(), r.getStatusMessage()));
        } else {
            log.info("Tag {} has been created with report ID \"{}\", status \"{}\", description \"{}\".",
                    tags, report.getId(), report.getStatus(), report.getStatusDescription());
        }
    }

    private void deleteTags(Collection<? extends Tag> dataTags) throws DynConfigException {
        Set<Long> tagIds = dataTags.stream().map(Tag::getId).collect(Collectors.toSet());
        ConfigurationReport rep = configurationService.removeDataTagsById(tagIds);
        tagService.refresh(tagIds);
        handleConfigurationReport(Collections.singletonList(rep), DynConfigException.Context.DELETE_TAG);

    }

    private Map.Entry<Long, ConfigurationReport> createTagsAndReport(URI uri, String tagName) throws DynConfigException {
        ITagConfigStrategy strategy = ITagConfigStrategy.of(uri);
        ProcessEquipmentURIMapping mapping = findMappingFor(strategy, tagName);
        log.info("Use mapping for process ID {}, process name {}, equipment name {}", mapping.getProcessId(), mapping.getProcessName(), mapping.getEquipmentName());
        final Optional<ProcessNameResponse> processCandidate = anyRunningProcesses(mapping);
        if (!processCandidate.isPresent()) {
            log.info("No compatible process is running. Create process and equipment. ");
            createProcess(mapping);
            createEquipment(mapping, strategy);
        } else if (!isEquipmentInProcess(processCandidate.get().getProcessName(), mapping)) {
            createEquipment(mapping, strategy);
        }
        final TagConfigStrategy.TagType tagType = URIParser.getTagType(uri);
        if (tagType.equals(TagConfigStrategy.TagType.COMMAND)) {
            final CommandTag commandTag = strategy.prepareCommandTagConfigurations();
            return new AbstractMap.SimpleEntry<>(commandTag.getId(), configurationService.createCommandTag(mapping.getEquipmentName(), commandTag));
        } else {
            DataTag dataTag = strategy.prepareDataTagConfigurations();
            return new AbstractMap.SimpleEntry<>(dataTag.getId(), configurationService.createDataTag(mapping.getEquipmentName(), dataTag));
        }
    }

    private Optional<ProcessNameResponse> anyRunningProcesses(ProcessEquipmentURIMapping mapping) {
        return configurationService.getProcessNames().stream()
                .filter(response -> response.getProcessName().contains(mapping.getProcessName())).findFirst();
    }

    private boolean isEquipmentInProcess(String processName, ProcessEquipmentURIMapping mapping) {
        return configurationService.getProcessXml(processName).contains(mapping.getEquipmentName());
    }

    private void createProcess(ProcessEquipmentURIMapping mapping) {
        Process process = Process.create(mapping.getProcessName())
                .id(mapping.getProcessId())
                .description(mapping.getProcessDescription())
                .build();
        configurationService.createProcess(process);
        log.info("Process created: {}", process.getName());
    }

    private void createEquipment(ProcessEquipmentURIMapping mapping, ITagConfigStrategy strategy) throws DynConfigException {
        Equipment equipment = strategy.prepareEquipmentConfiguration(mapping);
        configurationService.createEquipment(mapping.getProcessName(), equipment);
        log.info("Equipment created: {}", equipment.getName());

    }

    private ProcessEquipmentURIMapping findMappingFor(ITagConfigStrategy strategy, String tagName) throws DynConfigException {
        return config.getMappings().stream()
                .filter(m -> strategy.matches(m.getUriPattern()))
                .findFirst()
                .orElseThrow(() -> new DynConfigException(DynConfigException.Context.NO_MAPPING, tagName));
    }

    private void handleConfigurationReport(Collection<? extends ConfigurationReport> reports, DynConfigException.Context context) throws DynConfigException {
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
