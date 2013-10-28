package cern.c2mon.driver.ens;

import java.nio.ByteBuffer;

import cern.c2mon.driver.ens.S_EVENT_TIME;
/**
 * Event Attribute Structure
 * @author EFACEC
 *
 */
public class S_EVENT_ATTR extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	S_EVENT_ATTR(){
		active++;
		//System.out.println("new S_EVENT_ATTR = "+active);

		this.setTam(DEF_S_EVENT_ATTR); 
		}
	
	/** Event Timetag */
	private S_EVENT_TIME timetag=new S_EVENT_TIME();
	/**
	 * Gets Timetag
	 * @return Event Time Structure
	 */
	public final S_EVENT_TIME getTimetag(){ return timetag; }
	/**
	 * Sets Timetage
	 * @param _value - Event time structure
	 */
	public final void setTimetag( S_EVENT_TIME _value){ timetag = _value; }
	
	/** time attributes */
	private char cDateAttr;
	/**
	 * Gets time attributes 
	 * @return time attributes 
	 */
	public final char getCDateAttr(){ return cDateAttr; }
	/**
	 * Sets time attributes 
	 * @param _value - time attributes 
	 */
	public final void setCDateAttr( char _value){ cDateAttr = _value; }
	/**
	 * Fills buffer with all data to send to ScateX
	 */
	public void getSerialized(ByteBuffer msg)
	{
		timetag.getSerialized(msg);
		msg.put((byte)cDateAttr);
	}
	/**
	 * Retrieve data from buffer into variables
	 */
	public boolean enqueue(ByteBuffer msg)
	{
		try {
			if (timetag.enqueue(msg) == false){
				return false;
			}
			cDateAttr = (char)msg.get();
			
			return true;
			
		} catch (Exception e) {
			
			return false;
		}
	}
	
	/**
	 * implementation of memory destructor
	 *
	 */
	public void myDestruct()
	{
		// free mem
		if(timetag!=null){
			timetag.myDestruct();
		}
		timetag = null;
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
