/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.daq.out;

import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueRequest;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Implementation of Data refresh service.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service
@ManagedResource(objectName="cern.c2mon:name=dataRefreshManager")
public class DataRefreshManagerImpl implements DataRefreshManager {

  /** For updating the cache */
  private final DataTagService dataTagFacade;

  /** For getting the latest value from the DAQ layer */
  private final ProcessCommunicationManager processCommunicationManager;

  /** For refreshing all tags. */
  private final ProcessService processCache;

  @Autowired
  public DataRefreshManagerImpl(DataTagService dataTagFacade, ProcessCommunicationManager processCommunicationManager,
      ProcessService processService) {
    super();
    this.dataTagFacade = dataTagFacade;
    this.processCommunicationManager = processCommunicationManager;
    this.processCache = processService;
  }

  @Override
  @ManagedOperation(description="Refresh values for a given DAQ from the DAQ cache; provide DAQ id.")
  public void refreshValuesForProcess(final Long id) {
    SourceDataTagValueResponse latestValues = processCommunicationManager.requestDataTagValues(
        new SourceDataTagValueRequest(SourceDataTagValueRequest.DataTagRequestType.PROCESS, id));
    updateCache(latestValues);
  }

  @ManagedOperation(description="Refresh values for a given DAQ from the DAQ cache; provide DAQ name.")
  public void refreshValuesForProcess(final String name) {
    refreshValuesForProcess(processCache.getProcessIdFromName(name).getId());
  }

  @Override
  public void refreshTagsForAllProcess() {
    for (Long key : processCache.getCache().getKeys()) {
      try {
        refreshValuesForProcess(key);
      } catch (Exception e) {
        log.error("Exception caught while refreshing values for process {} (#{})", processCache.getCache().get(key).getName(), key, e);
      }
    }
  }

  /**
   * Updates the cache with the passed values (obtained from the DAQ layer during a data refresh).
   * @param latestValues values to update the cache with
   */
  private void updateCache(final SourceDataTagValueResponse latestValues) {
    Collection<SourceDataTagValue> updates = latestValues.getAllDataTagValueObjects();
    for (SourceDataTagValue value : updates) {
      try {
        dataTagFacade.updateFromSource(value.getId(), value);
      } catch (Exception e) {
        log.error("Exception caught while refreshing a Tag with the latest DAQ cache value (id=" + value.getId() + ")", e);
      }
    }
  }
}
