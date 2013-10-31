/*
 * Created on Apr 20, 2005
 *
 */
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
