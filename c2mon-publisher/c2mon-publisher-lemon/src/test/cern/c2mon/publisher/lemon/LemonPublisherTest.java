/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */

package cern.c2mon.publisher.lemon;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URL;
import java.sql.Timestamp;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jmx.support.MetricType;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.tim.shared.common.datatag.DataTagQuality;

/**
 * This class publishes JUnit test for LemonPublisher
 * 
 * @author Peter Jurcso
 */

public class LemonPublisherTest
{

	LemonPublisher publisher;
	DataTagQuality qualityValid, qualityInvalid;
	ClientDataTagValue cdt, cdt1, cdt2, cdt3, cdt4, cdt5, cdt6, cdt7;
	TagConfig cdtConfig, cdtConfig1;
	Timestamp ts, ts1, ts2, ts3, ts4, ts5, ts6, ts7;

	@Before
	public void setUp() throws Exception
	{

		publisher = new LemonPublisher();
		cdt = EasyMock.createMock(ClientDataTagValue.class);
		cdt1 = EasyMock.createMock(ClientDataTagValue.class);
		cdt2 = EasyMock.createMock(ClientDataTagValue.class);
		cdt3 = EasyMock.createMock(ClientDataTagValue.class);
		cdt4 = EasyMock.createMock(ClientDataTagValue.class); // invalid
		cdt5 = EasyMock.createMock(ClientDataTagValue.class);
		cdt6 = EasyMock.createMock(ClientDataTagValue.class);
		cdt7 = EasyMock.createMock(ClientDataTagValue.class);

		cdtConfig = EasyMock.createMock(TagConfig.class);
		qualityValid = EasyMock.createMock(DataTagQuality.class);
		cdtConfig1 = EasyMock.createMock(TagConfig.class);
		qualityInvalid = EasyMock.createMock(DataTagQuality.class);

	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testMapsInit()
	{

		try
		{
			System.out.println("************ Init testMapsInit()");
			publisher.loadLemonTemplate(LemonPublisherTest.class.getResource("lemonids.template"));
			assertEquals(14, publisher.lemonId2Metric.keySet().size());

		} catch (Exception ex)
		{
			fail("Execption not expected ");
		}

		try
		{

			publisher.loadLemonTemplate(new URL("http://lemonids.template"));
			fail("It should fail");

		} catch (Exception ex)
		{
			;
		}

	}

	@Test
	public void testStaticLoader()
	{

		try
		{

			publisher.loadLemonStaticData(LemonPublisherTest.class.getResource("static.data"));
			assertEquals(54, publisher.metricReceived.keySet().size());
			System.out.println("metricReceived: " + publisher.metricReceived);
		} catch (Exception ex)
		{
			fail("Execption not expected");
		}

	}

	@Test
	public void testMapsSender()
	{

		try
		{

			// Initialize template
			publisher.loadLemonTemplate(LemonPublisherTest.class.getResource("lemonids.template"));

			// Initialize static data
			publisher.loadLemonStaticData(LemonPublisherTest.class.getResource("static.data"));

			// Cut collection period the reduce the runtime of the test
			publisher.UPDATE_RECEIVING_PERIOD_SEC = 3;

			// Not generating real network traffic
			publisher.LEMON_SERVER_NONET = 1;

			// Prepare valid update quality
			expect(qualityValid.isValid()).andReturn(true).anyTimes();

			// Prepare invalid update quality
			expect(qualityInvalid.isValid()).andReturn(false).anyTimes();

			// Update 1
			ts = new Timestamp(System.currentTimeMillis());
			expect(cdt.getName()).andReturn("CLIC:CS-CCR-TEST:SYS.KERN.UPTIME").anyTimes();
			expect(cdt.getValue()).andReturn(111).anyTimes();
			expect(cdt.getTimestamp()).andReturn(ts);
			ts = new Timestamp(System.currentTimeMillis() + 15000);
			expect(cdt.getTimestamp()).andReturn(ts).anyTimes();

			expect(cdt.getDataTagQuality()).andReturn(qualityValid).anyTimes();

			// Update 2
			ts1 = new Timestamp(System.currentTimeMillis());
			expect(cdt1.getName()).andReturn("CLIC:CS-CCR-PROB:SYS.KERN.UPTIME").anyTimes();
			expect(cdt1.getValue()).andReturn(222).anyTimes();
			expect(cdt1.getTimestamp()).andReturn(ts1).anyTimes();
			expect(cdt1.getDataTagQuality()).andReturn(qualityValid).anyTimes();

			// Update 3
			ts2 = new Timestamp(System.currentTimeMillis());
			expect(cdt2.getName()).andReturn("CLIC:CS-CCR-PROB:SYS.NET.OUT").anyTimes();
			expect(cdt2.getValue()).andReturn(333.0).anyTimes();
			expect(cdt2.getTimestamp()).andReturn(ts2).anyTimes();
			expect(cdt2.getDataTagQuality()).andReturn(qualityValid).anyTimes();

			// Update 4
			ts3 = new Timestamp(System.currentTimeMillis());
			expect(cdt3.getName()).andReturn("CLIC:CS-CCR-PROB:SYS.NET.IN").anyTimes();
			expect(cdt3.getValue()).andReturn(444).anyTimes();
			expect(cdt3.getTimestamp()).andReturn(ts3).anyTimes();
			expect(cdt3.getDataTagQuality()).andReturn(qualityValid).anyTimes();

			// Update 4

			expect(cdt4.getDataTagQuality()).andReturn(qualityInvalid).anyTimes();

			// Update 5
			ts5 = new Timestamp(System.currentTimeMillis());
			expect(cdt5.getName()).andReturn("CLIC:CS-CCR-TEST:SYS.KERN.ACTIVEPROC").anyTimes();
			expect(cdt5.getValue()).andReturn(555).anyTimes();
			expect(cdt5.getTimestamp()).andReturn(ts5).anyTimes();
			expect(cdt5.getDataTagQuality()).andReturn(qualityValid).anyTimes();

			// Update 6
			ts6 = new Timestamp(System.currentTimeMillis());
			expect(cdt6.getName()).andReturn("CLIC:CS-CCR-DEMO:SYS.KERN.DOWNTIME").anyTimes();
			expect(cdt6.getValue()).andReturn(999).anyTimes();
			expect(cdt6.getTimestamp()).andReturn(ts6).anyTimes();
			expect(cdt6.getDataTagQuality()).andReturn(qualityValid).anyTimes();

			// Update 7
			ts7 = new Timestamp(System.currentTimeMillis() + 5000);
			expect(cdt7.getName()).andReturn("CLIC:CS-CCR-TEST:SYS.KERN.DOWNTIME").anyTimes();
			expect(cdt7.getValue()).andReturn(90909).anyTimes();
			expect(cdt7.getTimestamp()).andReturn(ts7).anyTimes();
			expect(cdt7.getDataTagQuality()).andReturn(qualityValid).anyTimes();

			replay(cdt, cdt1, cdt2, cdt3, cdt4, cdt5, cdt6, cdt7, qualityValid, qualityInvalid);

			publisher.onUpdate(cdt, null);

			publisher.onUpdate(cdt1, null);

			publisher.onUpdate(cdt4, null);

			publisher.onUpdate(cdt2, null);

			publisher.onUpdate(cdt1, null);

			publisher.onUpdate(cdt2, null);

			publisher.onUpdate(cdt3, null);

			publisher.onUpdate(cdt5, null);

			publisher.onUpdate(cdt6, null);

			publisher.onUpdate(cdt7, null);

			Thread.sleep(3000);

			System.out.print("\n\n**************** NEXT ITERATION ****************\n\n");

			publisher.onUpdate(cdt5, null);

			Thread.sleep(6000);

			verify(cdt, cdt1, cdt2, cdt3, cdt4, qualityValid);

		} catch (Exception ex)
		{
			ex.printStackTrace();
			fail("Execption not expected");
		}

	}

}
