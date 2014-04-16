
/******************************************************************************
 * This file is part of the CERN Control and Monitoring (C2MON) platform.
 * 
 * See http://cern.ch/c2mon
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
package cern.c2mon.daq.db;


/**
 * This class is used as a mapper in mybatis for taking info from the DB
 * 
 * @author vilches
 *
 */
public class DBDAQConfigInfo {

  
  protected String dbItemName;
  
  /**
   * Tag id
   */
  private transient Long tagId;
  
  /**
   * Tag Data Type
   */
  private transient String dataType;
  
  public DBDAQConfigInfo(){}
    
  
  public DBDAQConfigInfo(Long tagId, String dbItemName, String dataType){
    this.tagId = tagId;
    this.dbItemName = dbItemName;
    this.dataType = dataType;
  }
  
  /**
   * 
   * @return The Item Name
   */
  public String getDBItemName() {
    return dbItemName;
  }

  /**
   * @param dbItemName the dbItemName to set
   */
  public final void setDbItemName(String dbItemName) {
    this.dbItemName = dbItemName;
  }


  /**
   * @param dataType the dataType to set
   */
  public final void setDataType(String dataType) {
    this.dataType = dataType;
  }


  /**
   * @return the tagId
   */
  public final Long getTagId() {
    return tagId;
  }


  /**
   * @param tagId the tagId to set
   */
  public final void setTagId(Long tagId) {
    this.tagId = tagId;
  }


  /**
   * @return the dataType
   */
  public final String getDataType() {
    return dataType;
  }

}

