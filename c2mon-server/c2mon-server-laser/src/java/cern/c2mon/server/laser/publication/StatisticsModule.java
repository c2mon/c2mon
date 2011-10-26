/**
 * 
 */
package cern.c2mon.server.laser.publication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cern.tim.server.common.alarm.Alarm;

/** A Class which keep track on statistics for {@link Alarm} objects.
 *  
 * @author felixehm
 *
 */
public class StatisticsModule {

	/**
	 * Time formatter for toString(). 
	 */
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	/**
	 * Each {@link #update(Alarm)} will cause this counter to increase.
	 */
	private long totalProcessed = 0;
	
	/**
	 * A list which keeps the {@link MyAlarm} objects.
	 */
	private HashMap<String,MyAlarm> list = new HashMap<String,MyAlarm>();
	
	/**
	 * 
	 * @return the total number {@link #update(Alarm)} had been called (after {@link #resetStatistics()} ).
	 */
	public long getTotalProcessed() {
		return totalProcessed;
	}

	/**
	 * Empty constructor. 
	 */
	public StatisticsModule(){
		
	}
	
	/** Updates the timestamp of the alarmID and increases the processed counter.
	 * 
	 * @param alarm the Id of the alarm to track.
	 */
	public void update(Alarm alarm){
		String id = StatisticsModule.getID(alarm);
		if (!list.containsKey(id))
			list.put(id,new MyAlarm(id));
		list.get(id).update();
		totalProcessed++;
	}
	
	/**
	 * Resets the overall statistics and per alarm source statistics.
	 */
	public void resetStatistics(){
		totalProcessed = 0;
		for (MyAlarm a : list.values()){
			a.processed = 0;
		}
	}
	
	private static String getID(Alarm alarm){
		return (new StringBuilder().append(alarm.getFaultFamily()).append(":").append(alarm.getFaultMember()).append(":").append(alarm.getFaultCode())).toString();
	}
	
	/** Resets the statistics for one specific alarm.
	 * 
	 * @param alarmID the id of the alarm to reset. 
	 */
	public void resetStatistics(String alarm){
		if (list.containsKey(alarm)){
			list.get(alarm).processed = 0;
		}
	}
	
	/**
	 * 
	 * @return the internal statistics as string representation.
	 */
	public List<String> getStatsList(){
		ArrayList<String> ret = new ArrayList<String>();
		for (MyAlarm a : list.values()){
			ret.add(a.toString());
		}
		return ret;
	}
	
	public final MyAlarm getStatsForAlarm(String id){
		return list.get(id);
	}
	
	/** a private object which encapsulates individual information on one alarm 
	 * 
	 * @author felixehm
	 *
	 */
	public class MyAlarm{
		private long lastSeen = System.currentTimeMillis();
		private long processed = 0;
		private final String name;
		
		public MyAlarm(String id){
			name = id;
		}
		
		public void update(){
			processed++;
			lastSeen = System.currentTimeMillis();
		}
		
		public String toString(){
			return name + ", cnt=" + processed + ", lastSeen=" +formatter.format(new Date(lastSeen));
		}
		
		public final long getLastSeen(){
			return this.lastSeen;
		}
		
		public final String getName(){
			return this.name;
		}
		
		public final long getProcessedTimes(){
			return this.processed;
		}
		/**
		 * 
		 * @return a copy of this {@link MyAlarm} instance. 
		 */
		public MyAlarm getCopy(){
			MyAlarm ret = new MyAlarm(this.name);
			ret.lastSeen = this.lastSeen;
			ret.processed = this.processed;
			return ret;
		}
	}
	
}
