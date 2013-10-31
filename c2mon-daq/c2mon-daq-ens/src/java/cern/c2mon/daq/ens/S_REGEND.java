/*
 * Created on 6/Jun/2005
 *
 */
package cern.c2mon.daq.ens;

/**
 * Registration End Structure
 * @author EFACEC
 *
 */
public class S_REGEND extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	S_REGEND(){
		active++;
		//System.out.println("new S_REGEND = "+active);
		
		this.setTam(DEF_S_REGEND); 
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
