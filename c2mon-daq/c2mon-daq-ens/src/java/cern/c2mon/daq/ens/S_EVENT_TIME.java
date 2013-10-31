package cern.c2mon.daq.ens;

import java.nio.ByteBuffer;

/**
 * Event Time Structure
 * @author EFACEC
 *
 */
public class S_EVENT_TIME extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	
	S_EVENT_TIME(){
		active++;
		//System.out.println("new S_EVENT_TIME = "+active);
		
		this.setTam(DEF_S_EVENT_TIME); 
		}
	
	/**secs since 1970 (UTC)*/
	private int iSecs;
	/**
	 * Get Seconds
	 * @return secs since 1970 (UTC)
	 */
	public final int getISecs(){ return iSecs; }
	/**
	 * Sets Seconds
	 * @param _value - secs since 1970 (UTC)
	 */
	public final void setISecs( int _value){ iSecs = _value; }
	
	/** Time milliseconds */
	private short sMsecs;
	/**
	 * Gets Time milliseconds 
	 * @return milliseconds 
	 */
	public final short getSMsecs(){ return sMsecs; }
	/**
	 * Sets milliseconds 
	 * @param _value - milliseconds 
	 */
	public final void setSMsecs( short _value){ sMsecs = _value; }
	/**
	 * Fills buffer with all data to send to ScateX
	 */
	public void getSerialized(ByteBuffer msg)
	{
		msg.putInt(iSecs);
		msg.putShort(sMsecs);
	}
	/**
	 * Retrieve data from buffer into variables
	 */
	public boolean enqueue(ByteBuffer msg)
	{
		try {
			iSecs = msg.getInt();
			sMsecs = msg.getShort();
			return true;
		} catch (Exception e) {
			
			return false;
		}
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
