/*
 * Created on 6/Jun/2005
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cern.c2mon.daq.ens;

/**
 * Handshake Structure
 * @author EFACEC
 *
 */
public class S_HANDSHAKE extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	S_HANDSHAKE(){
		active++;
		//System.out.println("new S_HANDSHAKE = "+active);
		
		this.setTam(DEF_S_HANDSHAKE); 
		}
	
	/**
	 * finalize method
	 */
	protected void finalize(){
		myDestruct();
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace();  }
	}	
}
