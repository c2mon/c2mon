/******************************************************************************
 * This file is part of the CERN Control and Monitoring (C2MON) platform. See
 * http://cern.ch/c2mon
 * 
 * Copyright (C) 2005-2014 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: C2MON team, c2mon-support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.common.datatag.address.impl;

/**
 * This class is used to retrieve information useful for the DB DAQ where
 * any Hardware Address and teh Data Type needs to be linked to a Tag ID
 * 
 * @author vilches
 *
 */
public class DBDAQHardwareAdressImpl extends DBHardwareAddressImpl {

  /**
   * Serial UID
   */
  private static final long serialVersionUID = -3824138546554880971L;

  /**
   * Tag id
   */
  private Long tagId;
  
  /**
   * Tag Data Type
   */
  private String dataType;
  
  public DBDAQHardwareAdressImpl(){};
    
  /**
   * Constructor
   * 
   * @param tagId The Tag id
   * @param dbItemName The item name. Schema: [DATABASE_NAME].[ACCOUNT_NAME].[SPECIFIC_NAME]
   * @param dataType The data tag Data type
   */
  public DBDAQHardwareAdressImpl(Long tagId, String dbItemName, String dataType){
    this.tagId = tagId;
    this.dbItemName = dbItemName;
    this.dataType = dataType;
  }
  
  /**
   * Return the Tag id related with the hardware address
   * 
   * @return Tag ID
   */
  public Long getTagId() {
    return this.tagId;
  }
  
  /**
   * Sets the Tag id related with the hardware addresss
   * 
   * @param tagId Tag ID
   */
  public void setTagId(Long tagId){
    this.tagId = tagId;
  }
  
  /**
   * Return the Data Tag type
   * 
   * @return Data Tag Type
   */
  public String getDataType() {
    return this.dataType;
  }
  
  /**
   * Sets the Data Tag type
   * 
   * @param dataType The Data Tag type
   */
  public void setDataType(String dataType){
    this.dataType = dataType;
  }

}
