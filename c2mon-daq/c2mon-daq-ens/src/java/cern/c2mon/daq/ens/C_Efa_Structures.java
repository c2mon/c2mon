
package cern.c2mon.daq.ens;

import java.nio.ByteBuffer;
/**
 * This class is the base class for all structures(S_XXX) used in messages
 * @author EFACEC
 *
 */
class C_Efa_Structures extends Object {
	
	static int active;
	public C_Efa_Structures(){
		active++;
		// System.out.println("new C_Efa_Structures = "+active);
	}

	/** Struture Sizes */
	public static int SIZE_SCATEXTAG = 16;
	public static int DEF_S_CNT_REPLY = 18;
	public static int DEF_S_CNT_RQT = 23;
	public static int DEF_S_DATA_VALUE = 4;
	public static int DEF_S_DP_UPD = 21;
	public static int DEF_S_ENT_ASKVAL = 17;
	public static int DEF_S_ENT_OBJECTS = 38;
	public static int DEF_S_ENT_REGINFO = 38;
	public static int DEF_S_ENT_SENDVAL = 38;
	public static int DEF_S_ENT_TAG = 17;
	public static int DEF_S_EVENT_ATTR = 7;
	public static int DEF_S_EVENT_TIME = 6;
	public static int DEF_S_WDOG_REGINFO = 4;
	public static int DEF_S_HANDSHAKE = 1;
	public static int DEF_S_REGINIT = 1;
	public static int DEF_S_REGEND = 1;
	public static int DEF_S_ACK = 1; 
	/**
	 * Size of the structure
	 */
	private int tam;
	/**
	 * Returns the size of the structure
	 * @return size of the structure
	 */
	final public int getTam(){ return tam; }
	/**
	 * Sets the size of the structure
	 * @param _value - size of the structure
	 */
	final public void setTam(int _value){ tam = _value; }
	/**
	 * It will serialize all params into the buffer
	 * @param msg - Buffer that will contain the message serialized
	 */
	public void getSerialized(ByteBuffer msg)
	{
		
	}
	/**
	 * It will get data from Buffer and fill structure values
	 * @param msg - buffer with message with data
	 * @return True if all ok
	 */
	public boolean enqueue(ByteBuffer msg)
	{	
		return true;
	}
	
	/**
	 * implementation of memory destructor
	 *
	 */
	public void myDestruct()
	{
	}
	
	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace();  }
	}
	
}
