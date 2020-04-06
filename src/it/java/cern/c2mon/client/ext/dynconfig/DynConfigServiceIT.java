package cern.c2mon.client.ext.dynconfig;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.ext.dynconfig.config.DynConfiguration;
import cern.c2mon.client.ext.dynconfig.factories.ProtocolSpecificFactory;
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
@SpringBootTest(classes = {DynConfigService.class, DynConfiguration.class})
@TestPropertySource(locations = "classpath:mapping.properties")
@RunWith(SpringRunner.class)
public class DynConfigServiceIT {

	private static final URI uri = URI.create("dip://dip/acc/LHC/RunControl/Page1?itemName=Page1");

	@Autowired
	private TagService ts;

	@Autowired
	private ConfigurationService cs;

	@Autowired
	private DynConfigService dcs;

	@BeforeClass
	public static void setupServer() {
		GenericContainer<?> c2mon = new GenericContainer("cern/c2mon:1.9.2")
				.waitingFor(Wait.forListeningPort())
				.withNetworkMode("host");
		c2mon.start();
		log.info("C2MON is starting... ");

		//System.setProperty("c2mon.client.conf.url", "classpath:c2mon-dynconfig-client.yml");
		//System.setProperty("c2mon.client.jms.url", "http://dash.web.cern.ch");
		C2monServiceGateway.startC2monClientSynchronous();
	}

	@After
	public void clean() throws DynConfigException {
		dcs.deleteTagForURI(uri);
	}

	@Test
	public void getTagForURIWithUnknownTagShouldCreateTag() throws Exception {
		String tagName = ProtocolSpecificFactory.of(uri).createQueryObj().getTagName();

		Assert.assertTrue(ts.findByName(tagName).isEmpty());
		dcs.getTagForURI(uri);
		Assert.assertFalse(ts.findByName(tagName).isEmpty());
	}

	@Test
	public void getTagForURIWithUnknownTagShouldReturnCreatedTag() throws Exception {
		Tag tag = dcs.getTagForURI(uri);
		Assert.assertEquals(Collections.singletonList(tag), ts.findByName(ProtocolSpecificFactory.of(uri).createQueryObj().getTagName()));
	}

	@Test
	public void deleteTagForURIWithExistingTagShouldRemoveTag() throws Exception {
		dcs.getTagForURI(uri);
		dcs.deleteTagForURI(uri);
		Assert.assertTrue(ts.findByName(ProtocolSpecificFactory.of(uri).createQueryObj().getTagName()).isEmpty());
	}

	@Test
	public void deleteTagForURIWithoutTagShouldDoNothing() throws Exception {
		dcs.deleteTagForURI(uri);
		Assert.assertTrue(ts.findByName(ProtocolSpecificFactory.of(uri).createQueryObj().getTagName()).isEmpty());
	}
}
