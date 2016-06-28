/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.apitest.db;

import static org.junit.Assert.assertEquals;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.apitest.CommandDef;
import cern.c2mon.client.apitest.EquipmentDef;
import cern.c2mon.client.apitest.MetricDef;
import cern.c2mon.client.apitest.db.C2MonClientApiTestDao;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration({ "classpath:resources/application-context-test.xml" })
public class C2MonClientApiTestDaoTest {

	@Autowired
	C2MonClientApiTestDao dao;

    
    //@Test
    public void testGetProcessMetrics() throws Exception {
        List<MetricDef> defs = dao.getProcessMetrics("P_CLIC_01");        
        
        assertEquals(2,defs.size());
        
        List<MetricDef> defs2 = dao.getProcessMetrics("P_CLIC_02");
        assertEquals(1,defs2.size());
        
        MetricDef md = defs2.get(0);
        
        assertEquals(0,md.getRuleTagId());
        assertEquals(10003, md.getMetricTagId());
        assertEquals("CLIC:CS-CCR-DEV2:METRIC3", md.getName());               
        assertEquals("METRIC3", md.getDisplayName());
        assertEquals(1, md.getTestId());        
        assertEquals("Integer", md.getType());
        assertEquals("description metric3", md.getDescription());        
    }
    

    //@Test
    public void testGetEquipmentMetrics() throws Exception {
        List<MetricDef> defs = dao.getEquipmentMetrics("TESTDEVICE1");        
        
        assertEquals(2,defs.size());
        
        List<MetricDef> defs2 = dao.getEquipmentMetrics("TESTDEVICE3");
        assertEquals(1,defs2.size());
        
        MetricDef md = defs2.get(0);
        
        assertEquals(0,md.getRuleTagId());
        assertEquals(10003, md.getMetricTagId());
        assertEquals("CLIC:CS-CCR-DEV2:METRIC3", md.getName());               
        assertEquals("METRIC3", md.getDisplayName());
        assertEquals(1, md.getTestId());        
        assertEquals("Integer", md.getType());
        assertEquals("description metric3", md.getDescription());        
    }
    
    
    //@Test
    public void testGetEquipments() throws Exception {
        List<EquipmentDef> defs = dao.getEquipments("P_CLIC_01","P_CLIC_02");                
        assertEquals(3,defs.size());
        
        List<EquipmentDef> defs2 = dao.getEquipments("P_CLIC_02");
        assertEquals(1,defs2.size());
        
        EquipmentDef ed = defs2.get(0);
        assertEquals("TESTDEVICE3",ed.getName());
        assertEquals("CLIC",ed.getType());
        assertEquals(223L,ed.getRuleTagId());                
    }
    
    
    //@Test
    public void getRegisteredCommands() throws Exception {
        List<CommandDef> defs = dao.getRegisteredCommands("cs-ccr-dev1");                        
        assertEquals(2,defs.size());
        
        //List<EquipmentDef> defs2 = dao.getEquipments("P_CLIC_02");
        //assertEquals(1,defs2.size());
        
        //EquipmentDef ed = defs2.get(0);
        //assertEquals("TESTDEVICE3",ed.getName());
        //assertEquals("CLIC",ed.getType());
        //assertEquals(223L,ed.getRuleTagId());                
    }    
    
}
