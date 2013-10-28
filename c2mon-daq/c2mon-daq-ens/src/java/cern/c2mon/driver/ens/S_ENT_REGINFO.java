package cern.c2mon.driver.ens;

import java.nio.ByteBuffer;

import cern.c2mon.driver.ens.S_DP_UPD;
/**
 * Registration Info Structure
 * @author EFACEC
 *
 */
public class S_ENT_REGINFO extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	S_ENT_REGINFO(){
		active++;
		//System.out.println("new S_ENT_REGINFO = "+active);

		this.setTam(DEF_S_ENT_REGINFO); 
		}
	
	/** Entity Tag */
	private char[] caTag = new char[SIZE_SCATEXTAG+1];
	/**
	 * Returns Tag
	 * @return entity tag
	 */
	public final char[] getCaTag(){ return caTag; }
	/**
	 * Sets entity tag
	 * @param _value - entity tag
	 */
	public void setCaTag( char[] _value)
	{ 
		char[] auxchar = new char[SIZE_SCATEXTAG + 1];
		int realsize = 0;
		for (int i = 0; i < _value.length; i++)
		{	
			auxchar[i] = _value[i];
			if( auxchar[i]!=0 ) {
				realsize++;
			}
			
		}
		

		caTag = null;
		caTag = new char [realsize];

		// IN TEST
		//for(int i=0;i<realsize;i++)
		//	caTag[i] = auxchar[i];
		System.arraycopy( auxchar, 0, caTag , 0, realsize);		

		auxchar = null;
		//for(int i=0; i<_value.length; i++)
			//caTag[i] = _value[i]; 
	}
	
	/** DP_UPD Structure */
	private S_DP_UPD sInfo = new S_DP_UPD();
	/**
	 * Gets DP_UPD Structure 
	 * @return DP_UPD Structure 
	 */
	public final S_DP_UPD getSInfo(){ return sInfo; }
	/**
	 * Sets DP_UPD Structure 
	 * @param _value - DP_UPD Structure 
	 */
	public final void setSInfo( S_DP_UPD _value){ sInfo = _value; }
	
	/**
	 * Fills buffer with all data to send to ScateX
	 */
	public void getSerialized(ByteBuffer msg)
	{
		int i;
		//for (i=0; i<this.getTam(); i++)
		for (i=0; i<SIZE_SCATEXTAG+1; i++)
		{
			if (i<caTag.length){
				msg.put((byte)caTag[i]);
			}
			else{
				msg.put((byte)0);	// fill with blanks
			}
		}
		// serialize DP_UDP
		sInfo.getSerialized(msg);
				
	}
	/**
	 * Retrieve data from buffer into variables
	 */
	public boolean enqueue(ByteBuffer msg)
	{
		// 1 - copy ttag
		try {
			char[] auxchar = new char[SIZE_SCATEXTAG + 1];
			int realsize = 0;
			for (int i = 0; i < SIZE_SCATEXTAG + 1; i++)
			{	
				auxchar[i] = (char)msg.get();
				if( auxchar[i]!=0 ){
					realsize++;
				}
			}

			caTag=null;
			caTag = new char [realsize];
			// IN TEST
			//for(int i=0;i<realsize;i++)
			//	caTag[i] = auxchar[i];
			System.arraycopy( auxchar, 0, caTag , 0, realsize);		
			
			auxchar=null;
			
			// 2- serialize DP_UDP
			if (sInfo.enqueue(msg)){
				return true;
			}
			else{
				return false;
			}
			
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
		if(caTag!=null){
			caTag = null;
		}
		if(sInfo!=null){
			sInfo.myDestruct();
		}
		sInfo = null;
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
