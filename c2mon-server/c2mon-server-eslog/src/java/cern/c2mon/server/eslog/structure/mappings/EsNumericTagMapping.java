/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.structure.mappings;

/**
 * EsMapping that a {@link cern.c2mon.server.eslog.structure.types.EsTagNumeric} will use to be indexed in the ElasticSearch cluster.
 * Init valueNumeric with value; valueBoolean = valueString = null.
 *
 * @author Alban Marguet.
 */
public class EsNumericTagMapping extends EsTagMapping implements EsMapping {

  /**
   * Inbstantiate a new EsNumericTagMapping by setting its Properties according to {@param type}
   */
  public EsNumericTagMapping(ValueType type) {
    super();
    setProperties(type);
  }

  /**
   * Initialize the mapping according to the valueType.
   */
  @Override
  public void setProperties(ValueType tagValueType) {
    if(ValueType.isNumeric(tagValueType)) {
      this.properties = new Properties(tagValueType);
    } else {
      throw new IllegalArgumentException("Type for EsTagNumeric must be integer or double.");
    }
  }
}