/**
 * 
 */
package cern.c2mon.notification;

/**
 * @author felixehm
 *
 */
public class NotifiedDataTag {

	private int status = 0;
	
	private long lastNotified = System.currentTimeMillis(); 

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @param lastNotified the lastNotified to set
	 */
	public void setLastNotified(long lastNotified) {
		this.lastNotified = lastNotified;
	}

	/**
	 * @return the lastNotified
	 */
	public long getLastNotified() {
		return lastNotified;
	}
}
