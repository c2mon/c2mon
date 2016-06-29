package cern.c2mon.client.ext.dynconfig;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.TagService;

public class DynConfigServiceIT {
	@Test
	public void testDynConfigService() throws Exception {
		System.setProperty("c2mon.client.conf.url", "classpath:c2mon-dynconfig-client.properties");
		C2monServiceGateway.startC2monClientSynchronous();
		DynConfigService dcs = new DynConfigService();
		dcs.setConfigurationService(C2monServiceGateway.getConfigurationService());
		dcs.setTagService(C2monServiceGateway.getTagService());

		dcs.init();

//		Tag tag = dcs.getTagForURI(new URI("dip://dip/acc/LHC/RunControl/Page1"));
//		Tag tag = dcs.getTagForURI(new URI(SupportedProtocolsEnum.PROTOCOL_OPCUA+"://pitrafficlight/GreenLED.on"));
		Tag tag = dcs.getTagForURI(new URI("dip://dip/acc/LHC/RunControl/Page1"));
		
		TagService ts = C2monServiceGateway.getTagService();
		ts.subscribe(100000L, new TagListener() {
//        ts.subscribe(tag.getId(), new TagListener() {

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
		Thread.sleep(10000l);
		System.out.println("Done.");
	}

	

}
