/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.ext.history.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.ext.history.common.HistoryLoadingConfiguration;
import cern.c2mon.client.ext.history.common.HistoryLoadingManager;
import cern.c2mon.client.ext.history.common.HistorySupervisionEvent;
import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.HistoryUpdate;
import cern.c2mon.client.ext.history.common.event.HistoryLoadingManagerListener;
import cern.c2mon.client.ext.history.common.exception.LoadingParameterException;
import cern.c2mon.client.ext.history.common.id.SupervisionEventId;
import cern.c2mon.client.ext.history.playback.components.ListenersManager;
import cern.c2mon.client.ext.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.client.ext.history.util.KeyForValuesMap;
import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionEntity;

/**
 * This class implements the logical functions of a history loading manager. Ie.
 * does not implement the retrival using the history provider. And does not fire
 * events.
 * 
 * @author vdeila
 * 
 */
abstract class HistoryLoadingManagerAbs implements HistoryLoadingManager {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryLoadingManagerAbs.class);
  
  /** <code>true</code> if currently loading */
  private boolean loading;

  /** manages the listeners */
  private ListenersManager<HistoryLoadingManagerListener> listenerManager;

  /** the loading parameters */
  private HistoryLoadingConfiguration configuration;

  /** Keeps track of which tags connects to which supervision events */
  private KeyForValuesMap<Long, SupervisionEventId> tagToSupervisionIds;

  /** List of the tags that should be loaded */
  private Map<Long, ClientDataTag> tagsToLoad;

  /** List of the supervision events that should be loaded */
  private Set<SupervisionEventId> supervisionEventsToLoad;
  
  /** Comparator which sorts by execution time ascending */
  private final Comparator<HistoryUpdate> sortByExecutionTime;

  /** Map of loaded updates */
  private final Map<Long, List<HistoryTagValueUpdate>> loadedHistoryTagValueUpdates;
  
  /** Lock for {@link #loadedHistoryTagValueUpdates} */
  private final ReentrantReadWriteLock loadedHistoryTagValueUpdatesLock;
  
  /** Map of supervision events */
  private final Map<SupervisionEventId, List<HistorySupervisionEvent>> loadedHistorySupervisionEvents;
  
  /** Lock for {@link #loadedHistorySupervisionEvents} */
  private final ReentrantReadWriteLock loadedHistorySupervisionEventsLock;
  
  /**
   * Constructor
   */
  public HistoryLoadingManagerAbs() {
    this.loading = false;
    this.listenerManager = new ListenersManager<HistoryLoadingManagerListener>();
    this.configuration = null;
    this.tagToSupervisionIds = new KeyForValuesMap<Long, SupervisionEventId>();
    this.tagsToLoad = new HashMap<Long, ClientDataTag>();
    this.supervisionEventsToLoad = new HashSet<SupervisionEventId>();
    
    this.loadedHistoryTagValueUpdates = new HashMap<Long, List<HistoryTagValueUpdate>>();
    this.loadedHistorySupervisionEvents = new HashMap<SupervisionEventId, List<HistorySupervisionEvent>>();
    this.loadedHistoryTagValueUpdatesLock = new ReentrantReadWriteLock();
    this.loadedHistorySupervisionEventsLock = new ReentrantReadWriteLock();
    
    this.sortByExecutionTime = new Comparator<HistoryUpdate>() {
      @Override
      public int compare(final HistoryUpdate o1, final HistoryUpdate o2) {
        return o1.getExecutionTimestamp().compareTo(o2.getExecutionTimestamp());
      }
    };
  }

  @Override
  public void addClientDataTagForLoading(final ClientDataTag tag) {
    tagsToLoad.put(tag.getId(), tag);
    
    connectTagToSupervision(tag.getId(), SupervisionEntity.PROCESS, tag.getProcessIds());
    connectTagToSupervision(tag.getId(), SupervisionEntity.EQUIPMENT, tag.getEquipmentIds());
    // Uncomment when sub equipment is available..
    // connectTagToSupervision(tag.getId(), SupervisionEntity.SUBEQUIPMENT, tag.getSubEquipmentIds());
  }

  /**
   * Connects the <code>tagId</code> to the supervision entity with the given
   * ids.
   * 
   * @param tagId
   *          the tag id
   * @param entity
   *          the supervision entity
   * @param ids
   *          the supervision ids for the given entity
   */
  private void connectTagToSupervision(final Long tagId, final SupervisionEntity entity, final Collection<Long> ids) {
    for (Long id : ids) {
      final SupervisionEventId supervisionEventId = new SupervisionEventId(entity, id);
      this.tagToSupervisionIds.add(tagId, supervisionEventId);
      this.supervisionEventsToLoad.add(supervisionEventId);
    }
  }

  @Override
  public Collection<HistoryUpdate> getAllHistory(final Long tagId) {
    final List<HistoryUpdate> resultList = new ArrayList<HistoryUpdate>();

    // Gets the history of the supervision events
    for (final SupervisionEventId supervisionEventId : this.tagToSupervisionIds.getValues(tagId)) {
      resultList.addAll(getHistory(supervisionEventId));
    }

    // Gets the tag history
    resultList.addAll(getHistory(tagId));

    final HistoryUpdate[] result = resultList.toArray(new HistoryUpdate[0]);
    final Comparator<HistoryUpdate> comparator = getHistoryUpdateComparator();
    if (comparator != null) {
      Arrays.sort(result, comparator);
    }

    return Arrays.asList(result);
  }

  /**
   * The default comparator sorts by execution timestamp ascending
   * 
   * @return the comparator used before returning a list of HistoryUpdates. Or
   *         <code>null</code> if sorting shouldn't be done.
   */
  protected Comparator<HistoryUpdate> getHistoryUpdateComparator() {
    return this.sortByExecutionTime;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getAllHistoryConverted(final Long tagId) {
    final HistoryUpdate[] historyUpdates = getAllHistory(tagId).toArray(new HistoryUpdate[0]);
    
    // Sorts by execution time, ascending
    Arrays.sort(historyUpdates, sortByExecutionTime);
    
    final ClientDataTag clientDataTag = tagsToLoad.get(tagId);
    if (clientDataTag == null) {
      // Shouldn't happen
      throw new RuntimeException("The client data tag have been removed!");
    }
    
    final SupervisionListener clientDataTagSupervision;
    if (clientDataTag instanceof SupervisionListener) {
      clientDataTagSupervision = (SupervisionListener) clientDataTag;
    }
    else {
      clientDataTagSupervision = null;
      if (clientDataTag.getProcessIds() != null || clientDataTag.getProcessIds().size() != 0
          || clientDataTag.getEquipmentIds() != null || clientDataTag.getEquipmentIds().size() != 0) {
        throw new RuntimeException("The client data tag must be an instance of SupervisionListener!");
      }
    }
    
    // Setting the data type
    String dataType = "String";
    if (clientDataTag != null && clientDataTag.getType() != null) {
      dataType = clientDataTag.getType().getSimpleName();
    }
    for (HistoryUpdate historyUpdate : historyUpdates) {
      if (historyUpdate instanceof HistoryTagValueUpdate) {
        dataType = ((HistoryTagValueUpdate) historyUpdate).getDataType();
        break;
      }
    }
    
    clientDataTag.clean();
    
    final boolean removeRedundantData = configuration.isRemoveRedundantData();
    
    final List<HistoryTagValueUpdate> result = new ArrayList<HistoryTagValueUpdate>();
    
    HistoryTagValueUpdateImpl previousAddedValue = null;
    
    for (int index = 0; index < historyUpdates.length; index++) {
      final HistoryUpdate historyUpdate = historyUpdates[index];
      if (historyUpdate instanceof HistoryTagValueUpdate) {
        final HistoryTagValueUpdate historyTagValueUpdate = (HistoryTagValueUpdate) historyUpdate;
        clientDataTag.onUpdate(historyTagValueUpdate);
        
        try {
          final HistoryTagValueUpdateImpl update = new HistoryTagValueUpdateImpl(
              clientDataTag.getId(), 
              clientDataTag.getDataTagQuality().clone(), 
              clientDataTag.getValue(), 
              historyTagValueUpdate.getSourceTimestamp(), 
              clientDataTag.getServerTimestamp(), 
              historyTagValueUpdate.getLogTimestamp(), 
              clientDataTag.getDescription(), 
              clientDataTag.getAlarms().toArray(new AlarmValue[0]), 
              clientDataTag.getMode());
          update.setInitialValue(historyTagValueUpdate.isInitialValue());
          update.setDataType(dataType);
          update.setDaqTimestamp(historyTagValueUpdate.getDaqTimestamp());
          result.add(update);
          previousAddedValue = update;
        }
        catch (CloneNotSupportedException e) {
          LOG.error("Failed to create history record.", e);
        }
      }
      else if (historyUpdate instanceof HistorySupervisionEvent) {
        final HistorySupervisionEvent historyEvent = (HistorySupervisionEvent) historyUpdate;
        clientDataTagSupervision.onSupervisionUpdate(historyEvent);
        
        // Adds the client data tag only if it is initialized.
        if (clientDataTag.getDataTagQuality().isInitialised()) {
          try {
            final HistoryTagValueUpdateImpl update = new HistoryTagValueUpdateImpl(
                clientDataTag.getId(), 
                clientDataTag.getDataTagQuality().clone(), 
                clientDataTag.getValue(), 
                null, 
                historyEvent.getEventTime(), 
                null, 
                clientDataTag.getDescription(), 
                clientDataTag.getAlarms().toArray(new AlarmValue[0]), 
                clientDataTag.getMode());
            update.setInitialValue(historyEvent.isInitialValue());
            update.setDataType(dataType);
            if (!removeRedundantData || !isRedundantData(previousAddedValue, update)) {
              result.add(update);
              previousAddedValue = update;
            }
          }
          catch (CloneNotSupportedException e) {
            LOG.error("Failed to create history record.", e);
          }
        }
      }
      else {
        LOG.error(String.format("Does not support HistoryUpdate of type \"%s\" (Id: %s)", historyUpdate.getClass().getName(), historyUpdate.getDataId().toString()));
      }
    }
    return result;
  }
  
  /**
   * @param previousRecord the previous record
   * @param newRecord the record to check if it is redundant.
   * @return <code>true</code> of the <code>newRecord</code> is redundant because of the previous record
   */
  private static boolean isRedundantData(final HistoryTagValueUpdate previousRecord, final HistoryTagValueUpdate newRecord) {
    return 
      newRecord == null // Returns true if the value is null.
      ||
      previousRecord != null
      && objEquals(previousRecord.getValue(), newRecord.getValue())
      && objEquals(previousRecord.getDescription(), newRecord.getDescription())
      && objEquals(previousRecord.getValueDescription(), newRecord.getValueDescription())
      && objEquals(previousRecord.getMode(), newRecord.getMode())
      && (
          previousRecord.getDataTagQuality() == newRecord.getDataTagQuality()
          ||
          previousRecord.getDataTagQuality() != null 
          && newRecord.getDataTagQuality() != null
          && objEquals(previousRecord.getDataTagQuality().getDescription(), newRecord.getDataTagQuality().getDescription())
          );
  }
  
  /**
   * @param obj1 an object
   * @param obj2 another object
   * @return <code>true</code> if the two objects is the same (includes if both are null)
   */
  private static boolean objEquals(final Object obj1, final Object obj2) {
    return obj1 == obj2
      || 
      obj1 != null 
      && obj2 != null 
      && obj1.equals(obj2);
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(final Long tagId) {
    final List<HistoryTagValueUpdate> list;
    this.loadedHistoryTagValueUpdatesLock.readLock().lock();
    try {
      list = this.loadedHistoryTagValueUpdates.get(tagId);
    }
    finally {
      this.loadedHistoryTagValueUpdatesLock.readLock().unlock();
    }
    if (list != null) {
      synchronized (list) {
        return new ArrayList<HistoryTagValueUpdate>(list);
      }
    }
    else {
      return new ArrayList<HistoryTagValueUpdate>(); 
    }
  }

  @Override
  public Collection<HistorySupervisionEvent> getHistory(final SupervisionEventId supervisionEventId) {
    final List<HistorySupervisionEvent> list;
    this.loadedHistorySupervisionEventsLock.readLock().lock();
    try {
      list = this.loadedHistorySupervisionEvents.get(supervisionEventId);
    }
    finally {
      this.loadedHistorySupervisionEventsLock.readLock().unlock();
    }
    if (list != null) {
      synchronized (list) {
        return new ArrayList<HistorySupervisionEvent>(list);
      }
    }
    else {
      return new ArrayList<HistorySupervisionEvent>(); 
    }
  }

  @Override
  public Collection<HistorySupervisionEvent> getHistory(final SupervisionEntity entity, final Long entityId) {
    return getHistory(new SupervisionEventId(entity, entityId));
  }
  
  @Override
  public Collection<Long> getLoadedTagIds() {
    this.loadedHistoryTagValueUpdatesLock.readLock().lock();
    try {
      return Arrays.asList(this.loadedHistoryTagValueUpdates.keySet().toArray(new Long[0]));
    }
    finally {
      this.loadedHistoryTagValueUpdatesLock.readLock().unlock();
    }
  }

  /**
   * Adds records to the data store
   * 
   * @param records the records to add
   */
  protected void addTagValueUpdates(final Collection<HistoryTagValueUpdate> records) {
    this.loadedHistoryTagValueUpdatesLock.writeLock().lock();
    try {
      for (final HistoryTagValueUpdate record : records) {
        List<HistoryTagValueUpdate> list = this.loadedHistoryTagValueUpdates.get(record.getId());
        if (list == null) {
          list = new ArrayList<HistoryTagValueUpdate>();
          this.loadedHistoryTagValueUpdates.put(record.getId(), list);
        }
        synchronized (list) {
          list.add(record);
        }
      }
    }
    finally {
      this.loadedHistoryTagValueUpdatesLock.writeLock().unlock();
    }
  }
  
  /**
   * Adds records to the data store
   * 
   * @param records the records to add
   */
  protected void addSupervisionEvents(final Collection<HistorySupervisionEvent> records) {
    this.loadedHistorySupervisionEventsLock.writeLock().lock();
    try {
      for (final HistorySupervisionEvent record : records) {
        final SupervisionEventId id = new SupervisionEventId(record.getEntity(), record.getEntityId());
        List<HistorySupervisionEvent> list = this.loadedHistorySupervisionEvents.get(id);
        if (list == null) {
          list = new ArrayList<HistorySupervisionEvent>();
          this.loadedHistorySupervisionEvents.put(id, list);
        }
        synchronized (list) {
          list.add(record);
        }
      }
    }
    finally {
      this.loadedHistorySupervisionEventsLock.writeLock().unlock();
    }
  }
  
  @Override
  public void addClientDataTagsForLoading(final Collection<ClientDataTag> tags) {
    for (ClientDataTag cdt : tags) {
      addClientDataTagForLoading(cdt);
    }
  }

  @Override
  public synchronized boolean isLoading() {
    return this.loading;
  }

  /**
   * @param loading
   *          the loading state to set
   * @return <code>true</code> if the loading state were changed because of the
   *         call
   */
  protected synchronized boolean setLoading(final boolean loading) {
    if (this.loading != loading) {
      this.loading = loading;
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  public final void beginLoading() throws LoadingParameterException {
    beginLoading(true);
  }

  @Override
  public void setConfiguration(final HistoryLoadingConfiguration configuration) {
    this.configuration = new HistoryLoadingConfiguration(configuration);
  }

  /**
   * @return the configuration
   */
  protected HistoryLoadingConfiguration getConfiguration() {
    return this.configuration;
  }

  @Override
  public void addHistoryLoadingManagerListener(final HistoryLoadingManagerListener listener) {
    this.listenerManager.add(listener);
  }

  @Override
  public void removeHistoryLoadingManagerListener(final HistoryLoadingManagerListener listener) {
    this.listenerManager.remove(listener);
  }

  /**
   * 
   * @return all the listeners
   */
  protected Collection<HistoryLoadingManagerListener> getListeners() {
    return this.listenerManager.getAll();
  }

  /**
   * @return a list of tags to load
   */
  protected Map<Long, ClientDataTag> getTagsToLoad() {
    return new HashMap<Long, ClientDataTag>(tagsToLoad);
  }

  /**
   * @return a list of supervision events to load
   */
  protected Collection<SupervisionEventId> getSupervisionEventsToLoad() {
    return new HashSet<SupervisionEventId>(supervisionEventsToLoad);
  }
  
  
}
