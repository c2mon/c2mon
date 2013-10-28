/*
 * Created on 6/Jun/2005
 *
 */
package cern.c2mon.driver.ens;


/**
 * Registration End Structure
 * @author EFACEC
 *
 */
public class S_REGINIT extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	S_REGINIT(){
		active++;
		//System.out.println("new S_REGINIT = "+active);
		
		this.setTam(DEF_S_REGINIT); 
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
