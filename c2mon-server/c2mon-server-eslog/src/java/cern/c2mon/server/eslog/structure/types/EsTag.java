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

/**
 * Interface for the {@link EsTagImpl} format.
 * @author Alban Marguet.
 */
public interface EsTag {
  /**
   * Override the toString() method to display the structure of a {@link EsTagImpl} as a JSON.
   */
  String toString();

  /**
   * @return the value of a {@link EsTagImpl}.
   */
  Object getValue();

  /**
   * Set the Object value but also the valueNumeric, valueBoolean or valueString depending on the class of the child.
   * @param tagValue to give to a {@link EsTagImpl}. Type will be checked to correspond to the type of EsTagImpl.
   */
  void setValue(Object tagValue);
}