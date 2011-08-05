package cern.c2mon.client.apitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.apitest.MetricDef;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:application-context-test.xml" })
public class C2MonClientApiTestServiceImplTest {

	@Autowired
	C2MonClientApiTestService service;

	@Test
	public void testGetAllMetrics() throws Exception {

		List<MetricDef> def = service.getAllMetrics();

		assertEquals(2, def.size());
		
		assertEquals("TESTDEVICE1", def.get(0).getEquipmentName());
		assertEquals(100, def.get(0).getEquipmentRuleTag());
		
		assertEquals("TESTDEVICE2", def.get(1).getEquipmentName());
		assertEquals(222, def.get(1).getEquipmentRuleTag());
	}
}
