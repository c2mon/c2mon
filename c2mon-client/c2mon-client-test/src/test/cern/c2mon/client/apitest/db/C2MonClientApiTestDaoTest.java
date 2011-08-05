package cern.c2mon.client.apitest.db;

import static org.junit.Assert.assertEquals;
import java.util.List;

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
	public void testGetAllDeviceRuleMetrics() throws Exception {

		List<MetricDef> def = dao.getAllDeviceRuleMetrics();

		assertEquals(2, def.size());
		
		assertEquals(0, def.get(0).getMetricTagId());
		assertEquals("TESTDEVICE1:STATUS", def.get(0).getMetricName());
		assertEquals(100L, def.get(0).getRuleTagId());
		
		assertEquals(0, def.get(1).getMetricTagId());
		assertEquals("TESTDEVICE2:STATUS", def.get(1).getMetricName());
		assertEquals(222, def.get(1).getRuleTagId());
	}
	
	
	@Test
    public void testGetAllSimpleRuleMetrics() throws Exception {

        List<MetricDef> def = dao.getAllSimpleRuleMetrics();

        assertEquals(2, def.size());
        
        assertEquals(10002L, def.get(0).getMetricTagId());
        assertEquals("DIAMON.CLIC.CS-CCR-DEV2/Acquisition/sys.kern.activeproc", def.get(0).getMetricName());
        assertEquals(188864L, def.get(0).getRuleTagId());
        
        assertEquals(10001L, def.get(1).getMetricTagId());
        assertEquals("DIAMON.CLIC.CS-CCR-DEV2/Acquisition/ntp.avg", def.get(1).getMetricName());
        assertEquals(188866L, def.get(1).getRuleTagId());
                
    }	
	

    @Test
    public void getAllMetrics() throws Exception {

        List<MetricDef> def = dao.getAllMetrics();

        assertEquals(3, def.size());                        
    }   	
    
    @Test
    public void testGetDeviceRuleMetric() throws Exception {
        MetricDef def = dao.getDeviceRuleMetric("P_CLIC01");        
        assertEquals(100,def.getRuleTagId());

        def = dao.getDeviceRuleMetric("P_CLIC02");        
        assertEquals(222,def.getRuleTagId());

    }
    
}
