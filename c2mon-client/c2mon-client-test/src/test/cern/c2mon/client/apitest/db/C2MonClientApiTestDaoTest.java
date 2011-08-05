package cern.c2mon.client.apitest.db;

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
import cern.c2mon.client.apitest.db.C2MonClientApiTestDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:application-context-test.xml" })
public class C2MonClientApiTestDaoTest {

	@Autowired
	C2MonClientApiTestDao dao;

	@Test
	public void testGetAllMetrics() throws Exception {

		List<MetricDef> def = dao.getAllMetrics();

		assertEquals(2, def.size());
		
		assertEquals("TESTDEVICE1:STATUS", def.get(0).getMetricName());
		assertEquals(100, def.get(0).getEquipmentRuleTag());
		
		assertEquals("TESTDEVICE2:STATUS", def.get(1).getMetricName());
		assertEquals(222, def.get(1).getEquipmentRuleTag());
	}
}
