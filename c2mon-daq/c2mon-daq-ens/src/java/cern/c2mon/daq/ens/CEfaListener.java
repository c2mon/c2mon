/*
 * Created on May 1, 2005
 *
 */
package cern.c2mon.daq.ens;


/**
 * Thread that will Listen communications with ScateX
 * @author EFACEC
 *
 */
public class CEfaListener extends CEfaThread{

	/** GateX Client */
	private CEfaGatexClient myOwner=null;
	/** Communication Message */
	private CEfaMsg myMsg=null;
	/**
	 * Constructor
	 * @param _myOwner - GateX Client
	 */
	static int active =  0;
	CEfaListener(CEfaGatexClient _myOwner){
		active++;
		//System.out.println("new CEfaListener = "+active);
		
		myOwner = _myOwner;
		myMsg = null;
		this.setName("CEfaListener");
		//start();
		//stop();
	}

	/**
	 * Main listening thread method
	 */
	public void run(){
		while (bStop==false){
			try {
				// free previous mem
				if (myMsg!=null) 
				{
					myMsg.myDestruct();
					myMsg = null;
				}
				
				// READ //
				myMsg = myOwner.mySocket.GetMessage();
				
//				Process image and send to event linked lists!!!!
				if (myMsg!=null) 
				{
					myMsg.setOwner(myOwner.mySocket);
					//myOwner.mySocket.bReceivedResponse = true;
				}
			
				// something read correctly?
				if ((myMsg!=null) && myMsg.procMessage()){
					if((myMsg.getSType()!=253)&&(myMsg.getSType()!=40)&&(myMsg.getSType()!=41)&&(myMsg.getSType()!=42))
					{
						myOwner.mySocket.bReceivedResponse = true;
						myOwner.mySocket.resetRetries();
					}
					else
					{
						//System.out.println("rx a event or lost info with num:"+myMsg.getSNum());
					}
					// msg well received - any extra processing?
					// any well known message? Tell to socketmanager
					if (myMsg.IamHandshakeOK == true){
						myOwner.mySocket.bReceivedHandshakeOK = true;
					}
					if (myMsg.IAmWDogOk == true)
					{
						//myOwner.newLogMsg("Received WDog OK"); 
						myOwner.mySocket.bReceivedWDogOK = true;
					}
					if (myMsg.IAmACK == true){
						myOwner.mySocket.bReceivedACK = true;
					}
					if (myMsg.IAmNACK == true){
						myOwner.mySocket.bReceivedNACK = true;
					}
					if (myMsg.IAmLostInfo == true)
					{
						myOwner.newLogMsg("Received Lost Info - Switching Server"); 

						// comm failure, init timer of x sef, if ends give failure
						myOwner.mySocket.setWaitTime(0);
						myOwner.mySocket.setConnectionStatus(false);
					}

					// rx events or lost info, send ack
					if((myMsg.getSType()==253)||(myMsg.getSType()==40)||(myMsg.getSType()==41)||(myMsg.getSType()==42))
					{
						CEfaMsg myMessage = new CEfaMsg();
						S_ACK_NACK msg = new S_ACK_NACK();
						myMessage.AddEntity(msg);
						myMessage.setSNum(myMsg.getSNum());	// we have to answer with same number
						myMessage.setSType(255);// ack
						myOwner.mySocket.SendMessage(myMessage, false);	// false-> it's an answer, I will not wait for any response
						// free mem
						if(msg!=null){
							msg.myDestruct();
						}
						msg = null;
						if(myMessage!=null){
							myMessage.myDestruct();
						}
						myMessage = null;
					}
				}
				else
				{
					//msg invalid or unknown
				}

			} catch (Exception e1) {
				//e1.printStackTrace();
			}
			
			try {
				sleep(1);
			} catch (Exception e) {
				
			}
		}
	}
	
	/**
	 * implementation of memory destructor
	 *
	 */
	public void myDestruct()
	{
		// free mem
		myOwner = null;
		if(myMsg!=null){
			myMsg.myDestruct();
		}
		myMsg = null;
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
