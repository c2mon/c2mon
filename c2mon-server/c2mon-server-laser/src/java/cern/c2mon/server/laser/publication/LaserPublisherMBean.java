/**
 * 
 */
package cern.c2mon.server.laser.publication;

import java.util.List;

/** 
 * MBean interface for the LaserPublisher.
 * 
 * @author felixehm
 *
 */
public interface LaserPublisherMBean {

	/**
	 * @return the processed alarms since last reset. 
	 */
	public long getProcessedAlarms();
	
	/** 
	 * Resets the internal statistics
	 */
	public void resetStatistics();
	
	/**
	 * Resets the internal statistics for a specific alarm
	 */
	public void resetStatistics(String alarmID);
	
	/**
	 * @return A string representation of the collected statistics. 
	 * @see {@link StatisticsModule#getStatsList()} 
	 */
	public List<String> getRegisteredAlarms();
	
	/**
	 * 
	 * @param id the id of the alarm ('FF:FM:FC')
	 * @return a String representation of the alarm stats.
	 * @see {@link StatisticsModule#getStatsForAlarm(String)}
	 */
	public String getStatsForAlarm(String id);
	
}
