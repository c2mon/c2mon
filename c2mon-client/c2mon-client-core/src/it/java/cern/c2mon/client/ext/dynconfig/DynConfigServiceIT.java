package cern.c2mon.client.ext.dynconfig;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.config.C2monAutoConfiguration;
import cern.c2mon.client.core.config.C2monClientDynConfigProperties;
import cern.c2mon.client.core.config.dynamic.DynConfigException;
import cern.c2mon.client.core.config.dynamic.DynConfigService;
import cern.c2mon.client.core.config.dynamic.URIParser;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.net.URI;
import java.util.Collections;

@Slf4j
@SpringBootTest(classes = {C2monAutoConfiguration.class, DynConfigService.class})
@TestPropertySource(properties = {"c2mon.client.dynconfig.active=true"},
        locations = {"classpath:test-mapping.properties", "classpath:c2mon-client-default.properties"})
@RunWith(SpringRunner.class)
public class DynConfigServiceIT {

    private static final URI dipTagUri = URI.create("dip://dip/acc/LHC/RunControl/Page1?publicationName=Page1");
    private static final URI opcCommandUri = URI.create("opc.tcp://dip?tagType=COMMAND&commandPulseLength=2&setCommandType=CLASSIC&itemName=test");
    private static final int JMS_PORT = 61616;

    @Autowired
    private TagService ts;

    @Autowired
    private ConfigurationService cs;

    @Autowired
    private DynConfigService dcs;

    @BeforeClass
    public static void setupServer() {
        GenericContainer<?> c2mon = new GenericContainer<>("cern/c2mon:1.9.2")
                .waitingFor(Wait.forListeningPort())
                .withExposedPorts(JMS_PORT);
        c2mon.start();
        log.info("C2MON is starting... ");

        System.setProperty("c2mon.client.jms.url", "tcp://" + c2mon.getContainerIpAddress() + ":" + c2mon.getMappedPort(JMS_PORT));
        C2monServiceGateway.startC2monClientSynchronous();
    }

    @After
    public void clean() throws DynConfigException {
        dcs.deleteTagForURI(dipTagUri);
    }

    @Ignore
    public void getCommandTagForURIWithUnknownTagShouldCreateTag() throws Exception {
        String tagName = URIParser.toTagName(opcCommandUri);

        Assert.assertTrue(ts.findByName(tagName).isEmpty());
        dcs.getTagForURI(opcCommandUri);
        Assert.assertFalse(ts.findByName(tagName).isEmpty());
    }

    @Test
    public void getTagForURIWithUnknownTagShouldCreateTag() throws Exception {
        String tagName = URIParser.toTagName(dipTagUri);

        Assert.assertTrue(ts.findByName(tagName).isEmpty());
        dcs.getTagForURI(dipTagUri);
        Assert.assertFalse(ts.findByName(tagName).isEmpty());
    }

    @Test
    public void getTagForURIWithUnknownTagShouldReturnCreatedTag() throws Exception {
        Tag tag = dcs.getTagForURI(dipTagUri);
        Assert.assertEquals(Collections.singletonList(tag), ts.findByName(URIParser.toTagName(dipTagUri)));
    }

    @Test
    public void deleteTagForURIWithExistingTagShouldRemoveTag() throws Exception {
        dcs.getTagForURI(dipTagUri);
        dcs.deleteTagForURI(dipTagUri);
        Assert.assertTrue(ts.findByName(URIParser.toTagName(dipTagUri)).isEmpty());
    }

    @Test
    public void deleteTagForURIWithoutTagShouldDoNothing() throws Exception {
        dcs.deleteTagForURI(dipTagUri);
        Assert.assertTrue(ts.findByName(URIParser.toTagName(dipTagUri)).isEmpty());
    }
}
