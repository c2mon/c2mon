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
package cern.c2mon.web.restapi.service;

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.web.restapi.cache.TagCache;
import cern.c2mon.web.restapi.exception.UnknownResourceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Service bean for accessing {@link Tag} objects from the C2MON server.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class TagService implements TagListener {

  /**
   * Reference to the service gateway bean.
   */
  @Autowired
  private ServiceGateway gateway;

  /**
   * Reference to the data tag cache.
   */
  @Autowired
  private TagCache cache;

  /**
   * Retrieve a {@link Tag} object.
   *
   * @param id the ID of the {@link Tag} to retrieve
   * @return the {@link Tag} object
   *
   * @throws UnknownResourceException if no tag could be found with the given ID
   */
  public Tag getTag(Long id) throws UnknownResourceException {
    Tag tag;

    // Try to get the tag from the cache
    if (cache.contains(id)) {
      return cache.get(id);
    }

    // Otherwise, try to get the tag from the server
    List<Tag> list = (List<Tag>) gateway.getTagService().get(Collections.singletonList(id));
    if (list.isEmpty()) {
      throw new UnknownResourceException("No tag with id " + id + " was found.");
    } else {
      tag = list.get(0);
    }

    // Subscribe to the tag and add it to the cache
    gateway.getTagService().subscribe(id, this);
    cache.add(tag);

    return tag;
  }

  /**
   * Retrieve a {@link TagConfig} object.
   *
   * @param id the ID of the {@link TagConfig} to retrieve
   * @return the {@link TagConfig} object
   *
   * @throws UnknownResourceException if no tag could be found with the given ID
   */
  public TagConfig getTagConfig(Long id) throws UnknownResourceException {
    TagConfig tagConfig;

    List<TagConfig> list = (List<TagConfig>) gateway.getConfigurationService().getTagConfigurations(Collections.singletonList(id));
    if (list.isEmpty()) {
      throw new UnknownResourceException("No tag with id " + id + " was found.");
    } else {
      tagConfig = list.get(0);
    }

    return tagConfig;
  }

  @Override
  public void onInitialUpdate(Collection<Tag> initialValues) {
    for (Tag tag : initialValues) {
      cache.add(tag);
    }
  }

  @Override
  public void onUpdate(Tag tag) {
    // Update the tag in the cache
    cache.add(tag);
  }
}
