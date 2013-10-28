package cern.c2mon.driver.ens;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

/**
 * Class that will encapsulate messages exchanged between client and server
 * @author EFACEC
 *
 */public class CEfaMsg extends Object {
 	
	/** Is a Handshake msg */
	public boolean IamHandshakeOK=false;
	/** Is a WDog msg */
	public boolean IAmWDogOk=false;
	/** Is a ACK msg */
	public boolean IAmACK=false;
	/** Is a NACK msg */
	public boolean IAmNACK=false;
	/** Is a Lost of Info msg */
	public boolean IAmLostInfo=false;
	
	/** Manager Reference */
	private CEfaSocketManager myOwner = null;
	/**
	 * Sets manager
	 * @param _owner - manager reference
	 */
	public final void setOwner(CEfaSocketManager _owner)
	{
		myOwner = _owner;
	}
	
	static int active=0;
 	public CEfaMsg(){
		active++;
		//System.out.println("new CEfaMsg = "+active);	
 	}
	
	/** message number */
	private int sNum;

	/**
	 * Gets message number
	 * @return message number
	 */
	public final int getSNum() {
		return sNum;
	}

	/**
	 * Sets message number
	 * @param _value - msg number
	 */
	public final void setSNum(int _value) {
		sNum = _value;
	}

	/** Message Type */
	private int sType;

	/**
	 * Gets Message Type
	 * @return Message Type
	 */
	public final int getSType() {
		return sType;
	}

	/**
	 * Sets Message Type
	 * @param _value - Message Type
	 */
	public void setSType(int _value) {
		sType = _value;
	}

	/** Message size */
	private int sSize;

	/**
	 * Gets Message size
	 * @return Message size
	 */
	public int getSSize() {
		return sSize;
	}

	/** 
	 * Sets Message size
	 * @param _value - Message size
	 */
	public void setSSize(int _value) {
		sSize = _value;
	}
	
	/** bytes received */
	private byte[] Bytes = null;
	/** message to send */
	private ByteBuffer msg;	

	/**
	 * Returns message ready to send
	 * @return array with message to send
	 */
	public byte[] getMessageToSend() {
		return msg.array();
	}

	/**
	 * Bytes Received
	 * @param _value - appends bytes to reception array
	 */
	public void appendBytes(byte[] _value) {
		Bytes = _value;
	}

	/**
	 * Entity of this message
	 */
	private C_Efa_Structures myCurrentEntity = null;
	/**
	 * Add only one entity to this message
	 * @param _value
	 */
	public void AddEntity(C_Efa_Structures _value){
		myCurrentEntity = _value;
	}
	
	/**
	 * List with entities to append to message
	 */
	private LinkedList llEntities;
	/**
	 * Add new entity to message
	 * @param _value - entity reference
	 */
	public void AppendEntity(CEfaEntity _value)
	{
		if (llEntities == null)
			llEntities = new LinkedList();
		
		llEntities.addLast(_value);
	}
	
	/**
	 * Process Message that was received in Bytes and constructs a new object
	 * @return true if correctly processed
	 */
	public boolean procMessage(){
		// decode msg that is in bytecode, 
		if (Bytes!=null)
		{
			msg=ByteBuffer.wrap(Bytes);
			//Set to little endian
			msg.order(ByteOrder.LITTLE_ENDIAN);
		}

		// if there are entities, add it to event queue
		switch (sType){
			case 0:
				// handshake ok?
				try {
					if (Bytes[0] == 65)
						IamHandshakeOK = true;
						return true;
				} catch (Exception e) {
					
				}
				return false;
			case 2:
				// wdog refresh
				IAmWDogOk=true;
				return true;
			case 4:
				// sync
				// not to implement
				return true;
			case 11:
				// digital reg info
				if(myOwner!=null){
					//String strLog = new String("RX <- Registration Dig");
					//myOwner.getGatexClient().newLogMsg(strLog);
					//strLog = null;
				}
				return ProcessRegDigMsg();
			case 13:
				// analog reg info
				if(myOwner!=null){
					//String strLog = new String("RX <- Registration Anl");
					//myOwner.getGatexClient().newLogMsg(strLog);
					//strLog = null;
				}
				return ProcessRegAnlMsg();
			case 15:
				// counter reg info
				if(myOwner!=null){
					//String strLog = new String("RX <- Registration Cnt");
					//myOwner.getGatexClient().newLogMsg(strLog);
					//strLog = null;
				}
				return ProcessRegCntMsg();
			case 33:
				//ctr execute
				if(myOwner!=null){
					//String strLog = new String("RX <- Control Response");
					//myOwner.getGatexClient().newLogMsg(strLog);
					//strLog = null;
				}
				return ProcessCtrResponseMsg();
			case 40:
				// dig value event
				if(myOwner!=null){
					//String strLog = new String("RX <- Event DIG");
					//myOwner.getGatexClient().newLogMsg(strLog);
					//strLog = null;
				}
				return ProcessEventMsg(CEfaGatexClient.GX_EVETYP_DIG);
			case 41:
				// ana value event
				if(myOwner!=null){
					/*String strLog = new String("RX <- Event ANL");
					myOwner.getGatexClient().newLogMsg(strLog);
					strLog = null;*/
				}
				return ProcessEventMsg(CEfaGatexClient.GX_EVETYP_ANL);
			case 42:
				// cnt value event
				if(myOwner!=null){
					/*String strLog = new String("RX <- Event CNT");
					myOwner.getGatexClient().newLogMsg(strLog);
					strLog = null;*/
				}
				return ProcessEventMsg(CEfaGatexClient.GX_EVETYP_CNT);
			case 253:
				// lost info
				IAmLostInfo=true;
				return true;
			case 254:
				// fail
				IAmNACK=true;
				return true;
			case 255:
				//ack
				IAmACK=true;
				return true;
			default:
				//msg desconhecida
				return false;			
		}
	}
	
	/**
	 * Serialize Message to send to ScateX
	 * @return true if correctly processed
	 */
	public boolean serializeMessage(){
		// send contents to private variavle "msg"
		if (myCurrentEntity != null)	// if currentEntity is null, msg is about multiple entitys 
		{								// in linked queues the size is already set 
			sSize = myCurrentEntity.getTam();
		}
		
		msg = ByteBuffer.allocate(6 + sSize);	// 6  o Header
		//Set to little endian
		msg.order(ByteOrder.LITTLE_ENDIAN);
		
//		 sNum, sType e sSize
		msg.putShort((short) sNum);
		msg.putShort((short) sType);
		msg.putShort((short) sSize);
		
		// decode msg e make append to msg
		switch (sType){
			case 1: 
				// wdog registry
				myCurrentEntity.getSerialized(msg);
				return true;
			case 3:
				// request sync
				// not to implement
				return true;
			case 4:
				// handshake
				// is only 1 byte
				msg.put((byte) 255);
				return true;
			case 5:
				// registration init
//				 is only 1 byte
				msg.put((byte) 255);
				return true;
			case 6:
				// registration end
//				 is only 1 byte
				msg.put((byte) 255);				
				return true;
			case 10:
				// register digital
				// let see the linked queue...
				// 1  byte says how many objects we have:
				msg.put((byte)llEntities.size());
				// for all the list, ask each obj to serialize 
				S_ENT_REGINFO ent = new S_ENT_REGINFO();
				CEfaEntityDig dig = null; 
				while(llEntities.isEmpty() == false)
				{
					//dig = (CEfaEntityDig)llEntities.getFirst();
					dig = (CEfaEntityDig)llEntities.removeFirst();
					ent.setCaTag( dig.getSxId().toCharArray() );
					ent.getSerialized(msg);
				}
				if(ent!=null) 
				{
					ent.myDestruct();
					ent = null;
				}
				
				return true;
			case 12:
			case 16:	// register analog with jitter
				// register analog
				// let see the linked queue...
				// 1  byte says how many objects we have:
				msg.put((byte)llEntities.size());
				// for all the list, ask each obj to serialize 
				S_ENT_REGINFO ent1 = new S_ENT_REGINFO();
				CEfaEntityAnl anl = null; 
				while(llEntities.isEmpty() == false)
				{
					//anl = (CEfaEntityAnl)llEntities.getFirst();
					anl = (CEfaEntityAnl)llEntities.removeFirst();
					ent1.setCaTag( anl.getSxId().toCharArray() );
					ent1.getSInfo().getSDpValue().setFValue(anl.fGetDeadBand());
					ent1.getSerialized(msg);
				}
				if(ent1!=null){
					ent1.myDestruct();
					ent1 = null;
				}
				return true;
			case 14:
				// register counter
				// let see the linked queue...
				// 1  byte says how many objects we have:
				msg.put((byte)llEntities.size());
				// for all the list, ask each obj to serialize 
				S_ENT_REGINFO ent2 = new S_ENT_REGINFO();
				CEfaEntityCnt cnt = null; 
				while(llEntities.isEmpty() == false)
				{
					//cnt = (CEfaEntityCnt)llEntities.getFirst();
					cnt = (CEfaEntityCnt)llEntities.removeFirst();
					ent2.setCaTag( cnt.getSxId().toCharArray() );
					ent2.getSerialized(msg);
				}
				if(ent2!=null){
					ent2.myDestruct();
					ent2 = null;
				}
				
				return true;
			case 23:
				// ask ctr execute
				// let see the linked queue...
				// for all the list, ask each obj to serialize 
				S_CNT_RQT ent_ctr = new S_CNT_RQT();
				CEfaEntity ctr = null; 
				if(llEntities.isEmpty() == false)
				{					
					//ctr = (CEfaEntity)llEntities.getFirst();
					ctr = (CEfaEntity)llEntities.removeFirst();
					ent_ctr.setCaTag( ctr.getSxId().toCharArray() );
					if (CEfaEntityCtr.class.isInstance(ctr))
					{
						ent_ctr.setSControlType(CEfaGatexClient.GX_COMMAND);
					}
					else
						ent_ctr.setSControlType(CEfaGatexClient.GX_SETPOINT);
					// value
					ent_ctr.setFSetPointValue(ctr.fGetValue());
					ent_ctr.getSerialized(msg);	
				}
				if(ent_ctr!=null){
					ent_ctr.myDestruct();
					ent_ctr = null;
				}
				
				return true;
			case 43:
				// send dig value
				return true;
			case 44:
				// send ana value
				return true;
			case 45:
				// send cnt value event
				return true;
			case 254:
				// fail
//				 nothing to send as data, only header
				return true;
			case 255:
				//ack
//				 is only 1 byte
				msg.put((byte) 0);
				return true;
			default:
				//msg unknown
				return false;			
		}
	}
	
	/**
	 * Process Received Message of Digital Registration
	 * @return true if correctly processed
	 */
	public boolean ProcessRegDigMsg()
	{
		// we can have several entitys here
		// create the entity and serialize and put in queues
		S_ENT_REGINFO ent = new S_ENT_REGINFO();
		// how much entitys ?
		int numEnt;
		try {
			numEnt = (int)msg.get();
			for (int i = 0; i < numEnt; i++) {
				if (ent.enqueue(msg) == true) {
					// set the kidx of digital for hash key
					if(myOwner!=null)
					{
						CEfaEntity dig;
						String s = new String(ent.getCaTag());
						dig = myOwner.GetEntityBySxId(CEfaGatexClient.GX_EVETYP_DIG, s);
						if (dig!=null)
						{
							// set kidx
							dig.setKidx(ent.getSInfo().getNKidx());
							// System.out.println("kidx:"+ent.getSInfo().getNKidx());
							// keep in hash table
							myOwner.SaveEntityInHash(CEfaGatexClient.GX_EVETYP_DIG, dig);
							// create event only in situations:
							// a) bSetXXX has changed something
							// b) is not change, but it was failed  (do not make event, if 2 servers has comuted in less of x seconds Xseg->waitTime)
							boolean bDoEvent = false;
							if (ent.getSInfo().getNKidx()>0)
							{
								if (dig.bSetInvCode((byte) ent.getSInfo().getEquivalentStatus())) bDoEvent = true;
							}
							else
							{
								dig.bSetInvCode(CEfaEntity.EFAENT_INVTAG);	// tag invalid!
								bDoEvent = true;
							}
							if (dig.bSetValue((float) ent.getSInfo().getSDpValue().getNValue())) bDoEvent = true;
							if (dig.bSetTTag(ent.getSInfo().getSEvTtag().getTimetag().getISecs(), ent.getSInfo().getSEvTtag().getTimetag().getSMsecs()))
							{
								// note: do note make event because TTag is of the register 
								// bDoEvent = true;
							}
							
							if (myOwner.bRegDoEvent || bDoEvent)
							{
								// create event
								CEfaEvent eve = new CEfaEvent();
								eve.setEntityRef(dig);
								myOwner.GenerateEvent(CEfaGatexClient.GX_EVETYP_DIG, eve);
							}							
						}
					}
				}
			}
			
			return true;
			
		} catch (Exception e) {
			if(myOwner!=null){
				myOwner.getGatexClient().newErrMsg("Erro ao processar Mensagem de Registo de Digitais");
			}
			
			return false;
		}
		
	}
	
	/**
	 * Process Received Message of Analog Registration
	 * @return true if correctly processed
	 */
	public boolean ProcessRegAnlMsg()
	{
		// we can have several entitys here
		// create the entity and serialize and put in queues
		S_ENT_REGINFO ent = new S_ENT_REGINFO();
		// how much entitys ?
		int numEnt;
		try {
			numEnt = (int)msg.get();
			for (int i = 0; i < numEnt; i++) {
				if (ent.enqueue(msg) == true) {
					// set the kidx of anl for hash key
					if(myOwner!=null)
					{
						CEfaEntity anl;
						anl = myOwner.GetEntityBySxId(CEfaGatexClient.GX_EVETYP_ANL, String.valueOf(ent.getCaTag()));
						if (anl!=null)
						{
							// set kidx
							anl.setKidx(ent.getSInfo().getNKidx());
							// keep in hash table
							myOwner.SaveEntityInHash(CEfaGatexClient.GX_EVETYP_ANL, anl);
							// create event only in situations:
							// a) bSetXXX has changed something
							// b) is not change, but it was failed  (do not make event, if 2 servers has comuted in less of x seconds Xseg->waitTime)
							boolean bDoEvent = false;
							if (ent.getSInfo().getNKidx()>0){
								if (anl.bSetInvCode((byte) ent.getSInfo().getEquivalentStatus())) bDoEvent = true;
							}
							else
							{
								anl.bSetInvCode(CEfaEntity.EFAENT_INVTAG);	// tag invalid!
								bDoEvent = true;
							}
							if (anl.bSetValue((float) ent.getSInfo().getSDpValue().getFValue())) bDoEvent = true;
							if (anl.bSetTTag(ent.getSInfo().getSEvTtag().getTimetag().getISecs(), ent.getSInfo().getSEvTtag().getTimetag().getSMsecs()))
							{
								// note: do note make event because TTag is of the register 
								// bDoEvent = true;
							}
							
							if (myOwner.bRegDoEvent || bDoEvent)
							{
								// create event
								CEfaEvent eve = new CEfaEvent();
								eve.setEntityRef(anl);
								myOwner.GenerateEvent(CEfaGatexClient.GX_EVETYP_ANL, eve);
							}							
						}
					}
				}
			}
			
			return true;
			
		} catch (Exception e) {
			
			return false;
		}	
	}
	
	/**
	 * Process Received Message of Counter Registration
	 * @return true if correctly processed
	 */
	public boolean ProcessRegCntMsg()
	{
		// we can have several entitys here
		// create the entity and serialize and put in queues
		S_ENT_REGINFO ent = new S_ENT_REGINFO();
		// how much entitys ?
		int numEnt;
		try {
			numEnt = (int)msg.get();
			for (int i = 0; i < numEnt; i++) {
				if (ent.enqueue(msg) == true) {
					// set the kidx of cnt for hash key
					if(myOwner!=null)
					{
						CEfaEntity cnt;
						cnt = myOwner.GetEntityBySxId(CEfaGatexClient.GX_EVETYP_CNT, String.valueOf(ent.getCaTag()));
						if (cnt!=null)
						{
							// set kidx
							cnt.setKidx(ent.getSInfo().getNKidx());
							// keep in hash table
							myOwner.SaveEntityInHash(CEfaGatexClient.GX_EVETYP_CNT, cnt);
							// create event only in situations:
							// a) bSetXXX has changed something
							// b) is not change, but it was failed  (do not make event, if 2 servers has comuted in less of x seconds Xseg->waitTime)
							boolean bDoEvent = false;
							if(ent.getSInfo().getNKidx()>0){
								if (cnt.bSetInvCode((byte) ent.getSInfo().getEquivalentStatus())) bDoEvent = true;
							}
							else
							{
								cnt.bSetInvCode(CEfaEntity.EFAENT_INVTAG);	// tag invalid!
								bDoEvent = true;
							}
							if (cnt.bSetValue((float) ent.getSInfo().getSDpValue().getFValue())) bDoEvent = true;
							if (cnt.bSetTTag(ent.getSInfo().getSEvTtag().getTimetag().getISecs(), ent.getSInfo().getSEvTtag().getTimetag().getSMsecs()))
							{	
								// note: do note make event because TTag is of the register 
								// bDoEvent = true;
							}
							
							if (myOwner.bRegDoEvent || bDoEvent)
							{
								// create event
								CEfaEvent eve = new CEfaEvent();
								eve.setEntityRef(cnt);
								myOwner.GenerateEvent(CEfaGatexClient.GX_EVETYP_CNT, eve);
							}							
						}
					}
				}
			}
			
			return true;
			
		} catch (Exception e) {
			
			return false;
		}	
	}
	
	/**
	 * Processed received Event Message
	 * @param _type - type of event
	 * @return true if correctly processed
	 */
	public boolean ProcessEventMsg(byte _type)
	{
		try {
			S_DP_UPD ent = new S_DP_UPD();
			if (ent.enqueue(msg) == true)
			{
				// find throutgh the kidx in hash
				if(myOwner!=null)
				{
					CEfaEntity efa_ent = null;
					efa_ent = myOwner.GetEntityFromHash(_type, ent.getNKidx());
					if(efa_ent != null)
					{
						efa_ent.bSetInvCode((byte) ent.getEquivalentStatus());
						if (_type == CEfaGatexClient.GX_EVETYP_DIG)
							efa_ent.bSetValue((float) ent.getSDpValue().getNValue());
						else
							efa_ent.bSetValue((float) ent.getSDpValue().getFValue());
						efa_ent.bSetTTag(ent.getSEvTtag().getTimetag().getISecs(), ent.getSEvTtag().getTimetag().getSMsecs());
						
						// create evento and put in queue
						CEfaEvent eve = new CEfaEvent();
						eve.setEntityRef(efa_ent);
						myOwner.GenerateEvent(_type, eve);
						// free mem
						if(ent!=null)
							ent.myDestruct();
						ent = null;
						return true;
					}
				}
			}
			// free mem
			if(ent!=null)
				ent.myDestruct();
			ent = null;
		} catch (Exception e) {
			
			return false;
		}
		return false;
	}
	
	/**
	 * Process Response to a Control Message
	 * @return true if correctly processed
	 */
	public boolean ProcessCtrResponseMsg(){
		try {
			S_CNT_REPLY ent = new S_CNT_REPLY();
			if (ent.enqueue(msg) == true)
			{
				// create new answer
				if(myOwner!=null)
				{
					//CEfaEntityCtrResp efa_ent = new CEfaEntityCtrResp();
					CEfaEntityCtrResp efa_ent = null;
					// get from list of sent controls
					if(myOwner.llSentControl.isEmpty()==false)
					{
						try {
							for (int i = 0; i < myOwner.llSentControl.size(); i++) {
								CEfaEntityCtrResp ctrResp = (CEfaEntityCtrResp) myOwner.llSentControl.get(i); 
								if (ctrResp!= null && ctrResp.getMsgNum() == this.getSNum()) {
									efa_ent = (CEfaEntityCtrResp) myOwner.llSentControl.remove(i);
									break;
								}
							}
						} catch (Exception e) {
							myOwner.getGatexClient().newErrMsg("Error Getting Sent Control");
							
						}
						if(efa_ent != null)
						{
							//efa_ent.setId(String.valueOf(ent.getCTag()),"");
							efa_ent.setResponce(ent.getCCntReply());
							// create event and put in queue
							CEfaEvent eve = new CEfaEvent();
							eve.setEntityRef(efa_ent);
							myOwner.GenerateEvent(CEfaGatexClient.GX_EVETYP_CTR_RESP, eve);
							// free mem
							if(ent!=null)
								ent.myDestruct();
							ent = null;
							return true;
						}
					}
				}
			}
			// free mem
			if(ent!=null)
				ent.myDestruct();
			ent = null;
		} catch (Exception e) {
			
			return false;
		}
		return false;
	}
	
	/**
	 * implementation of memory destructor
	 *
	 */
	public void myDestruct()
	{
		// free mem
		myOwner = null;
		Bytes = null;
		
		if(llEntities != null)
		{
			// don't delete what is in the list because it's pointers to vars and not vars itself
			llEntities = null;
		}
		
		if(myCurrentEntity!=null){
			myCurrentEntity.myDestruct();
			myCurrentEntity = null;
		}
	
		msg = null;
	
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
