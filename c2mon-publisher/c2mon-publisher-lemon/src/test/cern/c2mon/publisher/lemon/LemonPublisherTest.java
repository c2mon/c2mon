/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */

package cern.c2mon.publisher.lemon;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;

import java.net.URL;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.TagQualityStatus;

/**
 * This class publishes JUnit test for LemonPublisher
 * 
 * @author Peter Jurcso
 */

public class LemonPublisherTest {

	LemonPublisher publisher;
	DataTagQuality quality, quality1;
	ClientDataTagValue cdt, cdt1, cdt2, cdt3, cdt4;
	TagConfig cdtConfig, cdtConfig1;

	@Before
	public void setUp() throws Exception {

		publisher = new LemonPublisher();
		cdt = EasyMock.createMock(ClientDataTagValue.class);
		cdt1 = EasyMock.createMock(ClientDataTagValue.class);
		cdt2 = EasyMock.createMock(ClientDataTagValue.class);
		cdt3 = EasyMock.createMock(ClientDataTagValue.class);
		cdt4 = EasyMock.createMock(ClientDataTagValue.class); // This will be
																// invalid
																// update

		cdtConfig = EasyMock.createMock(TagConfig.class);
		quality = EasyMock.createMock(DataTagQuality.class);
		cdtConfig1 = EasyMock.createMock(TagConfig.class);
		quality1 = EasyMock.createMock(DataTagQuality.class);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMapsInit() {

		try {

			publisher.loadLemonTemplate(LemonPublisherTest.class
					.getResource("lemonids.template"));
			assertEquals(3, publisher.lemonId2Metric.keySet().size());
			assertEquals(5, publisher.diamonMetric2LemonId.keySet().size());

		} catch (Exception ex) {
			fail("Execption not expected");
		}

		try {

			publisher.loadLemonTemplate(new URL("http://lemonids.template"));
			fail("It should fail");

		} catch (Exception ex) {
			;
		}

	}

	@Test
	public void testMapsSender() {

		try {
			publisher.UPDATE_RECEIVING_PERIOD_SEC = 2;
			expect(cdt.getDataTagQuality()).andReturn(quality).anyTimes();
			;
			expect(quality.isValid()).andReturn(true).anyTimes();

			expect(cdt4.getDataTagQuality()).andReturn(quality1).anyTimes();
			;
			expect(quality1.isValid()).andReturn(false).anyTimes();

			expect(cdt.getName()).andReturn("CLIC:CS-CCR-TEST:SYS.KERN.UPTIME")
					.anyTimes();
			;

			expect(cdt1.getDataTagQuality()).andReturn(quality).anyTimes();
			;
			expect(cdt1.getName())
					.andReturn("CLIC:CS-CCR-PROB:SYS.KERN.UPTIME").anyTimes();
			;

			expect(cdt2.getDataTagQuality()).andReturn(quality).anyTimes();
			;
			expect(cdt2.getName()).andReturn(
					"CLIC:CS-CCR-PROB:SYS.KERN.DOWNTIME").anyTimes();
			;

			expect(cdt3.getDataTagQuality()).andReturn(quality).anyTimes();
			;
			expect(cdt3.getName()).andReturn(
					"CLIC:CS-CCR-TEST:SYS.KERN.DOWNUPTIME").anyTimes();
			;

			replay(cdt, cdt1, cdt2, cdt3, cdt4, quality, quality1);
			publisher.onUpdate(cdt, null);

			publisher.onUpdate(cdt1, null);

			publisher.onUpdate(cdt2, null);

			publisher.onUpdate(cdt3, null);

			Thread.sleep(5000);
			publisher.onUpdate(cdt, null);

			publisher.onUpdate(cdt1, null);

			publisher.onUpdate(cdt4, null);

			publisher.onUpdate(cdt2, null);

			publisher.onUpdate(cdt1, null);

			publisher.onUpdate(cdt2, null);

			publisher.onUpdate(cdt3, null);

			Thread.sleep(5000);

			verify(cdt, quality);

		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Execption not expected");
		}

	}

}
