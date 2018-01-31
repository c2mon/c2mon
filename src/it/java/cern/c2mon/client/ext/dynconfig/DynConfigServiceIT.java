package cern.c2mon.client.ext.dynconfig;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.ext.dynconfig.configuration.DynConfigConfiguration;
import cern.c2mon.client.ext.dynconfig.configuration.ProcessEquipmentURIMapping;
import cern.c2mon.client.ext.dynconfig.strategy.DipConfigStrategy;
import cern.c2mon.client.ext.dynconfig.strategy.ITagConfigurationStrategy;

public class DynConfigServiceIT {
	@Test
	public void testDynConfigService() throws Exception {
		//System.setProperty("c2mon.client.conf.url", "classpath:c2mon-dynconfig-client.properties");
		//System.setProperty("c2mon.client.jms.url", "http://dash.web.cern.ch");
		System.setProperty("c2mon.client.jms.url", "tcp://localhost:61616");
		System.setProperty("c2mon.dynconfig.component.active", "false");
		C2monServiceGateway.startC2monClientSynchronous();
		
		DynConfigService dcs = new DynConfigService();
		dcs.setConfigurationService(C2monServiceGateway.getConfigurationService());
		dcs.setTagService(C2monServiceGateway.getTagService());
		
		dcs.config = new DynConfigConfiguration();
		List<ProcessEquipmentURIMapping> mappings = new ArrayList<>();
		
		mappings.add(ProcessEquipmentURIMapping.builder().uriPattern("^dip.*")
				.processId(10001L).processName("P_DYNDIP")
				.equipmentName("E_DYNDIP").build());
		
		dcs.config.setMappings(mappings);
		
		DipConfigStrategy dipCS = new DipConfigStrategy();
		
		dcs.setConfigurationStrategies(Arrays.asList(new ITagConfigurationStrategy[]{dipCS}));
		
//		Tag tag = dcs.getTagForURI(new URI("dip://dip/acc/LHC/RunControl/Page1"));
//		Tag tag = dcs.getTagForURI(new URI(SupportedProtocolsEnum.PROTOCOL_OPCUA+"://pitrafficlight/GreenLED.on"));
		URI uri = new URI("dip://dip/acc/LHC/RunControl/Page1");
		Tag tag = dcs.getTagForURI(uri);
		
		TagService ts = C2monServiceGateway.getTagService();
        //ts.subscribe(100000L, new TagListener() {
        ts.subscribe(tag.getId(), new TagListener() {

			@Override
			public void onUpdate(Tag tagUpdate) {
				onInitialUpdate(Arrays.asList(new Tag[] { tagUpdate }));
			}

			@Override
			public void onInitialUpdate(Collection<Tag> initialValues) {
				for (Tag tagUpdate : initialValues) {
					System.out.println(String.format("received update for %s : %s", tagUpdate.getName(), tagUpdate.getValue()));
					
				}

			} 
		});
		
		

		System.out.println("Waiting for updates...");
		Thread.sleep(3000l);
		
		
		System.out.println("Deleting the tag now ...");
		dcs.deleteTagForURI(uri);
		
		assertTrue(ts.findByName(SupportedProtocolsEnum.convertToTagName(uri)).isEmpty());
		
		System.out.println("Done.");
	}

	

}
