/*
 * Created on Apr 20, 2005
 *
 */
package cern.c2mon.driver.ens;

import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.DataTagDeadband;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import ch.cern.tim.shared.datatag.address.impl.ENSHardwareAddressImpl;

/**
 * @author Filipe Campos - EFACEC SE
 *
 * This class encapsulates the CEfaEntity object, with the SourceDataTag obj
 * 
 */
public class CEfaDbObj {

	/** object from tim */
	private ISourceDataTag timObj = null;
	/** object from ens */
	private CEfaEntity ensObj = null;
	
	/**
	 * constructor
	 *
	 */
	public CEfaDbObj(){
		
	}
	
	/**
	 * set time object
	 * @param obj
	 */
	final public void setTimObj( ISourceDataTag obj  ){
		timObj = obj ;
	}

	/**
	 * set ens object
	 * @param obj
	 */	
	final public void setEnsObj( CEfaEntity obj  ){
		ensObj = obj ;
	}

	/**
	 * get tim object
	 * @return
	 */
	final public ISourceDataTag getTimObj(){
		return timObj;
	}

	/**
	 * get ens object
	 * @return
	 */
	final public CEfaEntity getEnsObj(){
		return ensObj;
	}

	/**
	 * create ens object from the hardware
	 * @param hardAddr
	 */
	public boolean createEnsObj(ENSHardwareAddressImpl hardAddr, ISourceDataTag sourceDataTag) {
		
		// if does not exist , create it
		if( ensObj == null ){
			
			String entType = hardAddr.getDataType();
			if( entType.equals(ENSHardwareAddressImpl.TYPE_DIGITAL) ) {
				ensObj = new CEfaEntityDig();
				ensObj.setDeadband( 0 );
			}
			else if( entType.equals(ENSHardwareAddressImpl.TYPE_ANALOG) ){
				ensObj = new CEfaEntityAnl();
				ensObj.setDeadband( 0 );

				// 2005.07.08 - FC
				if( sourceDataTag.getValueDeadbandType() == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE ) {
					ensObj.setDeadband( sourceDataTag.getValueDeadband() );
					
					// for cern, they jitter 1 like 0
					float fDeadb = ensObj.fGetDeadBand() ;
					if( fDeadb >= 1.0f ){
						fDeadb -= 1.0f ;
						ensObj.setDeadband( fDeadb );
					}
				}
			}
			else if( entType.equals(ENSHardwareAddressImpl.TYPE_COUNTER) ){
				ensObj = new CEfaEntityCnt();
				ensObj.setDeadband( 0 );

				// 2005.07.08 - FC
				if( sourceDataTag.getValueDeadbandType() == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE ) {
					ensObj.setDeadband( sourceDataTag.getValueDeadband() );
					
					// for cern, they jitter 1 like 0
					float fDeadb = ensObj.fGetDeadBand() ;
					if( fDeadb >= 1.0f ){
						fDeadb -= 1.0f ;
						ensObj.setDeadband( fDeadb );
					}
				}
				
			}
			else if( entType.equals(ENSHardwareAddressImpl.TYPE_CTRL_SIMPLE) ){
				ensObj = new CEfaEntityCtr();
			}
			else if( entType.equals(ENSHardwareAddressImpl.TYPE_CTRL_SETPOINT) ){
				ensObj = new CEfaEntityCtrSet();
			}
		}
		
		if( ensObj==null || timObj==null ){
			return false;
		}
		
		// here already exist, update properties
		ensObj.setId( hardAddr.getAddress() , timObj.getName() );
		ensObj.bSetInvCode( CEfaEntity.EFAENT_UNINITIALISED );
		
		return true;
	}	
	
}
