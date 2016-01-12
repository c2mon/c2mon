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
package cern.c2mon.server.laser.publication;

import org.easymock.classextension.EasyMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.c2mon.server.common.alarm.Alarm;

/**
 * @author felixehm
 *
 */
public class StatisticsModuleTest {

	@Test
	public void testAddAndUpdate(){
		StatisticsModule stats = new StatisticsModule();
		
		Alarm alarm = EasyMock.createMock(Alarm.class);
		expect(alarm.getFaultFamily()).andReturn("TEST-FAMILY").atLeastOnce();
		expect(alarm.getFaultMember()).andReturn("TEST-MEMBER").atLeastOnce();
		expect(alarm.getFaultCode()).andReturn(1).atLeastOnce();
		replay(alarm);
		
		stats.update(alarm);
		verify(alarm);
		// total counter is 1 
		assertEquals(1, stats.getTotalProcessed());
		System.out.println(stats.getStatsList());
		
		// name is stored correctly  
		assertTrue("TEST-FAMILY:TEST-MEMBER:1".equals(stats.getStatsForAlarm("TEST-FAMILY:TEST-MEMBER:1").getName()));
		
		// counter for Alarm is 1 
		assertEquals(1, stats.getStatsForAlarm("TEST-FAMILY:TEST-MEMBER:1").getProcessedTimes());
		
		// reset for this alarm
		stats.resetStatistics("TEST-FAMILY:TEST-MEMBER:1");
		assertEquals(0, stats.getStatsForAlarm("TEST-FAMILY:TEST-MEMBER:1").getProcessedTimes());
		assertEquals(1, stats.getTotalProcessed());
		stats.resetStatistics();
		assertEquals(0, stats.getTotalProcessed());
	}
	
	
	
}
