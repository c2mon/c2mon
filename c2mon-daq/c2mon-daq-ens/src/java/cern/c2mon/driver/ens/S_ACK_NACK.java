/*
 * Created on 7/Jun/2005
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cern.c2mon.driver.ens;

/**
 * ACK or NACK Structure
 * @author EFACEC
 *
 */
public class S_ACK_NACK extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	S_ACK_NACK(){ 
		active++;
		//System.out.println("new S_ACK_NACK = "+active);
		
		this.setTam(DEF_S_ACK); 
		}	

	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace();   }
	}	
}
