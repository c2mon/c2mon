package cern.c2mon.daq.ens;

import java.nio.ByteBuffer;
/**
 * Watchdog Structure
 * @author EFACEC
 *
 */
public class S_WDOG_REGINFO extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	S_WDOG_REGINFO(){
		active++;
		//System.out.println("new S_WDOG_REGINFO = "+active);
		
		this.setTam(DEF_S_WDOG_REGINFO); 
		}
	
	/** Timeout for WDog in seconds*/
	private int iTimRefresh;
	/**
	 * Gets Timeout for WDog 
	 * @return Timeout for WDog in seconds
	 */
	public int getITimRefresh(){ return iTimRefresh; }
	/**
	 * Sets Timeout for WDog 
	 * @param _value - Timeout for WDog in seconds
	 */
	public void setITimRefresh(int _value){ iTimRefresh = _value; }
	/**
	 * Fills buffer with all data to send to ScateX
	 */
	public void getSerialized(ByteBuffer msg)
	{
		msg.putInt(getITimRefresh());
	}
	
	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace(); }
	}
	
}
