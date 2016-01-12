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
package cern.c2mon.daq.ens;

/**
 * Efacec Timetag structure
 * @author EFACEC SE - Filipe Campos
 *
 */
public class CEFaTimetag extends Object {

	/** variable with seconds since 1970 - UTC */
	long lSecs =0;
	/** variable with miliseconds since 1970 - UTC */
	long lMSecs =0;
	/** variable that indicates if time is valid */
	boolean bIsValid = false;
	
	/**
	 * Constructor 
	 *
	 */
	static int active=0;
	public CEFaTimetag (){
		active++;
		//System.out.println("new CEFaTimetag = "+active);
		
		bIsValid  = false;
	}
	
	/**
	 * Constructor
	 * @param secs - seconds
	 * @param msecs - miliseconds
	 */
	public CEFaTimetag (long secs,long msecs){
		active++;
		// System.out.println("new CEFaTimetag = "+active);

		lSecs = secs;
		lMSecs = msecs;
		bIsValid  = true;
	}
	
	/**
	 * Constructor
	 * @param efaTTag - Efacec Timetag object
	 */
	public CEFaTimetag (CEFaTimetag efaTTag){
		active++;
		System.out.println("new CEFaTimetag = "+active);
		
		lSecs = efaTTag.getSecs();
		lMSecs = efaTTag.getMSecs();
		bIsValid  = efaTTag.isTTagValid();
	}
	
	/**
	 * Set the utc time 
	 * @param secs from 1970
	 * @param msecs of timetag
	 */
	public void setUtc(long secs,long msecs){
		lSecs = secs;
		lMSecs = msecs;
		bIsValid  = true;
	}
	
	/**
	 * seconds from 1970
	 * @return seconds from 1970
	 */
	public long getSecs(){
		return lSecs ;
	}
	/**
	 * mili-seconds from 1970
	 * @return mili-seconds from 1970
	 */
	public long getMSecs(){
		return lMSecs ;
	}
	
	/**
	 * is timetag valid
	 * @return is timetag valid
	 */
	public boolean isTTagValid(){
		return bIsValid ;
	}
	
	/**
	 * get total ms from 1970
	 * @return ms from 1970
	 */
	public long  getTotalMS(){
		long tot = lSecs*1000 + lMSecs ;
		return tot;
	}
	
	/**
	 * implementation of memory destructor
	 *
	 */
	public void myDestruct()
	{
		
	}
	
	/**
	 * finalize method
	 */
	protected void finalize(){
		myDestruct();
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace(); }
	}
		
}
