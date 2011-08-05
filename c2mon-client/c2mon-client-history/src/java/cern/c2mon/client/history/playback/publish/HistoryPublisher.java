package cern.c2mon.client.history.playback.publish;

import java.util.Collection;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * This class manages the publishing of updates, and the managing of the listeners
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
   * Clears all listeners
   */
  public void clearAll() {
    tagListenersManager.clear();
    for (int i = 0; i < SupervisionEntity.values().length; i++) {
      supervisionManagers[i].clear();
    }
  }

  /**
   * Notifies the listeners about a new update
   * 
   * @param newValue
   *          The new value which is given. Is sent to the tags which subscribes
   *          to the tag id in the value
   */
  public void publish(final TagValueUpdate newValue) {
    for (final TagUpdateListener listener : this.tagListenersManager.getValues(newValue.getId())) {
      try {
        if (listener instanceof ClientDataTag) {
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
      listener.onSupervisionUpdate(event);
    }
  }
  
}
