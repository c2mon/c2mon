package cern.c2mon.server.cache.tag;

import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * Implementation of the common functionality for modifying Tag objects.
 *
 * @param <T> the Tag class this Facade acts on
 */
@Slf4j
@Service
public abstract class AbstractTagService<T extends Tag> implements CommonTagOperations<T> {

  @Override
  public void validate(T tag) {
    tag.getDataTagQuality().validate();
  }

  /**
   * Call within synch.
   * @param tag
   * @param value
   * @param valueDesc
   */
  @Override
  public void updateValue(final T tag, final Object value, final String valueDesc) {
    //cast the passed object to the module cache object implementation
    //to access setter methods (not provided in the common interface)
    AbstractTagCacheObject abstractTag = (AbstractTagCacheObject) tag;

    //update the value... need to adjust this for no obj creation
    abstractTag.setValue(value);

    if (valueDesc != null) {
      if (valueDesc.length() > MAX_DESC_LENGTH) {
        log.warn("Detected oversized value description for tag {} - is being truncated (max size is set at {})",
          tag.getId(), MAX_DESC_LENGTH);
        abstractTag.setValueDescription(valueDesc.substring(0, MAX_DESC_LENGTH));
      } else {
        abstractTag.setValueDescription(valueDesc);
      }
    }
  }

  @Override
  public void updateQuality(final T tag, final TagQualityStatus qualityStatusToAdd, final String description) {
    tag.getDataTagQuality().addInvalidStatus(qualityStatusToAdd, description);
  }

  @Override
  public void addQualityFlag(final T tag, final TagQualityStatus statusToAdd, final String description) {
    tag.getDataTagQuality().addInvalidStatus(statusToAdd, description);
  }

  @Override
  public void setCacheTimestamp(final T tag, final Timestamp timestamp) {
    AbstractTagCacheObject abstractTagCacheObject = (AbstractTagCacheObject) tag;
    abstractTagCacheObject.setCacheTimestamp(timestamp);
  }
}
