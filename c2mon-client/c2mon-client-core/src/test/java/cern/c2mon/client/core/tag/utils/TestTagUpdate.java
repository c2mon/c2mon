package cern.c2mon.client.core.tag.utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;

/**
 * Helper class for the tests.
 */
public class TestTagUpdate implements TagUpdate {

  private Timestamp sourceTimestamp;

  private Timestamp daqTimestamp;

  private Timestamp serverTimestamp;

  private String valueClassName;

  private final Long id = 1234L;
  
  private Collection<Long> processIds = new ArrayList<>();
  private Collection<Long> equipmentIds = new ArrayList<>();
  private Collection<Long> supEquipmentIds = new ArrayList<>();
  
  private DataTagQuality quality;

  
  public static TestTagUpdate create() {
    return new TestTagUpdate(new Timestamp(System.currentTimeMillis() - 10000L), null, null, "java.lang.String");
  }
  
  public TestTagUpdate(final Timestamp sourceTimestamp, final Timestamp daqTimestamp, final Timestamp serverTimestamp, final String valueClassName) {

    this.sourceTimestamp = sourceTimestamp;
    this.daqTimestamp = daqTimestamp;
    this.serverTimestamp = serverTimestamp;
    this.valueClassName = valueClassName;
    this.quality = new DataTagQualityImpl();
  }

  public void setServerTimestamp(Timestamp serverTimestamp) {
    this.serverTimestamp = serverTimestamp;
  }

  public void setSourceTimestamp(Timestamp sourceTimestamp) {
    this.sourceTimestamp = sourceTimestamp;
  }

  public void setDaqTimestamp(Timestamp daqTimestamp) {
    this.daqTimestamp = daqTimestamp;
  }
  
  @Override
  public Long getId() {
    return id;
  }

  @Override
  public DataTagQuality getDataTagQuality() {
    return quality;
  }

  @Override
  public Object getValue() {
    return null;
  }

  @Override
  public Timestamp getSourceTimestamp() {
    return sourceTimestamp;
  }

  @Override
  public Timestamp getDaqTimestamp() {
    return daqTimestamp;
  }

  @Override
  public Timestamp getServerTimestamp() {
    return serverTimestamp;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public Collection<AlarmValue> getAlarms() {
    return new ArrayList<AlarmValue>();
  }

  @Override
  public TagMode getMode() {
    return null;
  }

  @Override
  public boolean isSimulated() {
    return false;
  }

  @Override
  public String getValueDescription() {
    return null;
  }

  @Override
  public String getValueClassName() {
    return this.valueClassName;
  }

  @Override
  public void setValue(Object arg) {

  }
  
  public void setProcessIds(Collection<Long> ids) {
    this.processIds = ids;
  }

  @Override
  public Collection<Long> getProcessIds() {
    return processIds;
  }
  
  public void setEquipmentIds(Collection<Long> ids) {
    this.equipmentIds = ids;
  }

  @Override
  public Collection<Long> getEquipmentIds() {
    return equipmentIds;
  }

  public void setSubEquipmentIds(Collection<Long> ids) {
    this.supEquipmentIds = ids;
  }
  
  @Override
  public Collection<Long> getSubEquipmentIds() {
    return supEquipmentIds;
  }

  @Override
  public String getUnit() {
    return null;
  }

  @Override
  public String getRuleExpression() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getTopicName() {
    return null;
  }

  @Override
  public boolean isControlTag() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAliveTag() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Map<String, Object> getMetadata(){
    return new HashMap<> ();
  }
}