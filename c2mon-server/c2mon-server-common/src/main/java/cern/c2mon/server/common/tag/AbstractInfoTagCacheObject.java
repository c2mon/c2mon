package cern.c2mon.server.common.tag;

import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.Set;

import static cern.c2mon.server.common.util.Java9Collections.setOfNonNulls;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class AbstractInfoTagCacheObject extends AbstractTagCacheObject {
  /**
   * Minimum value for range checks. If the system receives a tag value that is
   * less than the authorized minimum value, it will flag the new tag value as
   * invalid.
   */
  private Comparable minValue = null;
  /**
   * Maximum value for range checks. If the system receives a tag value that is
   * less than the authorized minimum value, it will flag the new tag value as
   * invalid.
   */
  private Comparable maxValue = null;
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
   * Reference to equipment the Datatag is attached to.
   */
  private Long equipmentId = null;
  /**
   * Reference to sub equipment the DataTag is attached to.
   */
  private Long subEquipmentId = null;
  /**
   * Id of the Process this DataTag is attached to (loaded from DB also during cache loading).
   */
  private Long processId;

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

  /**
   * For testing only so far
   */
  public AbstractInfoTagCacheObject(final Long id) {
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
    return valueTagCacheObject;
  }

  /**
   * @param serverTimestamp the serverTimestamp to set
   */
  public void setSourceTimestamp(Timestamp serverTimestamp) {
    if (this.sourceTimestamp == null || serverTimestamp == null) {
      this.sourceTimestamp = serverTimestamp;
    } else {
      this.sourceTimestamp.setTime(serverTimestamp.getTime());
    }
  }

  public final boolean hasAddress() {
    return this.address != null;
  }

  @Override
  public final Timestamp getTimestamp() {
    return (sourceTimestamp != null) ? sourceTimestamp
      : (daqTimestamp != null) ? daqTimestamp
      : getCacheTimestamp();
  }

  /**
   * @param daqTimestamp the daqTimestamp to set
   */
  public void setDaqTimestamp(Timestamp daqTimestamp) {
    if (this.daqTimestamp == null || daqTimestamp == null) {
      this.daqTimestamp = daqTimestamp;
    } else {
      this.daqTimestamp.setTime(daqTimestamp.getTime());
    }
  }

  @Override
  public Set<Long> getEquipmentIds() {
    return setOfNonNulls(equipmentId);
  }

  @Override
  public Set<Long> getProcessIds() {
    return setOfNonNulls(processId);
  }

  @Override
  public Set<Long> getSubEquipmentIds() {
    return setOfNonNulls(subEquipmentId);
  }
}
