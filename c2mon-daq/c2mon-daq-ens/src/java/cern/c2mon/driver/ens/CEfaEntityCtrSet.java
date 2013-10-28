/*
 * Created on Apr 20, 2005
 *
 */
package cern.c2mon.driver.ens;

/**
 * Efacec Control Setpoint Entity
 *  @author EFACEC
 *
 */
public class CEfaEntityCtrSet extends CEfaEntityCtr {

	public CEfaEntityCtrSet(){
		active++;
		//System.out.println("new CEfaEntityCtrSet = "+active);
	}	
	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace();  }

	}		
	
}
