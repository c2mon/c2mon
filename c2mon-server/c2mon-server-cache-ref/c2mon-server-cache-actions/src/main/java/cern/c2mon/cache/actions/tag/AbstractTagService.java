package cern.c2mon.cache.actions.tag;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

// TODO Is this class still useful for anything?

/**
 * Implementation of the common functionality for modifying Tag objects.
 *
 * @param <T> the Tag class this Facade acts on
 */
@Slf4j
@Service
public abstract class AbstractTagService<T extends Tag> implements CommonTagOperations<T> {

  private C2monCache<T> cacheRef;

  @Inject
  protected AbstractTagService(final C2monCache<T> cacheRef) {
    this.cacheRef = cacheRef;
  }

  @Override
  public void validate(T tag) {
    tag.getDataTagQuality().validate();
  }

  /**
   * Call within synch.
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

  @Override
  public void setQuality(Long tagId, Collection<TagQualityStatus> flagsToAdd, Collection<TagQualityStatus> flagsToRemove, Map<TagQualityStatus, String> qualityDescriptions, Timestamp timestamp) {
    T tag = cacheRef.get(tagId);
    if (flagsToRemove == null && flagsToAdd == null) {
      log.warn("Attempting to set quality in TagFacade with no Quality flags to remove or set!");
    }

    if (flagsToRemove != null) {
      for (TagQualityStatus status : flagsToRemove) {
        tag.getDataTagQuality().removeInvalidStatus(status);
      }
    }
    if (flagsToAdd != null) {
      for (TagQualityStatus status : flagsToAdd) {
        tag.getDataTagQuality().addInvalidStatus(status, qualityDescriptions.get(status));
      }
    }
    ((AbstractTagCacheObject) tag).setCacheTimestamp(timestamp);
  }

  /**
   * Returns the tag located if it can be located in any of the rule, control
   * or data tag cache (in that order). If it fails to locate a tag with the
   * given id in any of these, it throws an unchecked <java>CacheElementNotFound</java>
   * exception.
   * <p>
   * If unsure if a tag with the given id exists, use preferably the
   * <java>isInTagCache(Long)</java> method
   *
   * @param id the Tag id
   * @return a copy of the Tag object in the cache
   */
  public T getCopy(Long id) {
    if (id != null) {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
           ObjectOutputStream oos = new ObjectOutputStream(baos);) {
        T reference = cacheRef.get(id);

        oos.writeObject(reference);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (T) ois.readObject();
      } catch (CacheElementNotFoundException cenfe) {
        throw cenfe;
      } catch (Exception ex) {
        log.error(
          "Unable to get a serialized copy of the cache element as serialization is not supported for this object.",
          ex);
        throw new UnsupportedOperationException(
          "The getCopy() method is not supported for this cache element since the cache object is not entirely serializable. Please revisit your object.",
          ex);
      }
    } else {
      log.error("getCopy() - Trying to access cache with a NULL key - throwing an exception!");
      throw new IllegalArgumentException("Accessing cache with null key!");
    }
  }

  /**
   * Checks if the new Tag value should be filtered out or updated.
   * Is filtered out if value, value description and quality are
   * the same.
   *
   * @param timestamp the new timestamp
   * @param valueDescription the new description
   * @param value the new value
   * @param tag the tag that is updated
   * @param statusToAdd the tag quality status to add; leave null if the tag is to be validated
   * @param statusDescription the new status description; leave null if the tag is to be validated
   * @return true if it should be filtered out
   * @throws NullPointerException if called with null tag parameter
   * @throws IllegalArgumentException if status description is not null but statusToAdd is (does not make any sense!) or the same for the value
   */
  public boolean filterout(Tag tag, Object value, String valueDescription,
                           TagQualityStatus statusToAdd, String statusDescription, Timestamp timestamp) {
    if (statusToAdd == null && statusDescription != null) {
      throw new IllegalArgumentException("Filterout method called with non-null status description but null status");
    }
    if (value == null && valueDescription != null) {
      throw new IllegalArgumentException("Filterout method called with non-null value description but null value");
    }
    boolean sameValue;
    if (tag.getValue() != null){
      sameValue = tag.getValue().equals(value);
    } else {
      sameValue = (value == null);
    }
    if (!sameValue) {
      return false;
    }

    boolean sameDescription;
    if (tag.getValueDescription() != null){
      sameDescription = tag.getValueDescription().equalsIgnoreCase(valueDescription);
    } else {
      sameDescription = (valueDescription == null);
    }
    if (!sameDescription) {
      return false;
    }

    boolean sameQuality;
    if (statusToAdd == null){
      sameQuality = tag.getDataTagQuality().isValid();
    } else {
      sameQuality = (tag.getDataTagQuality() != null
        && tag.getDataTagQuality().isInvalidStatusSetWithSameDescription(statusToAdd, statusDescription));
    }
    return sameQuality;
  }

  /**
   * As for general filterout method, but for invalidation only.
   * @param tag the current tag
   * @param statusToAdd the status to add
   * @param statusDescription the status description to use
   * @param timestamp the invalidation time
   * @return true if should be filtered
   */
  public boolean filteroutInvalidation(T tag, TagQualityStatus statusToAdd, String statusDescription, Timestamp timestamp) {
    return filterout(tag, tag.getValue(), tag.getValueDescription(), statusToAdd, statusDescription, timestamp);
  }

  /**
   * As for general filterout method, but for valid updates only.
   * @param tag the current tag
   * @param value the new value
   * @param valueDescription the new value description
   * @param timestamp the update time
   * @return true if should be filtered
   */
  public boolean filteroutValid(T tag, Object value, String valueDescription, Timestamp timestamp) {
    return filterout(tag, value, valueDescription, null, null, timestamp);
  }
}
