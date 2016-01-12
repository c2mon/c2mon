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
 * Efacec Control Entity
 *  @author EFACCE
 *
 */
public class CEfaEntityCtr extends CEfaEntity {
	/** control order */
	int iOrder=-1;
	/** msg number sent */
	int iMsgNum=-1;
	/**
	 * Constructor 
	 *
	 */
	public CEfaEntityCtr(){
		active++;
		//System.out.println("new CEfaEntityCtr = "+active);
	}
	
	/**
	 * set control order
	 * @param ord
	 */
	public final void setOrder(int ord){
		iOrder = ord;
	}
	
	/**
	 * get order from control
	 * @return
	 */
	public final int getOrder(){
		return iOrder;
	}
	
	/**
	 * set control msg number
	 * @param msg number
	 */
	public final void setMsgNum(int num){
		iMsgNum = num;
	}
	
	/**
	 * get msg num control
	 * @return msg number
	 */
	public final int getMsgNum(){
		return iMsgNum;
	}
	
	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace(); }
	}		
	
}
