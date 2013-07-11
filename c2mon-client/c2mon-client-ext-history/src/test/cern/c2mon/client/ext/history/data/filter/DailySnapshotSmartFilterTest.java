/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
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
import cern.c2mon.client.ext.history.data.filter.DailySnapshotSmartFilter;
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
    return new HistoryTagValueUpdateImpl(TAG_ID, null, value, serverTimestamp, serverTimestamp, logTimestamp, "Test tag", TagMode.OPERATIONAL);
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












