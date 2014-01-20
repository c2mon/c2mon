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
package cern.c2mon.client.ext.history.dbaccess;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cern.c2mon.client.ext.history.dbaccess.HistoryMapper;
import cern.c2mon.client.ext.history.dbaccess.beans.DailySnapshotRequestBean;
import cern.c2mon.client.ext.history.dbaccess.beans.HistoryRecordBean;
import cern.c2mon.client.ext.history.dbaccess.beans.InitialRecordHistoryRequestBean;
import cern.c2mon.client.ext.history.dbaccess.beans.ShortTermLogHistoryRequestBean;
import cern.c2mon.client.ext.history.dbaccess.beans.SupervisionEventRequestBean;
import cern.c2mon.client.ext.history.dbaccess.beans.SupervisionRecordBean;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * Used for testing purposes
 * 
 * @author vdeila
 *
 */
public class FakeHistoryMapperImpl implements HistoryMapper {

  
  private static final long MINIMUM_TIME_BETWEEN_RECORDS = 1000;
  
  private static final long MAXIMUM_TIME_BETWEEN_RECORDS = 12 * 1000 * 60 * 60;
  
  private static final long TAG_TIME_MINUS_LOG_TIME = 1000*60*60*2;
  
  private static final Random random = new Random();
  
  public FakeHistoryMapperImpl() {
    
  }
  
  /**
   * Used to give different quality status for the tags.
   * By using a easy algorithm it can be checked from in the test
   * that the right status is set.
   * 
   * @param logTime The log time for the tag
   * @return A random quality based on the <code>logTime</code>
   */
  public static DataTagQuality getDataTagQuality(final long logTime) {
    random.setSeed(logTime);
    int index = random.nextInt(TagQualityStatus.values().length+10);
    
    DataTagQuality result;
    if (index >= TagQualityStatus.values().length) {
      result = new DataTagQualityImpl();
      result.validate();
    }
    else {
      result = new DataTagQualityImpl(TagQualityStatus.values()[index], "Test description");
    }
    return result;
  }
  
  public static Float getTagValue(final long logTime) {
    random.setSeed(logTime);
    return random.nextFloat() * 150;
  }

  private HistoryRecordBean createHistoryRecordBean(final Long tagId, final long logTime) {
    final long seed = tagId * logTime;
    
    final HistoryRecordBean result = new HistoryRecordBean(tagId);
    final DataTagQuality dataTagQuality = getDataTagQuality(seed);
    
    result.setDataTagQuality(dataTagQuality);
    result.setLogDate(new Timestamp(logTime));
    result.setTagTime(new Timestamp(logTime + TAG_TIME_MINUS_LOG_TIME));
    result.setFromInitialSnapshot(false);
    result.setTagDataType("Float");
    result.setTagValue(getTagValue(seed).toString());
    result.setTagMode((short) 0);
    result.setTagName("TagName" + tagId);
    
    return result;
  }
  
  @Override
  public HistoryRecordBean getInitialRecord(final InitialRecordHistoryRequestBean request) {
    final HistoryRecordBean result = createHistoryRecordBean(request.getTagId(), request.getBeforeTime().getTime() - 1000*60*60*6);
    result.setFromInitialSnapshot(true);
    return result;
  }

  @Override
  public List<HistoryRecordBean> getRecords(final ShortTermLogHistoryRequestBean request) {
    if (request.getFromTime() == null && request.getMaxRecords() == null) {
      throw new RuntimeException("Either fromTime or maxRecords must be set!");
    }
    
    final Timestamp fromTime = request.getFromTime();
    Timestamp toTime = request.getToTime();
    if (toTime == null) {
      toTime = new Timestamp(System.currentTimeMillis());
    }
    
    final List<HistoryRecordBean> result = new ArrayList<HistoryRecordBean>();
    final Random random = new Random();
    for (final Long tagId : request.getTagIds()) {
      random.setSeed(tagId * toTime.getTime());
      long currentTime = toTime.getTime();
      while ((fromTime == null || currentTime > fromTime.getTime()) 
          && (request.getMaxRecords() == null || result.size() < request.getMaxRecords())) {
        currentTime -= 
          Math.abs((random.nextLong() % (MAXIMUM_TIME_BETWEEN_RECORDS - MINIMUM_TIME_BETWEEN_RECORDS)))
          + MINIMUM_TIME_BETWEEN_RECORDS;
        
        result.add(createHistoryRecordBean(tagId, currentTime - TAG_TIME_MINUS_LOG_TIME));
      }
      if (request.getMaxRecords() != null && result.size() >= request.getMaxRecords()) {
        break;
      }
    }
    
    return result;
  }

  @Override
  public List<SupervisionRecordBean> getSupervisionEvents(SupervisionEventRequestBean request) {
    throw new UnsupportedOperationException("This test function is not yet implemented");
    // Todo implement to test
  }

  @Override
  public List<SupervisionRecordBean> getInitialSupervisionEvents(SupervisionEventRequestBean request) {
    throw new UnsupportedOperationException("This test function is not yet implemented");
 // Todo implement to test
  }

  @Override
  public List<HistoryRecordBean> getDailySnapshotRecords(DailySnapshotRequestBean request) {
    throw new UnsupportedOperationException("This test function is not yet implemented");
    // Todo implement to test
  }
  

}
