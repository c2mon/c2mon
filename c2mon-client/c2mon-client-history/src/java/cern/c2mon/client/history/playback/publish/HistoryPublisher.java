package cern.c2mon.client.history.playback.publish;

import java.sql.Timestamp;
import java.util.Collection;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.HistoryUpdate;
import cern.c2mon.client.common.history.id.SupervisionEventId;
import cern.c2mon.client.common.history.id.TagValueUpdateId;
import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.history.playback.HistoryPlayerImpl;
import cern.c2mon.client.history.updates.HistorySupervisionEventImpl;
import cern.c2mon.client.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.datatag.TagQualityStatus;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * This class manage the publishing of updates, and the mapping to the listeners.
 * 
 * @see HistoryPlayerImpl
 * 
 * @author vdeila
 * 
 */
public class HistoryPublisher {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryPublisher.class);
  
  /** Manages the listeners of the tags */
  private final TagListenersManager tagListenersManager;

  /** Manages the listeners for the supervision */
  private final SupervisionListenersManager[] supervisionManagers;

  /**
   * Constructor
   */
  public HistoryPublisher() {

    tagListenersManager = new TagListenersManager();

    supervisionManagers = new SupervisionListenersManager[SupervisionEntity.values().length];
    for (int i = 0; i < SupervisionEntity.values().length; i++) {
      supervisionManagers[i] = new SupervisionListenersManager();
    }
  }

  /**
   * 
   * @param entity
   *          The entity to get the manager for
   * @return The manager for the given entity type
   */
  public SupervisionListenersManager getSupervisionManager(final SupervisionEntity entity) {
    return this.supervisionManagers[entity.ordinal()];
  }

  /**
   * 
   * @return the manager for the tag listeners
   */
  public TagListenersManager getTagListenersManager() {
    return tagListenersManager;
  }
  
  /**
   * Rmoves all listeners from all the managers
   */
  public void clearAll() {
    tagListenersManager.clear();
    for (int i = 0; i < SupervisionEntity.values().length; i++) {
      supervisionManagers[i].clear();
    }
  }

  /**
   * 
   * @param id
   *          the object id to invalidate
   * @param message
   *          the message to have in the invalidation
   */
  public void invalidate(final Object id, final String message) {
    if (id instanceof TagValueUpdateId) {
      publishInitialValue((TagValueUpdate)
        new HistoryTagValueUpdateImpl(
          ((TagValueUpdateId) id).getTagId(), 
          new DataTagQualityImpl(TagQualityStatus.UNINITIALISED, message), 
          null, 
          null, 
          null, 
          new Timestamp(1), // The data tag is only updated of a value greater than 0
          null,
          "",  
          TagMode.OPERATIONAL));
    }
    else if (id instanceof SupervisionEventId) {
      publishInitialValue((HistoryUpdate)
        new HistorySupervisionEventImpl(
            (SupervisionEventId) id, 
            SupervisionStatus.RUNNING,
            new Timestamp(0),
            message));
    }
    else {
      LOG.error(String.format("The identifier of class \"%s\" is not supported", id.getClass().getName()));
    }
  }
  
  /**
   * Notifies the listeners about the new initial value.
   * 
   * @param initialValue
   *          The initial value which is given. Is sent to the tags which
   *          subscribes to the listeners of this value.
   */
  public void publishInitialValue(final HistoryUpdate initialValue) {
    publish(initialValue, true);
  }
  
  /**
   * Notifies the listeners about a new update.
   * 
   * @param newValue
   *          The new value which is given. Is sent to the tags which subscribes
   *          to the listeners of this value
   */
  public void publish(final HistoryUpdate newValue) {
    publish(newValue, false);
  }
  
  /**
   * Notifies the listeners about a new update
   * 
   * @param newValue
   *          The new value which is given. Is sent to the tags which subscribes
   *          to the listeners of this value
   * @param doClean
   *          <code>true</code> if the the {@link ClientDataTag#clean()} should
   *          be called first. Is of course only called if the newValue is a
   *          {@link TagValueUpdate}
   */
  private void publish(final HistoryUpdate newValue, final boolean doClean) {
    if (newValue instanceof TagValueUpdate) {
      publish((TagValueUpdate) newValue, doClean);
    }
    else if (newValue instanceof SupervisionEvent) {
      publish((SupervisionEvent) newValue);
    }
    else {
      final String errorMessage = String.format("The HistoryUpdate of class \"%s\" is not supported..", newValue.getClass().getName());
      LOG.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }
  }
  
  /**
   * Calls {@link ClientDataTag#clean()} before sending the update. Notifies the
   * listeners about a new update.
   * 
   * @param initialValue
   *          The new value which is given. Is sent to the tags which subscribes
   *          to the tag id in the value
   */
  public void publishInitialValue(final TagValueUpdate initialValue) {
    publish(initialValue, true);
  }
  
  /**
   * Notifies the listeners about a new update
   * 
   * @param nextValue
   *          The new value which is given. Is sent to the tags which subscribes
   *          to the tag id in the value
   */
  public void publish(final TagValueUpdate nextValue) {
    publish(nextValue, false);
  }
  
  /**
   * Notifies the listeners about a new update. Does only call the
   * {@link ClientDataTag#clean()} if <code>doClean</code> is <code>true</code>
   * 
   * @param newValue
   *          The new value which is given. Is sent to the tags which subscribes
   *          to the tag id in the value
   * @param doClean
   *          <code>true</code> if the the {@link ClientDataTag#clean()} should
   *          be called first
   */
  private void publish(final TagValueUpdate newValue, final boolean doClean) {
    for (final TagUpdateListener listener : this.tagListenersManager.getValues(newValue.getId())) {
      try {
        if (doClean
            && listener instanceof ClientDataTag) {
          ((ClientDataTag) listener).clean();
        }
        listener.onUpdate(newValue);
      }
      catch (Exception e) {
        LOG.error(String.format("Error when trying to update tag (id: %d)", newValue.getId()), e);
      }
    }
  }
  
  /**
   * Notifies the listeners about an event
   * 
   * @param event the event to give to the listeners
   */
  public void publish(final SupervisionEvent event) {
    final Collection<SupervisionListener> listeners = getSupervisionManager(event.getEntity()).getValues(event.getEntityId());
    for (final SupervisionListener listener : listeners) {
      try {
        listener.onSupervisionUpdate(event);
      }
      catch (Exception e) {
        LOG.error(String.format("Error when trying to update with a supervision event (%s)", event.toString()), e);
      }
    }
  }
  
}
