/*
 * Created on Apr 18, 2005
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cern.c2mon.driver.ens;

/**
 * Efacec Analog Entity
 * @author EFACEC
 *
 */
public class CEfaEntityAnl extends CEfaEntity {

	/**
	 * Constructor 
	 *
	 */
	public CEfaEntityAnl(){
		active++;
		//System.out.println("new CEfaEntityAnl = "+active);
	}
		
	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace(); }
	}		
	
	
}
