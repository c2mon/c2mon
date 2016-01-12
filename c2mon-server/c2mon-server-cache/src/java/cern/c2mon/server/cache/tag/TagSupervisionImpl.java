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
package cern.c2mon.server.cache.tag;

import java.sql.Timestamp;

import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.TagSupervision;
import cern.c2mon.server.common.tag.Tag;

@Service("tagSupervision")
public class TagSupervisionImpl implements TagSupervision {

  @Override
  public void onEquipmentDown(Tag tag, String message, Timestamp timestamp) {
//    AbstractTagCacheObject abstractTag = (AbstractTagCacheObject) tag;
//    abstractTag.setCacheTimestamp(timestamp);
//    abstractTag.getDataTagQuality().addQualityFlag(DataTagQuality.INACCESSIBLE + DataTagQuality.EQUIPMENT_DOWN);
//    abstractTag.getDataTagQuality().setQualityDesc(message);
  }

  @Override
  public void onEquipmentUp(Tag tag, String message, Timestamp timestamp) {
//    AbstractTagCacheObject abstractTag = (AbstractTagCacheObject) tag;
//    abstractTag.setCacheTimestamp(timestamp);
//    abstractTag.getDataTagQuality().removeQualityFlag(DataTagQuality.EQUIPMENT_DOWN); //leaves "inaccessible"
//    abstractTag.getDataTagQuality().setQualityDesc(message + " Waiting for refreshed data after equipment was down.");
  }

  @Override
  public void onProcessDown(Tag tag, String message, Timestamp timestamp) {
//    AbstractTagCacheObject abstractTag = (AbstractTagCacheObject) tag;
//    abstractTag.setCacheTimestamp(timestamp);
//    abstractTag.getDataTagQuality().addQualityFlag(DataTagQuality.INACCESSIBLE + DataTagQuality.PROCESS_DOWN);
//    abstractTag.getDataTagQuality().setQualityDesc(message);
  }

  @Override
  public void onProcessUp(Tag tag, String message, Timestamp timestamp) {
//    AbstractTagCacheObject abstractTag = (AbstractTagCacheObject) tag;
//    abstractTag.setCacheTimestamp(timestamp);
//    abstractTag.getDataTagQuality().removeQualityFlag(DataTagQuality.PROCESS_DOWN); //leaves quality "inaccessible"
//    abstractTag.getDataTagQuality().setQualityDesc(message + " Waiting for refreshed data after DAQ was down.");
  }

}
