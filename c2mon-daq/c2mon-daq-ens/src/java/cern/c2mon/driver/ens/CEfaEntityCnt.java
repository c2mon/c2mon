/*
 * Created on Apr 18, 2005
 *
 */
package cern.c2mon.driver.ens;

/**
 * Efacec Counter Entity
 * @author EFACEC
 *
 */
public class CEfaEntityCnt extends CEfaEntity {

	/**
	 * Constructor 
	 *
	 */
	public CEfaEntityCnt(){
		active++;
		//System.out.println("new CEfaEntityCnt = "+active);
	}
	
	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace();  }
	}		
	
}
