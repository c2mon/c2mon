package cern.c2mon.client.apitest;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.apitest.MetricDef;
import cern.c2mon.client.apitest.service.C2MonClientApiTestService;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:application-context-test.xml" })
public class C2MonClientApiTestServiceImplTest {

	@Autowired
	C2MonClientApiTestService service;

	@Test
	public void getAllDeviceRuleMetricsForProcess() throws Exception {

		List<MetricDef> def = service.getProcessMetrics("P_CLIC_01");

		assertEquals(2, def.size());
		
		//assertEquals("TESTDEVICE1:STATUS", def.get(0).getName());
		//assertEquals(100, def.get(0).getRuleTagId());
		
		//assertEquals("TESTDEVICE2:STATUS", def.get(1).getName());
		//assertEquals(222, def.get(1).getRuleTagId());
	}
}
