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
 * This type of {@link AbstractEsTag} contains a numeric value.
 *
 * @author Alban Marguet.
 */
@Slf4j
public class EsTagNumeric extends AbstractEsTag {

  /**
   * Set the value of this EsTagNumeric to the value of the Tag in C2MON.
   *
   * @param rawValue Object supposed to be numeric.
   */
  @Override
  public void setRawValue(final Object rawValue) {
    if (rawValue == null) {
      log.trace("setRawValue() EsTagNumeric - Value is null");
      return;
    }

    this.rawValue = rawValue;

    if (!(rawValue instanceof Number)) {
      log.trace("setRawValue() - rawValue has rawValue " + rawValue + ".");
      throw new IllegalArgumentException("setRawValue() - Cannot instantiate new EsTagNumeric in ElasticSearch " +
          "because the rawValue is of type=" + rawValue.getClass().getName());
    }
    this.value = (Number) rawValue;
  }

  @Override
  public IFallback getObject(String line) {
    return gson.fromJson(line, EsTagNumeric.class);
  }
}