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
 * Watchdog thread. It will send watchdog message to ScateX
 * @author EFACEC 
 *
 */
public class CEfaWDogManager extends CEfaThread {
	/** GateX client reference */
	private CEfaGatexClient myOwner=null;
	/** Message to send */
	private CEfaMsg myMsg=null;
	/** Watchdog Structure */
	private S_WDOG_REGINFO wdog;
	/**
	 * Constructor
	 * @param _myOwner - GateX client reference
	 */
	static int active=0;
	public CEfaWDogManager(CEfaGatexClient _myOwner) {
		active++;
		//System.out.println("new CEfaWDogManager = "+active);
		
		this.setName("CEfaWDogManager");
		myOwner = _myOwner;
		myMsg = new CEfaMsg();
		myMsg.setSType(1);
		wdog = new S_WDOG_REGINFO();
		wdog.setITimRefresh(myOwner.srvWDogTim);
		myMsg.AddEntity(wdog);
	}
	/**
	 * WDog main cycle. It will send watchdog message to ScateX with the specified cycle in GateX Client
	 */
	public void run(){
		int iTimer=0;
		while ( bStop==false ) {
			try {
				// sendmessage de wdog
				if(iTimer==0 || iTimer>=((myOwner.srvWDogTim-5) * 1000)){	
					myOwner.mySocket.SendMessage(myMsg, true);
					iTimer = 0;
				}
				//myOwner.newLogMsg("WDOG -> Sent");
				sleep( 1 );	
				iTimer++;
			} catch (Exception e) {
				
			}
		}
	}
	
	/**
	 * implementation of memory destructor
	 *
	 */
	public void myDestruct()
	{
		// free mem
		if(myMsg!=null)
			myMsg.myDestruct();
		myMsg = null;
	}

	
	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		myDestruct();
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace(); }
	}	
}
