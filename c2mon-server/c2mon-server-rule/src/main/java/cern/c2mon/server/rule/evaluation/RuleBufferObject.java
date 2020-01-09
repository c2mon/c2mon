package cern.c2mon.server.rule.evaluation;

import cern.c2mon.shared.common.datatag.TagQualityStatus;
import lombok.Data;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Inner class which is used to store the rule update
 * information for the cache of the given rule data tag.
 *
 * @author Matthias Braeger
 */
@Data
final class RuleBufferObject {
  /** Rule data tag id */
  private Long id = null;
  /** rule result object */
  private Object value = null;
  /** quality flag */
  private HashSet<TagQualityStatus> qualityCollection = new HashSet<>();
  /** quality flag description */
  private HashMap<TagQualityStatus, String> qualityDescriptions = new HashMap<>();
  /** value description */
  private String valueDesc = null;
  /** rule evaluation timestamp */
  private Timestamp timestamp = null;

  /**
   * Copy Constructor
   * @param rbo The object to be copied
   */
  @SuppressWarnings("unchecked")
  RuleBufferObject(final RuleBufferObject rbo) {
    this.id = rbo.id;
    this.value = rbo.value;
    this.qualityCollection = (HashSet<TagQualityStatus>) rbo.qualityCollection.clone();
    this.qualityDescriptions = (HashMap<TagQualityStatus, String>) rbo.qualityDescriptions.clone();
    this.valueDesc = rbo.valueDesc;
    this.timestamp = new Timestamp(rbo.timestamp.getTime());
  }

  /**
   * Constructor
   * @param pId rule data tag id
   * @param pValue rule result
   * @param pValueDesc description
   * @param pTimestamp rule evaluation timestamp
   */
  RuleBufferObject(final Long pId, final Object pValue, final String pValueDesc, final Timestamp pTimestamp) {
    this(pId, pValue, null, null, pValueDesc, pTimestamp);
  }

  /**
   * Constructor
   * @param pId rule data tag id
   * @param pValue rule result
   * @param pStatus error quality flag
   * @param pValueDesc description
   * @param pTimestamp rule evaluation timestamp
   */
  RuleBufferObject(final Long pId, final Object pValue, final TagQualityStatus pStatus, final String pQualityDesc, final String pValueDesc, final Timestamp pTimestamp) {
      this.id = pId;
      this.value = pValue;
      if (pStatus != null) {
        this.qualityCollection.add(pStatus);
        if (pQualityDesc != null) {
          this.qualityDescriptions.put(pStatus, pQualityDesc);
        }
      }
      this.valueDesc = pValueDesc;
      this.timestamp = pTimestamp;
  }

  /**
   * Updates the values of this <code>BufferObject</code> instance (all invalid status' are removed)
   * @param pValue rule result
   * @param pValueDesc description
   * @param pTimestamp rule evaluation timestamp
   * @return <code>true</code>, if object was updated, else <code>false</code>
   */
  boolean update(final Object pValue, final String pValueDesc, final Timestamp pTimestamp) {
    boolean retval = false;
    synchronized (RuleUpdateBuffer.BUFFER_LOCK) {
      if (this.timestamp.before(pTimestamp) || this.timestamp.equals(pTimestamp)) {
        this.value = pValue;
        this.qualityCollection.clear();
        this.qualityDescriptions.clear();
        this.valueDesc = pValueDesc;
        this.timestamp = pTimestamp;
        retval = true;
      }
    }
    return retval;
  }

  /**
   * Updates the values of this <code>BufferObject</code> instance
   * @param pQuality the error quality code
   * @param pDescription error description
   * @param pTimestamp rule evaluation timestamp
   * @return <code>true</code>, if object was updated, else <code>false</code>
   */
  boolean invalidate(final TagQualityStatus pQuality, final String pDescription, final Timestamp pTimestamp) {
    if (pQuality == null) {
      throw new IllegalArgumentException("invalidate(..) method called with null TagQualityStatus argument.");
    }
    boolean retval = false;
    if (this.timestamp.before(pTimestamp) || this.timestamp.equals(pTimestamp)) {
      this.qualityCollection.add(pQuality);
      this.qualityDescriptions.put(pQuality, pDescription);
      this.timestamp = pTimestamp;
      retval = true;
    }
    return retval;
  }
} // end of RuleBufferObject class
