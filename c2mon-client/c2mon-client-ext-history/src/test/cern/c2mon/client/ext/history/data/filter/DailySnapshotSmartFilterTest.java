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
package cern.c2mon.client.ext.history.data.filter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.Timespan;
import cern.c2mon.client.ext.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.shared.client.tag.TagMode;

/**
 * Tests the {@link DailySnapshotSmartFilter} class
 * 
 * @author vdeila
 * 
 */
public class DailySnapshotSmartFilterTest {

  private static final Long TAG_ID = 12345L;
  
  @Before
  public void setUp() throws Exception {
  }
  
  private HistoryTagValueUpdate create(
      final Object value, 
      final Timestamp logTimestamp,
      final Timestamp serverTimestamp) {
    return new HistoryTagValueUpdateImpl(TAG_ID, null, value, serverTimestamp, serverTimestamp,
        serverTimestamp, logTimestamp, "Test tag", TagMode.OPERATIONAL);
  }
  
  private Timestamp time(int day, int hour, int minute) {
    return new Timestamp(new GregorianCalendar(2011, 06, day, hour, minute).getTime().getTime());
  }

  @After
  public void tearDown() throws Exception {
  }
  
  @Test
  public void testCanSkipDay() {
    double value = 1.0;
    final Collection<HistoryTagValueUpdate> mixedSkippingDaysValues = new ArrayList<HistoryTagValueUpdate>(Arrays.asList(
        create(value++, time(9, 00, 00),  time(8, 21, 00)),
        create(value++, time(10, 00, 00), time(8, 21, 00)),
        create(value++, time(11, 00, 00), time(8, 21, 00)),
        create(value++, time(12, 00, 00), time(8, 21, 00)),
        create(value++, time(13, 00, 00), time(8, 21, 00)),
        create(value++, time(14, 00, 00), time(8, 21, 00)),
        
        create(value++, time(15, 00, 00), time(14, 22, 00))
    ));
    
    final DailySnapshotSmartFilter filter = new DailySnapshotSmartFilter(mixedSkippingDaysValues);

    Assert.assertNull(filter.getTimespan(TAG_ID, time(7, 10, 00)));
    Assert.assertNull(filter.getTimespan(TAG_ID, time(8, 5, 00)));
    Assert.assertNull(filter.getTimespan(TAG_ID, time(8, 10, 00)));
    Assert.assertNull(filter.getTimespan(TAG_ID, time(8, 20, 00)));
    
    Assert.assertNotNull(filter.getTimespan(TAG_ID, time(8, 21, 00)));
    Assert.assertNotNull(filter.getTimespan(TAG_ID, time(9, 5, 00)));
    Assert.assertNotNull(filter.getTimespan(TAG_ID, time(9, 16, 59)));
    Assert.assertNotNull(filter.getTimespan(TAG_ID, time(9, 20, 00)));
    
    Assert.assertNotNull(filter.getTimespan(TAG_ID, time(9, 21, 00)));
    Assert.assertNotNull(filter.getTimespan(TAG_ID, time(10, 00, 00)));
    Assert.assertNotNull(filter.getTimespan(TAG_ID, time(10, 23, 00)));
    Assert.assertNotNull(filter.getTimespan(TAG_ID, time(12, 10, 00)));
    Assert.assertNotNull(filter.getTimespan(TAG_ID, time(13, 12, 00)));
    
    Assert.assertNull(filter.getTimespan(TAG_ID, time(14, 12, 00)));
    Assert.assertNull(filter.getTimespan(TAG_ID, time(14, 21, 00)));
    
    
    Timespan timespan = filter.getTimespan(TAG_ID, time(10, 21, 00));
    Assert.assertEquals(time(8, 21, 00), timespan.getStart());
    Assert.assertEquals(time(14, 00, 00), timespan.getEnd());
  }

}












