/*
 * Created on Apr 18, 2005
 *
 */
package cern.c2mon.driver.ens;

/**
 * @author EFACEC
 * 
 * This class is the main class for entitys from gatex
 */
public class CEfaEntity extends Object {
		
	/** identifier in ScateX */
	private String sxId = null;
	/** identifier in others */
	private String oemId = null;
	/** ttag of last update */ 
	private CEFaTimetag timeTag = new CEFaTimetag ();
	/** value of entity */
	private float fValue = 0.0f; 
	/** kidx in scatex */
	private long lKidx = (long)0;
	/** invalid code - entity it is valid */
	public static final byte EFAENT_VALID = 0 ;
	/** invalid code - entity has a invalid tag */
	public static final byte EFAENT_INVTAG = 1 ;
	/** invalid code - entity has not been initialized */
	public static final byte EFAENT_UNINITIALISED = 2 ;
	/** invalid code - entity has overflow */
	public static final byte EFAENT_OVERFLOW = 3 ;
	/** invalid code - entity has moffscan */
	public static final byte EFAENT_MOFFSCAN = 4 ;
	/** invalid code - entity has aoffscan */
	public static final byte EFAENT_AOFFSCAN = 5 ;
	/** invalid code - entity is invalid */
	public static final byte EFAENT_IMPOSED = 6 ;
	/** byte with invalidity code */
	private byte byInvalidCode = EFAENT_UNINITIALISED; 
	/** internal deadband */
	private float fDeadband = 0.0f;
	/** internal max */
	private float fMaxValue = (float)(Long.MAX_VALUE) ; //1000000000.0f;
	/** internal min */
	private float fMinValue = 0.0f;
	
	/**
	 * Constructor
	 *
	 */
	static int active=0;
	
	public CEfaEntity(){
		active++;
		//System.out.println("new CEfaEntity = "+active);
	}

	/**
	 * Set value
	 * @param f - value to set
	 * @return true if the setvalue is a new event 
	 */
	public boolean bSetValue(float f){
		final float dif = fValue-f;
		
		if( Math.abs(dif) > fGetDeadBand() ) {
			fValue=f;
			return true;
		}
		if( byInvalidCode == EFAENT_UNINITIALISED ){
			fValue=f;
			return true;			
		}
	
		return false;
	}

	/**
	 * get the entity value
	 * @return the actual entity value
	 */
	public final float fGetValue(){
		return fValue;
	}
	
	/**
	 * set the kidx from scatex
	 * @param l - the kidx to set
	 */
	public final void setKidx(long l){
		lKidx=l;
	}
	/**
	 * get kidx
	 * @return the kidx of this entity
	 */
	public final long lGetKidx(){
		return lKidx;
	}

	/**
	 * entity is valid
	 * @return true if is valid
	 */
	public final boolean isValid(){
		return (getInvCode()==EFAENT_VALID);
	}
	
	/**
	 * set timetag
	 * @param secs - seconds of the timetag
	 * @param msecs - miliseconds of the timetag
	 */
	public boolean bSetTTag(long secs,long msecs){
		boolean bEvent = false;

		if( timeTag == null || (!(timeTag.getSecs()==secs && timeTag.getMSecs()==msecs)) ){
			bEvent = true;
		}
		
		/*
		// delete for better mem manage
		if(timeTag!=null)
			timeTag.myDestruct();
		timeTag = null;
		timeTag = new CEFaTimetag(secs,msecs);
		*/
		
		if( timeTag == null ){
			timeTag = new CEFaTimetag();
		}
		timeTag.setUtc(secs,msecs);
				
		return bEvent;
	}

	/**
	 * get timetag
	 * @return object efatimetag
	 */
	public final CEFaTimetag getTTag(){
		// delete for better mem manage
		// return new CEFaTimetag(timeTag);
		return timeTag;
	}
	
	/**
	 * get invalidity code
	 * @return invalidity code
	 */
	public final byte getInvCode(){
		return byInvalidCode;
	}
	
	/**
	 * set invalid code
	 * @param byCode byte code for status
	 */
	public final boolean bSetInvCode(byte byCode){
		final boolean bEvent = (byInvalidCode != byCode) ;
		byInvalidCode = byCode;
		return bEvent;
	}
	
	/**
	 * set identifiers 
	 * @param sx - ScateX ID
	 * @param oem - OEM ID
	 */
	public void setId(String sx,String oem){
		sxId = null;
		oemId = null;
		
		sxId = new String (sx);
		oemId = new String (oem);
	}
	
	/**
	 * get sx id
	 * @return the ScateX ID of this entity
	 */
	public final String getSxId(){
		// return new String(sxId);
		return sxId;
	}
	
	/**
	 * get oem tag
	 * @return the oem ID if this entity
	 */
	public final String getOemId(){
		// return new String(oemId);
		return oemId;
	}
	
	/**
	 * get deadband
	 * @return the actual deadband
	 */
	public final float fGetDeadBand(){
		return fDeadband;
	}
	
	/**
	 * set deadband
	 * @param f - the value of the deadband to set
	 */
	public final void setDeadband(float f){
		fDeadband = f;
	}

	/**
	 * set min value
	 * @param f - min value to set
	 */
	public final void setMinValue(float f){
		fMinValue = f;
	}
	
	/**
	 * set max value
	 * @param f - max value to set
	 */
	public final void setMaxValue(float f){
		fMaxValue = f;
	}
	
	public void myDestruct()
	{
		sxId = null; 
		oemId = null;
		if(timeTag!=null){
			timeTag.myDestruct();
		}
		timeTag = null;
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
