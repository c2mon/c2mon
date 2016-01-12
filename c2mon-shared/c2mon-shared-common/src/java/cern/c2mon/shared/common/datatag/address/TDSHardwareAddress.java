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
package cern.c2mon.shared.common.datatag.address;


/**
 * This interface represents a single hardware address for the TDS DAQ module.
 * TDS tags are addressed by their tag name, the name of the SmartSockets 
 * subject on which the tag is distributed as well as a TDS-specific data type.
 *
 * @author J. Stowisek
 * @version $Revision: 1.5 $ ($Date: 2005/02/01 17:07:16 $ - $State: Exp $)
 */

public interface TDSHardwareAddress extends HardwareAddress {
  //---------------------------------------------------------------------------
  // Public constant definitions
  //---------------------------------------------------------------------------
  /**
   * TDS type for boolean tags.
   */
  public static final String TDS_BOOL = "BOOL";

  /**
   * TDS type for numeric (analog) tags.
   */
  public static final String TDS_NUM = "NUM";

  /**
   * TDS type for String data tags.
   */
  public static final String TDS_STRING = "STRING";

  //---------------------------------------------------------------------------
  // Public member accessors
  //---------------------------------------------------------------------------
  /**
   * Gets the TDS tag name.
   * The TDS tag name can never be null for any valid TDS tag and its length
   * does normally not exceed 62 characters.
   * @return the TDS tag name.
   */
  public String getTagName();

  /**
   * Get the SmartSockets subject name on which the tag is distributed.
   * @return the SmartSockets subject name on which the tag is distributed.
   */
  public String getSubjectName();

  /**
   * Get the TDS data type for this tag.
   * Valid data types are defined in the constants TDS_STRING, TDS_NUM and 
   * TDS_BOOL.
   * @return the TDS data type for this tag.
   * @see #TDS_STRING
   * @see #TDS_NUM
   * @see #TDS_BOOL
   */
  public String getDataType();
}
