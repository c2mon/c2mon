package cern.c2mon.client.ext.dynconfig;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.core.tag.TagImpl;
import cern.c2mon.client.ext.dynconfig.config.DynConfiguration;
import cern.c2mon.client.ext.dynconfig.config.ProcessEquipmentURIMapping;
import cern.c2mon.client.ext.dynconfig.strategy.TagConfigStrategy;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.process.ProcessNameResponseImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.easymock.EasyMock.*;

class DynConfigServiceTest {

    static final URI dataTag = URI.create("opc.tcp://host:500/path?itemName=x1&namespace=1");
    static final URI commandTag = URI.create("opc.tcp://host:500/path?tagType=COMMAND&itemName=x1&namespace=1");
    final Collection<Tag> tags = Collections.singletonList(new TagImpl(1L));

    final DynConfigService dcs = new DynConfigService();
    final ConfigurationService configurationService = createMock(ConfigurationService.class);
    final TagService tagService = createMock(TagService.class);

    @BeforeEach
    void setUp() throws IOException {
        DynConfiguration config = new DynConfiguration();
        config.setMappings(loadMappings("mapping.yaml"));

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
        setupMockForDeleteWithReport(new ConfigurationReport());
        replay(tagService, configurationService);
        dcs.deleteTagForURI(dataTag);
        verify(tagService, configurationService);
    }

    @Test
    void deleteExistingTagWithBadReportShouldThrowError() {
        setupMockForDeleteWithReport(new ConfigurationReport(false, "fail"));
        replay(tagService, configurationService);
        Assertions.assertThrows(DynConfigException.class, () -> dcs.deleteTagForURI(dataTag));
        verify(tagService, configurationService);
    }

    @Test
    void getTagsForURIShouldOnlyReturnExistingTagIfFound() throws DynConfigException {
        expect(tagService.findByName(anyString()))
                .andReturn(tags)
                .once();
        replay(tagService, configurationService);
        Collection<Tag> tagsForURI = dcs.getTagsForURI(Collections.singletonList(dataTag));
        verify(tagService, configurationService);

        Assertions.assertEquals(tags, tagsForURI);
    }

    @Test
    void getTagsForURIShouldCreateTagIfNotFound() throws DynConfigException {
        setupMockForCreateTagWithReport(new ConfigurationReport(), TagConfigStrategy.TagType.Data);

        replay(tagService, configurationService);
        Collection<Tag> tagsForURI = dcs.getTagsForURI(Collections.singletonList(dataTag));
        verify(tagService, configurationService);

        Assertions.assertEquals(tags, tagsForURI);
    }

    @Test
    void getTagsForCommandURIShouldCreateTagIfNotFound() throws DynConfigException {
        setupMockForCreateTagWithReport(new ConfigurationReport(), TagConfigStrategy.TagType.Command);

        replay(tagService, configurationService);
        Collection<Tag> tagsForURI = dcs.getTagsForURI(Collections.singletonList(commandTag));
        verify(tagService, configurationService);

        Assertions.assertEquals(tags, tagsForURI);
    }

    @Test
    void getSingleTagForURIShouldReturnFirstTagFound() throws DynConfigException {
        expect(tagService.findByName(anyString()))
                .andReturn(tags)
                .once();
        replay(tagService, configurationService);
        Tag tagsForURI = dcs.getTagForURI(dataTag);
        verify(tagService, configurationService);

        Assertions.assertEquals(tags.iterator().next(), tagsForURI);
    }

    @Test
    void getSingleTagForURIShouldCreateTagIfNotFound() throws DynConfigException {
        setupMockForCreateTagWithReport(new ConfigurationReport(), TagConfigStrategy.TagType.Data);
        replay(tagService, configurationService);
        Tag tagsForURI = dcs.getTagForURI(dataTag);
        verify(tagService, configurationService);

        Assertions.assertEquals(tags.iterator().next(), tagsForURI);
    }


    @Test
    void getSingleTagForURIShouldThrowErrorWhenCreateTagReportsError() {
        setupMockForCreateTagWithReport(new ConfigurationReport(false, "fail"), TagConfigStrategy.TagType.Data);
        replay(tagService, configurationService);
        Assertions.assertThrows(DynConfigException.class, () -> dcs.getTagForURI(dataTag));
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
        expect(type.equals(TagConfigStrategy.TagType.Data) ?
                    configurationService.createDataTag(anyString(), anyObject()) :
                    configurationService.createCommandTag(anyString(), anyObject()))
                .andReturn(report)
                .anyTimes();
    }


    static List<ProcessEquipmentURIMapping> loadMappings(String name) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, ProcessEquipmentURIMapping.class);
        ClassLoader classLoader = DynConfigServiceTest.class.getClassLoader();
        String filename = classLoader.getResource(name).getFile();
        return mapper.readValue(new File(filename), listType);
    }
}