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
package cern.c2mon.server.eslog.structure.types;

import lombok.extern.slf4j.Slf4j;
import cern.c2mon.pmanager.IFallback;

/**
 * Represents a Tag in ElasticSearch.
 * This type of {@link EsTagImpl}  contains a String value.
 * @author Alban Marguet.
 */
@Slf4j
public class EsTagString extends EsTagImpl implements EsTag {
  /**
   * Set the value as a String for this {@link EsTagImpl}.
   * @param value Object supposed to be a String.
   */
  @Override
  public void setValue(Object value) {
    if (value == null) {
      log.trace("setValue() EsTagString - Value is not set (value= " + value + ").");
    }
    else if (value instanceof String) {
      this.value = value;
      this.valueString = (String) value;
    }
    else {
      throw new IllegalArgumentException("setValue() - Cannot instantiate new EsTagString in ElasticSearch because the value has class=" + value.getClass().getName() + ")");
    }
  }

  @Override
  public IFallback getObject(String line) {
    return GSON.fromJson(line, EsTagString.class);
  }
}