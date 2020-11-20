package cern.c2mon.client.core.config.dynamic;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.config.dynamic.strategy.TagConfigStrategy;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.core.tag.TagImpl;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.process.ProcessNameResponseImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static cern.c2mon.client.core.config.dynamic.C2monClientDynConfigProperties.ProcessEquipmentURIMapping;
import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

class DynConfigServiceTest {

    private static final ProcessEquipmentURIMapping dipMapping = new ProcessEquipmentURIMapping();
    private static final ProcessEquipmentURIMapping opcuaMapping = new ProcessEquipmentURIMapping();
    private static final URI dataTag = URI.create("opc.tcp://host:500/path?itemName=x1&namespace=1");
    private static final URI commandTag = URI.create("opc.tcp://host:500/path?tagType=COMMAND&itemName=x1&namespace=1");
    private final Collection<Tag> tags = Collections.singletonList(new TagImpl(1L));

    private final DynConfigService dcs = new DynConfigService();
    private final ConfigurationService configurationService = createMock(ConfigurationService.class);
    private final TagService tagService = createMock(TagService.class);

    private C2monClientDynConfigProperties config;


    static {
        dipMapping.setProcessId(1001L);
        dipMapping.setProcessName("P_DYNDIP");
        dipMapping.setEquipmentName("E_DYNDIP");
        dipMapping.setUriPattern("^dip.*");

        opcuaMapping.setProcessId(10002L);
        opcuaMapping.setProcessName("P_DYNOPCUA");
        opcuaMapping.setEquipmentName("E_DYNOPCUA");
        opcuaMapping.setUriPattern("^opc.tcp.*");
    }

    @BeforeEach
    void setUp() throws IOException {
        config = new C2monClientDynConfigProperties();

        config.setMappings(Arrays.asList(dipMapping, opcuaMapping));
        dcs.setConfigurationService(configurationService);
        dcs.setTagService(tagService);
        dcs.setConfig(config);

        expect(configurationService.getProcessXml(anyString())).andReturn("").anyTimes();
        expect(configurationService.createEquipment(anyObject(), (Equipment) anyObject())).andReturn(null).anyTimes();
    }

    @Test
    void deleteNonExistentTagShouldOnlyQueryTagServiceOnce() throws DynConfigException {
        expect(tagService.findByName(anyString()))
                .andReturn(new ArrayList<>())
                .once();
        replay(tagService, configurationService);
        dcs.deleteTagForURI(dataTag);
        verify(tagService, configurationService);
    }

    @Test
    void deleteExistingTagShouldCallRemoveAndReturnReport() throws DynConfigException {
        ConfigurationReport report = new ConfigurationReport();
        report.setStatus(ConfigConstants.Status.OK);
        setupMockForDeleteWithReport(report);
        replay(tagService, configurationService);
        dcs.deleteTagForURI(dataTag);
        verify(tagService, configurationService);
    }

    @Test
    void deleteExistingTagWithBadReportShouldThrowError() {
        ConfigurationReport report = new ConfigurationReport(false, "fail");
        report.setStatus(ConfigConstants.Status.FAILURE);
        setupMockForDeleteWithReport(report);
        replay(tagService, configurationService);
        assertThrows(DynConfigException.class, () -> dcs.deleteTagForURI(dataTag));
        verify(tagService, configurationService);
    }

    @Test
    void getTagsForURIShouldOnlyReturnExistingTagIfFound() {
        expect(tagService.findByName(anyString()))
                .andReturn(tags)
                .once();
        replay(tagService, configurationService);
        Collection<Tag> tagsForURI = dcs.getTagsForURI(Collections.singletonList(dataTag));
        verify(tagService, configurationService);

        assertEquals(tags, tagsForURI);
    }

    @Test
    void getTagsForURIShouldCreateTagIfNotFound() {
        ConfigurationReport report = new ConfigurationReport();
        report.setStatus(ConfigConstants.Status.OK);
        setupMockForCreateTagWithReport(report, TagConfigStrategy.TagType.DATA);
        replay(tagService, configurationService);
        Collection<Tag> tagsForURI = dcs.getTagsForURI(Collections.singletonList(dataTag));
        verify(tagService, configurationService);

        assertEquals(tags, tagsForURI);
    }

    @Test
    void getTagsForURIShouldFailIfNotConfigured() {
        config.setMappings(new ArrayList<>());
        expect(tagService.findByName(anyString()))
                .andReturn(new ArrayList<>())
                .anyTimes();
        replay(tagService);
        assertThrows(DynConfigException.class,
                () -> dcs.getTagForURI(dataTag),
                DynConfigException.Context.NO_MATCHING_MAPPING.message);
    }

    @Test
    void getTagsForEmptyURIsShouldSkipUris() {
        ConfigurationReport report = new ConfigurationReport();
        report.setStatus(ConfigConstants.Status.OK);
        setupMockForCreateTagWithReport(report, TagConfigStrategy.TagType.DATA);
        replay(tagService, configurationService);

        assertDoesNotThrow(() -> dcs.getTagsForURI(Arrays.asList(dataTag, null)));
    }

    @Test
    void getTagsForCommandURIShouldCreateTagIfNotFound() {
        ConfigurationReport report = new ConfigurationReport();
        report.setStatus(ConfigConstants.Status.OK);
        setupMockForCreateTagWithReport(report, TagConfigStrategy.TagType.COMMAND);

        replay(tagService, configurationService);
        Collection<Tag> tagsForURI = dcs.getTagsForURI(Collections.singletonList(commandTag));
        verify(tagService, configurationService);

        assertEquals(tags, tagsForURI);
    }

    @Test
    void getSingleTagForURIShouldReturnFirstTagFound() throws DynConfigException {
        expect(tagService.findByName(anyString()))
                .andReturn(tags)
                .once();
        replay(tagService, configurationService);
        Tag tagsForURI = dcs.getTagForURI(dataTag);
        verify(tagService, configurationService);

        assertEquals(tags.iterator().next(), tagsForURI);
    }

    @Test
    void getSingleTagForURIShouldCreateTagIfNotFound() throws DynConfigException {
        ConfigurationReport report = new ConfigurationReport();
        report.setStatus(ConfigConstants.Status.OK);
        setupMockForCreateTagWithReport(report, TagConfigStrategy.TagType.DATA);
        replay(tagService, configurationService);
        Tag tagsForURI = dcs.getTagForURI(dataTag);
        verify(tagService, configurationService);

        assertEquals(tags.iterator().next(), tagsForURI);
    }


    @Test
    void getSingleTagForURIShouldThrowErrorWhenCreateTagReportsError() {
        ConfigurationReport report = new ConfigurationReport(false, "fail");
        report.setStatus(ConfigConstants.Status.FAILURE);
        setupMockForCreateTagWithReport(report, TagConfigStrategy.TagType.DATA);
        replay(tagService, configurationService);
        assertThrows(DynConfigException.class, () -> dcs.getTagForURI(dataTag));
        verify(tagService, configurationService);
    }

    private void setupMockForDeleteWithReport(ConfigurationReport report) {
        Collection<Tag> tags = Collections.singletonList(new TagImpl(1L));
        expect(tagService.findByName(anyString()))
                .andReturn(tags)
                .once();
        expect(configurationService.removeDataTagsById(anyObject()))
                .andReturn(report)
                .once();
        tagService.refresh(anyObject());
        expectLastCall();
    }

    private void setupMockForCreateTagWithReport(ConfigurationReport report, TagConfigStrategy.TagType type) {
        Collection<ProcessNameResponse> processNames = Arrays.asList(new ProcessNameResponseImpl("P_DYNDIP"), new ProcessNameResponseImpl("P_DYNOPCUA"));
        expect(tagService.findByName(anyString()))
                .andReturn(new ArrayList<>())
                .once();
        expect(configurationService.getProcessNames())
                .andReturn(processNames)
                .anyTimes();
        expect(tagService.findByName(anyString()))
                .andReturn(tags)
                .once();
        expect(type.equals(TagConfigStrategy.TagType.DATA) ?
                configurationService.createDataTag(anyString(), anyObject()) :
                configurationService.createCommandTag(anyString(), anyObject()))
                .andReturn(report)
                .anyTimes();
    }
}