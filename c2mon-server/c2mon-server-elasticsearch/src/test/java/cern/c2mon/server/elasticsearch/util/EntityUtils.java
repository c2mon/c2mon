package cern.c2mon.server.elasticsearch.util;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.metadata.Metadata;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

import java.sql.Timestamp;
import java.util.Collections;

/**
 * @author Justin Lewis Salmon
 */
public class EntityUtils {

  public static Alarm createAlarm() {
    AlarmCacheObject alarm = new AlarmCacheObject();
    alarm.setId(1L);
    alarm.setDataTagId(2L);
    alarm.setFaultFamily("fault family");
    alarm.setFaultMember("fault member");
    alarm.setFaultCode(0);
    alarm.setTimestamp(new Timestamp(System.currentTimeMillis()));
    alarm.setMetadata(Metadata.builder()
        .addMetadata("building", "1")
        .addMetadata("array", Collections.singletonList("test"))
        .addMetadata("responsiblePerson", "Fred").build());
    alarm.setInfo("alarm info");
    alarm.setState(AlarmCondition.TERMINATE);
    return alarm;
  }

  public static DataTag createDataTag() {
    DataTagCacheObject tag = new DataTagCacheObject(1L, "cpu.loadavg", Long.class.getName(), DataTagConstants.MODE_OPERATIONAL);
    tag.setProcessId(50L);
    tag.setEquipmentId(150L);
    tag.setDescription("Average CPU usage");
    tag.setAddress(new DataTagAddress());
    tag.setMaxValue(0);
    tag.setMinValue(4.0);
    tag.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    tag.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    tag.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    tag.setDataTagQuality(new DataTagQualityImpl());
    tag.setLogged(true);
    tag.setMetadata(Metadata.builder()
        .addMetadata("building", "1")
        .addMetadata("array", Collections.singletonList("test"))
        .addMetadata("responsiblePerson", "Fred").build());
    return tag;
  }

  public static SupervisionEvent createSupervisionEvent() {
    return new SupervisionEventImpl(SupervisionConstants.SupervisionEntity.PROCESS, 50L,
        "P_TEST01", SupervisionConstants.SupervisionStatus.DOWN, new Timestamp(System.currentTimeMillis()), "");
  }
}
