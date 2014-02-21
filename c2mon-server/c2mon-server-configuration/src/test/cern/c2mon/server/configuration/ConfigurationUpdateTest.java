/******************************************************************************
 * This file is part of the CERN Control and Monitoring (C2MON) platform.
 * 
 * See http://cern.ch/c2mon
 * 
 * Copyright (C) 2005-2014 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: C2MON team, c2mon-support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.configuration;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.configuration.impl.ConfigurationUpdateImpl;
import cern.c2mon.shared.common.datatag.DataTagConstants;


/**
* Unit test of this class only, all else are mocked.
* @author Mark Brightwell
*
*/
public class ConfigurationUpdateTest {

 /**
  * Class to test.
  */
 private ConfigurationUpdateImpl configurationUpdate;
 
 /**
  * Mocks.
  */
 private ConfigurationUpdateListener listener1;
 private ConfigurationUpdateListener listener2;
 
 private IMocksControl control = EasyMock.createNiceControl();
 
 @Before
 public void init() {        
  this.configurationUpdate = new ConfigurationUpdateImpl();
  
  //register 2 listeners
  this.listener1 = control.createMock(ConfigurationUpdateListener.class);
  this.listener2 = control.createMock(ConfigurationUpdateListener.class);
  this.configurationUpdate.registerForConfigurationUpdates(this.listener1);
  this.configurationUpdate.registerForConfigurationUpdates(this.listener2);
 }
   
 /**
  * Tests that a the ConfigurationUpdateImpl notifies
  * (2) registered listeners if it receives a
  * configuration update notification. The tag has 2 alarms
  * attached in this test.
  */
 @Test
 public void testNotifyConfigurationUpdated() {
	 // Data Tag
	 DataTag tag = new DataTagCacheObject(5L, "test NachTag", "Float", DataTagConstants.MODE_OPERATIONAL);
	 ((DataTagCacheObject)tag).setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
	 // Alarms
	 List<Long> alarmIds = new ArrayList<Long>();
	 alarmIds.add(10L);
	 alarmIds.add(20L);
	 ((DataTagCacheObject) tag).setAlarmIds(alarmIds);
	 List<Alarm> alarmList = new ArrayList<Alarm>();
	 alarmList.add(new AlarmCacheObject(10L));
	 alarmList.add(new AlarmCacheObject(20L));  
	 
	 this.listener1.notifyOnConfigurationUpdate(EasyMock.isA(Long.class));
	 EasyMock.expectLastCall().times(1);
	 this.listener2.notifyOnConfigurationUpdate(EasyMock.isA(Long.class));
	 EasyMock.expectLastCall().times(1);

	 control.replay();

	 this.configurationUpdate.notifyListeners(tag.getId());
	 
	 control.verify();
 }

}
