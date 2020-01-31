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
package cern.c2mon.server.history.listener;

import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.history.logger.BatchLogger;
import cern.c2mon.shared.common.CacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Listens to updates in the Rule and DataTag caches and calls the DAO
 * for logging these to the history database.
 *
 * @author Alexandros Papageorgiou, Mark Brightwell
 */
@Service
public class TagRecordListener {

  /**
   * Bean that logs Tags into the history.
   */
  private BatchLogger<Tag> tagLogger;
  private TagCacheCollection unifiedTagCacheFacade;

  /**
   * Autowired constructor.
   *
   * @param unifiedTagCacheFacade for registering cache listeners
   * @param tagLogger                for logging cache objects to the STL
   */
  @Autowired
  public TagRecordListener(final TagCacheCollection unifiedTagCacheFacade,
                           @Qualifier("tagLogger") final BatchLogger<Tag> tagLogger) {
    super();
    this.unifiedTagCacheFacade = unifiedTagCacheFacade;
    this.tagLogger = tagLogger;
  }

  /**
   * Registers to be notified of all Tag updates (data, rule and control tags).
   */
  @PostConstruct
  public void init() {
    unifiedTagCacheFacade.registerBufferedListener( tags ->
      tagLogger.log(((List<Tag>) tags).stream().filter(tag -> !tag.isLogged()).collect(Collectors.toList()))
    , CacheEvent.UPDATE_ACCEPTED);
  }
}
