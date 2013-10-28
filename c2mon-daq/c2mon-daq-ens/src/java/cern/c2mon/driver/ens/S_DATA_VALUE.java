package cern.c2mon.driver.ens;

import java.nio.ByteBuffer;
/**
 * Data Value Structure 
 * @author EFACEC
 *
 */
public class S_DATA_VALUE extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	S_DATA_VALUE(){
		active++;
		//System.out.println("new S_DATA_VALUE = "+active);
		
		this.setTam(DEF_S_DATA_VALUE); 
		}
	
	/** Entity digital value */
	private int nValue=0;
	/**
	 * Gets Entity digital value 
	 * @return Entity digital value 
	 */
	public final int getNValue(){ return nValue; }
	/**
	 * Sets Gets Entity digital value 
	 * @param _value - value to set
	 */
	public final void setNValue( int _value){ nValue = _value; }
	
	/** Entity analog value */
	private float fValue=0;
	/**
	 * Gets Entity analog value (analogs and counters)
	 * @return Entity analog value 
	 */
	public final float getFValue(){ return fValue; }
	/**
	 * Sets Entity analog value (analogs and counters)
	 * @param _value - value to set
	 */
	public final void setFValue( float _value){ fValue = _value; }
	//** Is digital value */
	public boolean bIntValue=true; 
	/**
	 * Fills buffer with all data to send to ScateX
	 */
	public void getSerialized(ByteBuffer msg)
	{
		msg.putFloat(fValue);	 
	}
	/**
	 * Retrieve data from buffer into variables
	 */
	public boolean enqueue(ByteBuffer msg)
	{
		try {
			if (bIntValue){
				nValue = msg.getInt();
			}
			else{
				fValue = msg.getFloat();
			}
			
			return true;
		} catch (Exception e) {
			
			return false;
		}
	}
	
	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace(); }
	}
	
}
