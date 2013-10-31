/*
 * Created on Apr 18, 2005
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cern.c2mon.daq.ens;

/**
 * Efacec Digital Entity
 * @author EFACEC
 *
 */
public class CEfaEntityDig extends CEfaEntity{

	public CEfaEntityDig(){
		active++;
		//System.out.println("new CEfaEntityDig = "+active);
	}	
	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace();  }

	}		
				
}
