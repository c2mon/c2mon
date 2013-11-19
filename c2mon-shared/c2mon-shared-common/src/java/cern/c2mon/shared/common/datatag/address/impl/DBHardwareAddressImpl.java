package cern.c2mon.shared.common.datatag.address.impl;

import cern.c2mon.shared.common.datatag.address.DBHardwareAddress;

public class DBHardwareAddressImpl extends HardwareAddressImpl implements
		DBHardwareAddress {

	/** Serial UID */
	private static final long serialVersionUID = 3098291787686272949L;
	
	protected String dbItemName;
	
	public DBHardwareAddressImpl(){
    }
    
	
	public DBHardwareAddressImpl(String dbItemName){
		this.setDBItemName(dbItemName);
	}
	
	@Override
	public String getDBItemName() {
		return dbItemName;
	}
	
	public void setDBItemName(String dbItemName){
		this.dbItemName = dbItemName;
	}

}
