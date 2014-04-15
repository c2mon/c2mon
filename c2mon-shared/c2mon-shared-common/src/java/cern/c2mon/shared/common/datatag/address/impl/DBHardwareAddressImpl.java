package cern.c2mon.shared.common.datatag.address.impl;

import org.simpleframework.xml.Element;

import cern.c2mon.shared.common.datatag.address.DBHardwareAddress;

public class DBHardwareAddressImpl extends HardwareAddressImpl implements DBHardwareAddress {

	/** Serial UID */
	private static final long serialVersionUID = 3098291787686272949L;
	
	@Element
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
