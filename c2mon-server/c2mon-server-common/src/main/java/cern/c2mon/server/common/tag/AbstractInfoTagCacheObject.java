package cern.c2mon.server.common.tag;

import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractInfoTagCacheObject extends AbstractTagCacheObject {

  public static final Timestamp DEFAULT_TIMESTAMP = new Timestamp(0L);
  /**
   * Address configuration of the datatag (if any)
   */
  private DataTagAddress address = null;
  /**
   * Timestamp set by the source for the current value (may be null).
   */
  private Timestamp sourceTimestamp;
  /**
   * Timestamp set by the DAQ for the current value (may be null).
   * Is set when the value is updated or invalidated in the DAQ core map.
   */
  private Timestamp daqTimestamp;

  /**
   * Constructor used to return a cache object when the object cannot be found
   * in the cache. Sets the quality to UNINITIALISED with the message
   * "No value received for this data tag so far".
   * <p>
   * The fields sets in this constructor (4 parameters + quality field) can be
   * assumed to always be non-null on objects circulating in the server. All other
   * fields may take on null values and appropriate checks should be made.
   */
  public AbstractInfoTagCacheObject(Long id, String name, String datatype, short mode) {
    super(id);
    setName(name);
    setDataType(datatype);
    setMode(mode);
    setDataTagQuality(new DataTagQualityImpl());
  }

  public AbstractInfoTagCacheObject(long id) {
    super(id);
  }

  /**
   * Clone implementation.
   */
  @Override
  public AbstractInfoTagCacheObject clone() {
    AbstractInfoTagCacheObject valueTagCacheObject = (AbstractInfoTagCacheObject) super.clone();
    if (address != null) {
      valueTagCacheObject.address = this.address.clone();
    }
    if (sourceTimestamp != null) {
      valueTagCacheObject.sourceTimestamp = (Timestamp) this.sourceTimestamp.clone();
    }
    if (daqTimestamp != null) {
      valueTagCacheObject.daqTimestamp = (Timestamp) this.daqTimestamp.clone();
    }
    return valueTagCacheObject;
  }

  /**
   * @param serverTimestamp the serverTimestamp to set
   */
  public void setSourceTimestamp(Timestamp serverTimestamp) {
    sourceTimestamp = serverTimestamp != null ? new Timestamp(serverTimestamp.getTime()) : null;
  }

  @Override
  public final Timestamp getTimestamp() {
    return (sourceTimestamp != null) ? sourceTimestamp
      : (daqTimestamp != null) ? daqTimestamp
      : DEFAULT_TIMESTAMP;
  }

  /**
   * @param daqTimestamp the daqTimestamp to set
   */
  public void setDaqTimestamp(Timestamp daqTimestamp) {
    this.daqTimestamp = daqTimestamp != null ? new Timestamp(daqTimestamp.getTime()) : null;
  }
}
