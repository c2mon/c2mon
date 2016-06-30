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
package cern.c2mon.server.daqcommunication.out.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.daqcommunication.out.DataRefreshManager;
import cern.c2mon.server.daqcommunication.out.ProcessCommunicationManager;
import cern.c2mon.shared.common.datatag.DataTagValueUpdate;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueRequest;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueResponse;

/**
 * Implementation of Data refresh service.
 *
 * @author Mark Brightwell
 *
 */
@Service
@ManagedResource(objectName="cern.c2mon:name=dataRefreshManager")
public class DataRefreshManagerImpl implements DataRefreshManager {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DataRefreshManagerImpl.class);

  /**
   * For locating tags in caches.
   */
  private TagLocationService tagLocationService;

  /**
   * For updating the cache.
   */
  private DataTagFacade dataTagFacade;

  /**
   * For getting the latest value from the DAQ layer.
   */
  private ProcessCommunicationManager processCommunicationManager;

  /**
   * For refreshing all tags.
   */
  private ProcessCache processCache;

  /**
   * Autowired constructor.
   * @param tagLocationService Tag location service
   * @param dataTagFacade Tag facade
   * @param processCommunicationManager DAQ communication manager
   * @param processCache the Process cache
   */
  @Autowired
  public DataRefreshManagerImpl(final TagLocationService tagLocationService, final DataTagFacade dataTagFacade,
                                final ProcessCommunicationManager processCommunicationManager, final ProcessCache processCache) {
    super();
    this.tagLocationService = tagLocationService;
    this.dataTagFacade = dataTagFacade;
    this.processCommunicationManager = processCommunicationManager;
    this.processCache = processCache;
  }

  @Override
  @ManagedOperation(description="Refresh values for a given DAQ from the DAQ cache; provide DAQ id.")
  public void refreshValuesForProcess(final Long id) {
    SourceDataTagValueResponse latestValues = processCommunicationManager.requestDataTagValues(
        new SourceDataTagValueRequest(SourceDataTagValueRequest.DataTagRequestType.PROCESS, id));
    updateCache(latestValues);
  }

  @Override
  public void refreshTagsForAllProcess() {
    for (Long key : processCache.getKeys()) {
      try {
        refreshValuesForProcess(key);
      } catch (Exception e) {
        LOGGER.error("Exception caught while refreshing values for process " + key, e);
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
        LOGGER.error("Exception caught while refreshing a Tag with the latest DAQ cache value (id=" + value.getId() + ")", e);
      }
    }
  }
}
