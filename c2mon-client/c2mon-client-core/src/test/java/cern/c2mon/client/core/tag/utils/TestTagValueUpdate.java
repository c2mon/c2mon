package cern.c2mon.client.core.tag.utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;

/**
 * Helper class for the tests.
 */
public class TestTagValueUpdate implements TagValueUpdate {

  private Timestamp sourceTimestamp;

  private Timestamp daqTimestamp;

  private Timestamp serverTimestamp;

  private String valueClassName;
  
  private DataTagQuality quality;

  private final Long id = 1234L;
  
  
  public static TestTagValueUpdate create() {
    return  new TestTagValueUpdate(new Timestamp(System.currentTimeMillis() - 10000L), null, null, "java.lang.String");
  }
  
  public TestTagValueUpdate(final Timestamp sourceTimestamp, final Timestamp daqTimestamp, final Timestamp serverTimestamp, final String valueClassName) {

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
}