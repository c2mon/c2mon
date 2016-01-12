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
package cern.c2mon.client.common.tag;

/**
 * Enumeration of all class types that are supported by
 * <code>ClientDataTag</code>.
 *
 * @author Matthias Braeger
 */
public enum TypeNumeric {
  /** Hash Code type constant used by <code>getNumericType()</code> */
  TYPE_UNKNOWN(Void.class.hashCode()),
  /** Hash Code type constant used by <code>getNumericType()</code> */
  TYPE_BOOLEAN(Boolean.class.hashCode()),
  /** Hash Code type constant used by <code>getNumericType()</code> */
  TYPE_FLOAT(Float.class.hashCode()),
  /** Hash Code type constant used by <code>getNumericType()</code> */
  TYPE_INTEGER(Integer.class.hashCode()),
  /** Hash Code type constant used by <code>getNumericType()</code> */
  TYPE_DOUBLE(Double.class.hashCode()),
  /** Hash Code type constant used by <code>getNumericType()</code> */
  TYPE_LONG(Long.class.hashCode()),
  /** Hash Code type constant used by <code>getNumericType()</code> */
  TYPE_SHORT(Short.class.hashCode()),
  /** Hash Code type constant used by <code>getNumericType()</code> */
  TYPE_STRING(String.class.hashCode()),
  /** Hash Code type constant used by <code>getNumericType()</code> */
  TYPE_BYTE(Byte.class.hashCode());
  
  /** Hashcode of the represented class type*/
  private int hashCode;
  
  /**
   * Default Constructor
   * @param hashCode The hash code of the represented class type
   */
  private TypeNumeric(final int hashCode) {
    this.hashCode = hashCode;
  }
  
  /**
   * @return The hash code of the represented class type
   */
  public int getCode() {
    return hashCode;
  }
}
