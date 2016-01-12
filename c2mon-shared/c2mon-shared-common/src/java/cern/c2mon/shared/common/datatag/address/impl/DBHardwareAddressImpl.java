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
package cern.c2mon.shared.common.datatag.address.impl;

import org.simpleframework.xml.Element;

import cern.c2mon.shared.common.datatag.address.DBHardwareAddress;

public class DBHardwareAddressImpl extends HardwareAddressImpl implements DBHardwareAddress {

	/** Serial UID */
	private static final long serialVersionUID = 3098291787686272949L;

	@Element(name = "db-item-name")
	protected String dbItemName;

	public DBHardwareAddressImpl(){}


	public DBHardwareAddressImpl(String dbItemName){
		this.dbItemName = dbItemName;
	}

	@Override
	public String getDBItemName() {
		return dbItemName;
	}

  /**
   * This method is only needed on the DAQ layer to store
   * additional information
   * @param dbItemName the dbItemName to set
   */
  public final void setDbItemName(String dbItemName) {
    this.dbItemName = dbItemName;
  }
}
