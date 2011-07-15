/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.core.tag;

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
