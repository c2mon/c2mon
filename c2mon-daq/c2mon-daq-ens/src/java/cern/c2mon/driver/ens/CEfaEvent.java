/*
 * Created on Apr 18, 2005
 *
 */
package cern.c2mon.driver.ens;

/**
 * Event Entity
 * @author EFACEC
 * @version 2.0
 */
public class CEfaEvent extends Object { 
	
	static int active=0;

	/** scada entity */ 
	private CEfaEntity scadaEntity = null;

	
	/**
	 * Constructor
	 *
	 */
	public CEfaEvent(){
		active++;
		//System.out.println("new CEfaEvent = "+active);
	}
	
	/**
	 * set reference of a entity
	 * @param ref - entity reference
	 */
	public void setEntityRef(CEfaEntity ref){
		// scadaEntity =  ref;
		scadaEntity = null;
	
		if( CEfaEntityDig.class.isInstance(ref) ){
			scadaEntity = new CEfaEntityDig();
		}
		else if( CEfaEntityAnl.class.isInstance(ref) ){
			scadaEntity = new CEfaEntityAnl();
		}
		else if( CEfaEntityCtrResp.class.isInstance(ref) ){
			scadaEntity = new CEfaEntityCtrResp();
			((CEfaEntityCtrResp)scadaEntity).setOrder(((CEfaEntityCtrResp)ref).getOrder());
		}
		else if( CEfaEntityCtr.class.isInstance(ref) ){
			scadaEntity = new CEfaEntityCtr();
			((CEfaEntityCtr)scadaEntity).setOrder(((CEfaEntityCtr)ref).getOrder());
		}
		else if( CEfaEntityCnt.class.isInstance(ref) ){
			scadaEntity = new CEfaEntityCnt();
		}
		else {
			System.out.println("ERRO NO setEntityRef");
		}
		
		scadaEntity.setId(ref.getSxId(),ref.getSxId());
		scadaEntity.bSetInvCode(ref.getInvCode());
		scadaEntity.bSetTTag(ref.getTTag().getSecs(),ref.getTTag().getMSecs());
		scadaEntity.bSetValue(ref.fGetValue());
				
	}
	
	
	/**
	 * get reference of entity
	 * @return a reference of a CEfaEntity
	 */
	public final CEfaEntity getEntityRef(){
		return scadaEntity;
	}
	
	
	/**
	 * implementation of memory destructor
	 *
	 */
	public void myDestruct()
	{
		// don't destruct the var because it's a reference and not a var itself
		if(scadaEntity!=null){
			scadaEntity.myDestruct();
		}
		scadaEntity = null;
		
		
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
