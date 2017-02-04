/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.server.elasticsearch.tag.config;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.elasticsearch.tag.BaseTagDocumentConverter;

/**
 * @author Szymon Halastra
 */
@Component
@Slf4j
public class TagConfigDocumentConverter extends BaseTagDocumentConverter {

  @Autowired
  public TagConfigDocumentConverter(final ProcessCache processCache, final EquipmentCache equipmentCache, final SubEquipmentCache subEquipmentCache) {
    super(processCache, equipmentCache, subEquipmentCache);
  }

  @Override
  public TagConfigDocument convert(Tag tag) {
    TagConfigDocument document = new TagConfigDocument();
    document.putAll(super.convert(tag));
    document.put("timestamp", System.currentTimeMillis());
    return document;
  }

  @Override
  protected Map<String, Object> getC2monMetadata(Tag tag) {
    Map<String, Object> map = super.getC2monMetadata(tag);
    map.put("logged", tag.isLogged());
    return map;
  }
}
