package cern.c2mon.driver.ens;

import java.util.LinkedList;
import java.util.NoSuchElementException;
/**
 * Thread that will be used to:
 * 1- register entities in ScateX
 * 2- poll control queues and send controls
 *  
 * @author EFACEC
 *
 */
public class CEfaSyncQueue extends CEfaThread implements Cloneable{
	/** GateX client reference */
	private CEfaGatexClient myOwner;
	/** Queue with entities to Register */
	public LinkedList MyQueue = null;
	/**
	 * Constructor
	 * @param _myOwner - GateX client reference
	 */
	static int active=0;
	CEfaSyncQueue(CEfaGatexClient _myOwner){
		active++;
		//System.out.println("new CEfaSyncQueue = "+active);
		
		this.setName("CEfaSyncQueue");
		MyQueue = new LinkedList();

		myOwner = _myOwner;
	}

	/**
	 * Main thread cycle. It will check queues and send the messages. 
	 * If is a registration queue then it will stop when all entities are registered
	 * If is a control thread it will poll control queue in cycle and sends controls
	 */
	public void run(){
			while(bStop==false)
			{
				try { 
					sleep(1); 
				} catch (InterruptedException e) { e.printStackTrace();  }
				
				// messages 10 a 15 can have N objects!
				if (!IsEmpty() ) {
					CEfaMsg myMessage = new CEfaMsg();
					int MaxEntities = 0;
					
					// how many in queue?
					int _myQueueSize = MyQueue.size();
					
					//	message size
					if( "qDigPend".equals(currentThread().getName()) ){
						myMessage.setSType(10);
						MaxEntities = (myOwner.srvMaxBytes-6-1) / 38; 
						if (MaxEntities > _myQueueSize)
							MaxEntities = _myQueueSize;
						myMessage.setSSize(1+(38 * MaxEntities));
					}
					else if( "qAnlPend".equals(currentThread().getName()) ){
						if(myOwner.getUseJitterMsg())
							myMessage.setSType(16);
						else
							myMessage.setSType(12);
						MaxEntities = (myOwner.srvMaxBytes-6-1) / 38; 
						if (MaxEntities > _myQueueSize)
							MaxEntities = _myQueueSize;
						myMessage.setSSize(1+(38 * MaxEntities));
						myOwner.newLogMsg("Registering Analogs");
					}
					else if( "qCntPend".equals(currentThread().getName()) ){
						myMessage.setSType(14);
						MaxEntities = (myOwner.srvMaxBytes-6-1) / 38; 
						if (MaxEntities > _myQueueSize)
							MaxEntities = _myQueueSize;
						myMessage.setSSize(1+(38 * MaxEntities));
						myOwner.newLogMsg("Registering Counters");
					}
					else if( "qCtrPend".equals(currentThread().getName()) ){
						myMessage.setSType(23);
						//que coincidencia ;)
						myMessage.setSSize(23);
						MaxEntities  = 1; // commands only 1
					}
					
					// entities to send
					CEfaEntity _tempEnt = null;
					for ( int i = 0 ; i < MaxEntities ; i++){
						_tempEnt = (CEfaEntity)Get();
						myMessage.AppendEntity(_tempEnt);
					}
					
					// sendMessage pelo socket
					if ( "qCtrPend".equals(currentThread().getName()) == false)
					{
						myOwner.mySocket.SendMessage(myMessage,true);
						String s = new String();
						if (myMessage.getSType()== 10) s=" Digitals";
						if (myMessage.getSType()== 12) s=" Analogs";
						if (myMessage.getSType()== 14) s=" Counters";
						myOwner.newLogMsg("TX -> Registration"+s);
					}
					else
					{
						myOwner.mySocket.SendMessage(myMessage,false);	// no response to control
						myOwner.newLogMsg("TX -> Control");
//						 save this control in a "sent control list", so that the response will match this
						try {
							CEfaEntityCtrResp ctrResp = new CEfaEntityCtrResp();
							ctrResp.setId(_tempEnt.getSxId(), "");
							ctrResp.setOrder(((CEfaEntityCtr) _tempEnt).getOrder());
							ctrResp.setMsgNum(myMessage.getSNum());
							myOwner.mySocket.llSentControl.add(ctrResp);
						} catch (Exception e) {
							myOwner.newErrMsg("Error Saving Sent Control");
							
						}
					}
					// free mem
					if(myMessage!=null)
						myMessage.myDestruct();
					myMessage = null;
			} 
			else {//empty
				// only control queue will remain alive
				if ( "qCtrPend".equals(currentThread().getName()) == false){
					//stop();
					break;	// breaks while and exits normally...
				}	
			}
		}
	}
	
	/**
	 * Adds entity to queue
	 * @param o - object to add
	 */
	public synchronized void Add( Object o) { MyQueue.addLast(o); }
	/**
	 * Gets an object fom queue
	 * @return a reference of an object or null if queue is empty
	 */
	public synchronized Object Get() {
		Object _temp;
		try{
			_temp = MyQueue.getFirst(); 
			MyQueue.removeFirst();
			return _temp;	
		}
		catch(NoSuchElementException e){
			
			return null;
		}
	}
	/**
	 * Checks if queue is empty
	 * @return true if queue is empty
	 */
	public boolean IsEmpty() { return MyQueue.isEmpty(); }

	/**
	 * implementation of memory destructor
	 *
	 */
	public void myDestruct()
	{
		// free mem
		myOwner = null;
		if(MyQueue!=null)
		{
			// don't delete what is in the list because it's pointers to vars and not vars itself
			MyQueue = null;
		}
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