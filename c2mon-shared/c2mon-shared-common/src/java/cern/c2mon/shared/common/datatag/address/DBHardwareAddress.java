package cern.c2mon.shared.common.datatag.address;

/**
 * The <code>DBHardwareAddress</code> interface is used by the 
 * <code>DBMessageHandler</code>. 
 * @see cern.c2mon.daq.db.DBMessageHandler
 * @author Aleksandra Wardzinska
 */
public interface DBHardwareAddress {

 	/** 
	 * Returns the name of the DB tag 
	 * @return The name of the DB tag to monitor
	 */
	String getDBItemName();
}
