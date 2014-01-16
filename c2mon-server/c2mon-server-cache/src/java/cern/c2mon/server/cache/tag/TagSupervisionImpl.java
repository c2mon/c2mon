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
