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
package cern.c2mon.web.restapi.cache;

import cern.c2mon.client.common.tag.Tag;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the {@link TagCache} interface.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class TagCacheImpl implements TagCache {

  Map<Long, Tag> cache = new HashMap<>();

  @Override
  public void add(Tag tag) {
    cache.put(tag.getId(), tag);
  }

  @Override
  public Tag get(Long id) {
    return cache.get(id);
  }

  @Override
  public void remove(Long id) {
    cache.remove(id);
  }

  @Override
  public boolean contains(Long id) {
    return cache.containsKey(id);
  }
}
