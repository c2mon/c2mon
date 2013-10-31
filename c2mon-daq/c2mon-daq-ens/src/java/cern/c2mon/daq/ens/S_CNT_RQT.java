package cern.c2mon.daq.ens;

import java.nio.ByteBuffer;

/**
 * Control Request Structure to send to ScateX
 * @author EFACEC
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class S_CNT_RQT extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	S_CNT_RQT(){
		active++;
		//System.out.println("new S_CNT_RQT = "+active);
		
		this.setTam(DEF_S_CNT_RQT); 
	}
	
	/** Control Tag */
	private char[] caTag = new char[SIZE_SCATEXTAG+1];
	/**
	 * Returns Tag
	 * @return control tag
	 */
	public final char[] getCTag(){ return caTag; }
	/**
	 * Sets control tag
	 * @param _value - control tag
	 */
	public void setCaTag( char[] _value)
	{ 
		char[] auxchar = new char[SIZE_SCATEXTAG + 1];
		int realsize = 0;
		for (int i = 0; i < _value.length; i++)
		{
			auxchar[i] = _value[i];
			if( auxchar[i]!=0 ){
				realsize++;
			}
		}
		caTag=null;
		caTag = new char [realsize];
		//for(int i=0;i<realsize;i++)
		//	caTag[i] = auxchar[i];
		System.arraycopy( auxchar, 0, caTag , 0, realsize);	
		
		auxchar=null;
		
	}

	/** Control type - command or setpoint */
	private short sControlType;
	/**
	 * Gets Control type
	 * @return Control type
	 */
	public final short getSControlType(){ return sControlType; }
	/**
	 * Sets Control type
	 * @param _value - Control type
	 */
	public final void setSControlType(short _value){ sControlType = _value; }
	
	/** Setpoint value */
	private float fSetPointValue;
	/**
	 * Gets Setpoint value 
	 * @return Setpoint value 
	 */
	public final float getFSetPointValue(){ return fSetPointValue; }
	/**
	 * Sets Setpoint value 
	 * @param _value - Setpoint value 
	 */
	public final void setFSetPointValue(float _value){ fSetPointValue = _value; }

	/**
	 * Fills buffer with all data to send to ScateX
	 */
	public void getSerialized(ByteBuffer msg)
	{
		int i;
		for (i=0; i<SIZE_SCATEXTAG + 1; i++)
		{
			if (i<caTag.length){
				msg.put((byte)caTag[i]);
			}
			else{
				msg.put((byte)0);	// fill with blanks
			}
		}
		// control type
		msg.putShort(getSControlType());
		// setpoint value
		msg.putFloat(getFSetPointValue());
	}
	
	/**
	 * implementation of memory destructor
	 *
	 */
	public void myDestruct()
	{
//		 free mem
		caTag = null;
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
