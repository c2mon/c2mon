/*
 * Created on Apr 18, 2005
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cern.c2mon.driver.ens;

/**
 * Efacec Control Response Entity
 * @author EFACEC
 *
 */
public class CEfaEntityCtrResp extends CEfaEntityCtr {

	
		/** event values */
		public static final byte GX_EVECTR_OK = 0;
		public static final byte GX_EVECTR_INIB = 1;
		public static final byte GX_EVECTR_FAIL = 2;
		public static final byte GX_EVECTR_INVALID = 3;
		public static final byte GX_EVECTR_TIMEOUT = 4;
	
	/**
	 * Constructor 
	 *
	 */
	public CEfaEntityCtrResp(){
		active++;
		//System.out.println("new CEfaEntityCtrResp = "+active);
		
	}		
		
	/**
	 * get control responce from scatex
	 * @return
	 */
	public int getResponce(){
		switch ((int)fGetValue()){
		case 0:
			return GX_EVECTR_OK;
		case 1:
			return GX_EVECTR_INIB;
		case 2:
			return GX_EVECTR_FAIL;
		case 3:
		case 4:
			return GX_EVECTR_TIMEOUT;
		}
		
		// unknow code or invalid
		return GX_EVECTR_INVALID ;
	}
	

	/**
	 * set responce code
	 * @param code
	 * @return true if is event, in control is always event
	 */
	public boolean setResponce(int code){
		bSetValue(code);
		return true;
	}

	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace();  }

	}		
	
}
