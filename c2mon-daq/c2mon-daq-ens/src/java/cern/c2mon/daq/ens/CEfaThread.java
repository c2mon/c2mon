/*
 * Created on 5/Jul/2005
 *
 */
package cern.c2mon.daq.ens;

/**
 * This class will be the main class for all threads used. It has flags to stop normally the threads.
 * @author EFACEC
 * 
 */
public class CEfaThread extends Thread{
	/** Stop Running Thread Flag */
	protected boolean bStop=false;
	/**
	 * Sets flags to stop thread
	 *
	 */
	public void ThreadStop(){bStop=true;}
	/**
	 * Starts Thread
	 *
	 */
	public void ThreadStart(){
		bStop=false;
		start();
	}
	
}
