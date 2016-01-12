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
 * The DIPHardwareAddress interface is used by the DIPMessageHandler. 
 * It contains all information necessary to extract a data element from any 
 * DIP publication. DIP publications can be one of the following types:
 * <UL>
 * <LI>Simple: In that case they contain a single data element. Only the name of
 * the DIP publication (itemName) needs to be known.</LI>
 * <LI>Structures: In this case the data element TIM is interested in is in a 
 * named field within the DIP publication and needs to be extracted from that 
 * field. (Example: itemName.fieldName)</LI>
 * <LI>Simple arrays: In this case the data element TIM is interested in is in 
 * an array. (Example: itemName[fieldIndex])</LI>
 * <LI>Array within a structure: In this (most complex) case the data element 
 * TIM is interested in is within an array contained in a named field within the 
 * DIP publication (Example: itemName.fieldName[fieldIndex])
 * </UL>
 * @see cern.c2mon.daq.dip.DIPMessageHandler
 * @author J. Stowisek
 * @version $Revision: 1.7 $ ($Date: 2006/09/11 10:35:36 $ - $State: Exp $)
 */

 public interface DIPHardwareAddress extends HardwareAddress {
  
  // ---------------------------------------------------------------------------
  // Public accessor methods
  // ---------------------------------------------------------------------------
  /** 
   * Get the name of the DIP publication (DIP item).
   * The item name can never be null.
   * @return the name of the DIP publication
   */
  public String getItemName();

  /**
   * Get the name of the field within the published structure.
   * Note: If isComplexItem() returns false, the field will be null. In this 
   * case the DIP publication is not a structure.
   */
  public String getFieldName();

  /**
   * Return the index of the element within the array published on DIP.
   */
  public int getFieldIndex();

  // ---------------------------------------------------------------------------
  // Public utility methods
  // ---------------------------------------------------------------------------

  /**
   * Returns true if the DIP publication is a structure. In that case, the 
   * DIPHardareAddress object refers to a given field within the structure.
   * @see #getFieldName()
   */
  public boolean isComplexItem();
 
  /**
   * Returns true if the DIP publication contains an array. In that case the 
   * DIPHardwareAddress object refers to a field within the array.
   * @see #getFieldIndex()
   */
  public boolean isArrayItem();
}
