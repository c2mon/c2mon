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
