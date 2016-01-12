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
 * Efacec Control Response Entity
 * @author EFACEC
 *
 */
public class CEfaEntityCtrResp extends CEfaEntityCtr {

	
		/** event values */
		public static final byte GX_EVECTR_OK = 0;
		public static final byte GX_EVECTR_INIB = 1;
		public static final byte GX_EVECTR_FAIL = 2;
		public static final byte GX_EVECTR_INVALID = 3;
		public static final byte GX_EVECTR_TIMEOUT = 4;
	
	/**
	 * Constructor 
	 *
	 */
	public CEfaEntityCtrResp(){
		active++;
		//System.out.println("new CEfaEntityCtrResp = "+active);
		
	}		
		
	/**
	 * get control responce from scatex
	 * @return
	 */
	public int getResponce(){
		switch ((int)fGetValue()){
		case 0:
			return GX_EVECTR_OK;
		case 1:
			return GX_EVECTR_INIB;
		case 2:
			return GX_EVECTR_FAIL;
		case 3:
		case 4:
			return GX_EVECTR_TIMEOUT;
		}
		
		// unknow code or invalid
		return GX_EVECTR_INVALID ;
	}
	

	/**
	 * set responce code
	 * @param code
	 * @return true if is event, in control is always event
	 */
	public boolean setResponce(int code){
		bSetValue(code);
		return true;
	}

	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace();  }

	}		
	
}
