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
package cern.c2mon.server.eslog.structure.types.tag;

import cern.c2mon.pmanager.IFallback;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a Tag in ElasticSearch.
 * This type of {@link AbstractEsTag}  contains a String value.
 *
 * @author Alban Marguet.
 */
@Slf4j
public class EsTagString extends AbstractEsTag {

  /**
   * Set the value as a String for this {@link AbstractEsTag}.
   *
   * @param rawValue Object supposed to be a String.
   */
  @Override
  public void setRawValue(final Object rawValue) {
    if (rawValue == null) {
      log.trace("setRawValue() EsTagString - Value is not set (rawValue= " + rawValue + ").");
      return;
    }

    this.rawValue = rawValue;

    if (!(rawValue instanceof String)) {
      throw new IllegalArgumentException("setRawValue() - Cannot instantiate new EsTagString in ElasticSearch " +
          "because the rawValue has class=" + rawValue.getClass().getName() + ")");
    }

    this.valueString = (String) rawValue;
  }

  @Override
  public IFallback getObject(String line) {
    return gson.fromJson(line, EsTagString.class);
  }
}