/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2008  CERN
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.daq.ens;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;

import java.net.Socket;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.HashMap;
import java.util.LinkedList;


/**
 * Main Thread Class that will manage all communications with ScateX Servers
 * @author EFACEC
 *
 */
public class CEfaSocketManager extends CEfaThread {
	/** GateX Client reference */
	private CEfaGatexClient myOwner;
	/**
	 * Get GateX reference
	 * @return GateX client reference
	 */
	public final CEfaGatexClient getGatexClient(){
		return myOwner;
	}
	/** Socket Variable */
	private Socket mySocketScateX = null;
	/** Input stream - receives messages from socket  */
	private DataInputStream input = null;
	/** Output stream - sends messages from socket  */
	private DataOutputStream output = null;
	/** Flag to indicate if socket is in use */
	private Thread MySocketBusy = null;
	/** Response timeout internal var */
	private int iTimeout;
	/** Message Number to Send */
	private static short messageNumber = -1;
	/** Indicates the server that is currently connected */
	private String srvConnected="";
	
	/** wait time internal variable */
	private int waitTime = 0;
	/**
	 * set wait tim
	 * @param _tim - time
	 */
	public final void setWaitTime(int _tim){waitTime = _tim;}
	/** Manager is connected to a server */
	private boolean bIAmConnected = false; // when is false, will start attempt to connect
	/** 
	 * Sets if manager is connected to a server
	 * @param _isConnected - is connect
	 */
	public final void setConnectionStatus(boolean _isConnected){bIAmConnected=_isConnected;}
	/** Is doing Registration cycle */
	private boolean bIsRegistering= false; // is registering, wait queues to be empty
	
	/** Received a Response */
	public boolean bReceivedResponse;
	/** Received Handshake */
	public boolean bReceivedHandshakeOK;
	/** Received WDog Response */
	public boolean bReceivedWDogOK;
	/** Received ACK*/
	public boolean bReceivedACK;
	/** Received NACK */
	public boolean bReceivedNACK;
	/** Internal flag to indicates if is to generate events when connecting to a server */
	public boolean bRegDoEvent=true;
	/** number of timeouts of server - if is over maxretires  it will switch servers */
	public int iRetries = 0;
	/**
	 * increments the number of retries of communication with server
	 * @return number of retries
	 */
	public int incRetries(){
		iRetries++;
		return (iRetries);
	}
	/**
	 * Resets the number of retries
	 *
	 */
	public final void resetRetries(){iRetries=0;}
	/** Sent control List */
	LinkedList llSentControl;
	/**
	 * Constructor
	 * @param _myOwner - Gatex client reference
	 */
	static int active=0;
	public CEfaSocketManager(CEfaGatexClient _myOwner) {
		active++;
		//System.out.println("new CEfaSocketManager = "+active);
		
		this.setName("CEfaSocketManager");
		myOwner = _myOwner;
	}
	 /**
	  * Returns message number to send
	  * @return Message number to send
	  */
	 public synchronized short getNextMessageNumber(){
		return ++messageNumber;
	}
	
	/**
	 * Reads From Socket Message Sent from ScateX
	 * @return CEfaMsg Message
	 */
	public CEfaMsg GetMessage() throws EOFException{
		 
		final int HEADER_LENG = 6 ;
		byte[ ] headerArray = new byte[HEADER_LENG] ;
		int msgreadsofar = 0 ;
		int msgread = 0 ;
		int msgLength = 0 ;
		do
		{
			try {
				msgread = input.read(headerArray,msgreadsofar, HEADER_LENG - msgreadsofar) ;
			} catch (Exception e1) {
//				 failed reading!
				if(bIAmConnected)
					myOwner.newErrMsg("COMMS FAILED");
//				 comms fails... init timer ... after this timer will give as failed
				waitTime = 0;
				bIAmConnected = false;
				headerArray = null;
		
				return null;
			}
			msgreadsofar += msgread ;
		}while ((msgread != -1) && (msgreadsofar < HEADER_LENG)) ;
		
		// nothing to read?
		if (msgread==-1)
		{
			headerArray = null;
			return null;
		}

		// 2005.06.21
		CEfaMsg myMessage = new CEfaMsg();
		
//		Create an instance of ByteBuffer and pass on the header read as byte array
		ByteBuffer header = ByteBuffer.wrap(headerArray);
		//Set to little endian
		header.order(ByteOrder.LITTLE_ENDIAN);
		
		try {
			myMessage.setSNum(header.getShort()); // sNum
			myMessage.setSType(header.getShort()); // sType
			msgLength = header.getShort(); // sSize
			myMessage.setSSize(msgLength);
		} catch (Exception e) {
			
			return null;
		}
		
		// is there anything else to read
		if (msgLength==0)
			return myMessage;
			
//		Set the object to null
		headerArray = null ;
		msgreadsofar = 0 ;
		msgread = 0 ;
		
//		Now once the message length is obtained, use this to get the actual data
		byte[] dataArray = new byte[msgLength] ;
		do{
			try {
				msgread = input.read(dataArray,msgreadsofar, msgLength - msgreadsofar) ;
			} catch (Exception e1) {
//				 fail to read!!
				if(bIAmConnected)
					myOwner.newErrMsg("FALHA COMMS");
//				 comms fails... init timer ... after this timer will give as failed
				waitTime = 0;
				bIAmConnected = false;
				dataArray = null;
				if(myMessage!=null)
					myMessage.myDestruct();
				myMessage = null;
				
				return null;
			}
			msgreadsofar += msgread ;
		}while ((msgread != -1) && (msgreadsofar < msgLength)) ;
		
//		 nothing to read?
		if (msgread==-1)
		{
			if(myMessage!=null)
				myMessage.myDestruct();
			myMessage = null;
			dataArray = null;
			return null;
		}
		
		// append bytes received
		myMessage.appendBytes(dataArray);
		
		return myMessage;
	}

	/**
	 * Send Message through Socket to ScateX - it will wait socket free to send
	 * @param _value - message to send
	 * @param bWaitResponse - waits a response from ScateX
	 * @return true if send ok and received response if was to wait response
	 */
	public boolean SendMessage(CEfaMsg _value, boolean bWaitResponse){
		if (bWaitResponse) LockMySocket();
		try {
			if (bWaitResponse)
				bReceivedResponse = false;
			// it's locked, get message number
			// note: if is answer to scateX, we have already number
			// because must be the same it send us
			if(_value.getSType() != 255)	// no number yet...
				_value.setSNum(getNextMessageNumber());
			if ( _value.serializeMessage() ){
				output.write(_value.getMessageToSend());
			} else 
			{
				if (bWaitResponse)
					UnlockMySocket();
				return false;
			}
			
			// wait response or timeout
			if (bWaitResponse)
			{
				int iLocTimeout = 0;
				while(bReceivedResponse==false)
				{
					sleep(1);
					iLocTimeout+=1;
					if (iLocTimeout > myOwner.srvTimeout*1000)
					{
						myOwner.newLogMsg("Timeout!");
						UnlockMySocket();
						if(myOwner.mySocket.incRetries() >= myOwner.getMaxRetries()){
							// Comms Fails!
							if(bIAmConnected)
								myOwner.newLogMsg("Max Retries! Swithing Server!");
//							  comms fails... init timer ... after this timer will give as failed
							waitTime = 0;
							bIAmConnected = false;							
						}
						return false;
					}
					
					resetRetries();
					if(bIAmConnected==false){
						UnlockMySocket();
						return false;
					}
				}
			}
		} catch (Exception e) {
			// sent failed!!!!
			if (bWaitResponse)
				UnlockMySocket();
			// failed:
			if(bIAmConnected)
				myOwner.newErrMsg("COMMS Failed");
//			 falha de comms... init timer ... after this timer will give as failed
			waitTime = 0;
			bIAmConnected = false;
			
			return false;
		}
		
		// ok
		if (bWaitResponse)
			UnlockMySocket();
		return true;
	}
		
	/**
	 * Locks Socket to send
	 *
	 */
	private void LockMySocket(){
		while (true) {
			synchronized(this) {
				if ( MySocketBusy == null ){
					MySocketBusy = Thread.currentThread();
					break;
				}
			}
			try {
				Thread.sleep(1);
			} catch (Exception e) { }
		}
	}
	/**
	 * Unlock Socket to send
	 *
	 */
	private void UnlockMySocket(){
		if (MySocketBusy == Thread.currentThread()) {
			MySocketBusy = null;
		}
	}
	
	/**
	 * Send Handshake Message
	 * @return true if send ok and received correctly response
	 */
	private boolean handshaking(){
		//	send handshaking
		CEfaMsg myMessage = new CEfaMsg();
		S_HANDSHAKE hs = new S_HANDSHAKE();
		myMessage.AddEntity(hs);
		myMessage.setSType(4);
		bReceivedHandshakeOK = false;	// to wait answer
		SendMessage(myMessage, true);
		myOwner.newLogMsg("TX -> Send Handshaking"); 
		// free mem
		if(myMessage!=null)
			myMessage.myDestruct();
		myMessage = null;
		hs = null;
		
		// wait answer...
		iTimeout = 0;
		while(bReceivedHandshakeOK==false)
		{
			try {
				// timeout?
				sleep(10);
				iTimeout+=10;
				if (iTimeout > 5000)
					return false;	// timeout !
			} catch (InterruptedException e) {
				
			}
		}
		
		// answer received - handshake OK
		myOwner.newLogMsg("RX <- Handshake Received OK"); 
		return true;
	}
	
	/**
	 * Send Registration Init Message
	 * @return true if send ok and received correctly response
	 */
	private boolean reginit(){
		//	send reginit
		CEfaMsg myMessage = new CEfaMsg();
		S_REGINIT reg = new S_REGINIT();
		myMessage.AddEntity(reg);
		myMessage.setSType(5);
		bReceivedACK = false;	
		SendMessage(myMessage, true);
		myOwner.newLogMsg("TX -> Send RegInit"); 
		// free mem
		if(myMessage!=null)
			myMessage.myDestruct();
		myMessage = null;
		reg = null;
		
		// wait answer...
		iTimeout = 0;
		while(bReceivedACK==false)
		{
			try {
				// timeout?
				sleep(10);
				iTimeout+=10;
				if (iTimeout > 5000)
					return false;	// timeout!
			} catch (InterruptedException e) {
				
			}
		}
		
		// received answer OK
		myOwner.newLogMsg("RX <- Received ACK"); 
		return true;
	}
	
	/**
	 * Send Registration End Message
	 * @return true if send ok and received correctly response
	 */
	private boolean regend(){
		//	send regend
		CEfaMsg myMessage = new CEfaMsg();
		S_REGEND reg = new S_REGEND();
		myMessage.AddEntity(reg);
		myMessage.setSType(6);
		bReceivedACK = false;	
		SendMessage(myMessage, true);
		myOwner.newLogMsg("TX -> Send RegEnd"); 
		// free mem
		if(myMessage!=null)
			myMessage.myDestruct();
		myMessage = null;
		reg = null;
		
		// wait answer...
		iTimeout = 0;
		while(bReceivedACK==false)
		{
			try {
				// timeout?
				sleep(10);
				iTimeout+=10;
				if (iTimeout > 5000)
					return false;	// timeout !
			} catch (InterruptedException e) {
				
			}
				
		}
		
		// received answer OK
		myOwner.newLogMsg("RX <- Received ACK"); 
		return true;
	}
	
	/**
	 * Returns a reference of an entity of the specified ScateX Tag from the Tag Hash table
	 * @param _type - type of entity
	 * @param _sxId - ScateX tag
	 * @return a entity reference or null if not found any with that tag
	 */
	public CEfaEntity GetEntityBySxId(byte _type, String _sxId)
	{
		try {
			HashMap hm = null;
			if (_type == CEfaGatexClient.GX_EVETYP_DIG)
				hm = myOwner.digTagHash;
			if (_type == CEfaGatexClient.GX_EVETYP_ANL)
				hm = myOwner.anlTagHash;
			if (_type == CEfaGatexClient.GX_EVETYP_CNT)
				hm = myOwner.cntTagHash;
			
			if(hm!=null)
			{
				return (CEfaEntity)(hm.get(_sxId));
			}

		} catch (Exception e) {
			
			return null;
		}
		return null;
	}
	
	/**
	 * Saves an entity in the Tag Hash table
	 * @param _type - type of th entity
	 * @param ent - entity reference to save
	 */
	public void SaveEntityInHash(byte _type, CEfaEntity ent)
	{
		HashMap hm = null;
		if (_type == CEfaGatexClient.GX_EVETYP_DIG)
			hm = myOwner.digHash;
		if (_type == CEfaGatexClient.GX_EVETYP_ANL)
			hm = myOwner.anlHash;
		if (_type == CEfaGatexClient.GX_EVETYP_CNT)
			hm = myOwner.cntHash;

		if(hm!=null)
		{
			hm.put(Integer.toString((int) ent.lGetKidx()), ent);
		}
	}
	
	/**
	 * Gets an entity from kidx hash table
	 * @param _type - type of the entity
	 * @param lKidx - kidx of the entity to get
	 * @return a entity reference or null if not found any with that kidx
	 */
	public CEfaEntity GetEntityFromHash(byte _type, long lKidx)
	{
		HashMap hm = null;
		if (_type == CEfaGatexClient.GX_EVETYP_DIG)
			hm = myOwner.digHash;
		if (_type == CEfaGatexClient.GX_EVETYP_ANL)
			hm = myOwner.anlHash;
		if (_type == CEfaGatexClient.GX_EVETYP_CNT)
			hm = myOwner.cntHash;

		if(hm!=null)
		{
			return (CEfaEntity)(hm.get(Integer.toString((int) lKidx)));
		}
		
		return null;
	}
	
	/**
	 * GenerateEvent - Generates a new event. It will save the the correct event queue
	 * @param eveType - event type
	 * @param eve - event reference
	 */
	public void GenerateEvent(byte eveType, CEfaEvent eve)
	{
		myOwner.newEvent(eveType, eve);
	}
	
	/**
	 * Main method of the Socket Manager. It will connect to server, makes server switches, indicates registrations.
	 */
	public void run(){
		while (bStop==false){
			// main thread responsible for checking comms and connecting to server
			if (bIAmConnected) waitTime = 0;
			if ((bIAmConnected==false) && (myOwner!=null))
			{
				// stop threads
				try {
					// is socket open...
					try {
						if (mySocketScateX!=null)
							if (mySocketScateX.isConnected()) {
								mySocketScateX.close();
								mySocketScateX = null;
							}
					} catch (Exception e) {
						
						
					}

					if (myOwner.myListener!=null && myOwner.myListener.isAlive())
					{
						if(myOwner.StopThisThread(myOwner.myListener,100)==false){
							myOwner.myListener.stop();	// should never happen...
						}
						myOwner.myListener.myDestruct();
						myOwner.myListener = null;
					}
					if (myOwner.wDogManager!=null && myOwner.wDogManager.isAlive())
					{
						if(myOwner.StopThisThread(myOwner.wDogManager,100)==false){
							myOwner.wDogManager.stop();
						}
						myOwner.wDogManager.myDestruct();
						myOwner.wDogManager = null;
					}
					
				} catch (Exception e) {
					
					
				}

				resetRetries();
				messageNumber = -1;
				bIsRegistering = false;
				MySocketBusy = null;
//				 tell queues to stop
				if(myOwner.StopThisThread(myOwner.qAnlPend,100)==false){
					myOwner.qAnlPend.stop();
				}
				if(myOwner.StopThisThread(myOwner.qDigPend,100)==false){
					myOwner.qDigPend.stop();
				}
				if(myOwner.StopThisThread(myOwner.qCntPend,100)==false){
					myOwner.qCntPend.stop();
				}
				if(myOwner.StopThisThread(myOwner.qCtrPend,100)==false){
					myOwner.qCtrPend.stop();
				}
								
				// was connect to any? if so, try the other...
				if (srvConnected=="" || srvConnected==myOwner.getsrvIpB())
					srvConnected = myOwner.getsrvIpA();
				else
					srvConnected = myOwner.getsrvIpB();
				
				myOwner.newLogMsg("Trying to connect to server: "+srvConnected);
					
				try {
					mySocketScateX = new Socket(srvConnected, 4810);
					input = new DataInputStream(mySocketScateX.getInputStream());
					output = new DataOutputStream(mySocketScateX.getOutputStream());
				
					// Conected! - RestartComms!
					myOwner.RestartComms();
					
					// thread that will listen to messages from scatex!!
					myOwner.myListener.ThreadStart();
					
					// restart hashing tables
					myOwner.digHash = new HashMap(myOwner.qDigRegistered.size());
					myOwner.anlHash = new HashMap(myOwner.qAnlRegistered.size());
					myOwner.cntHash = new HashMap(myOwner.qCntRegistered.size());
					
					// restart sent controlos list
					llSentControl = new LinkedList();

					// send handshake				
					if (handshaking()) {				
						resetRetries();
						// wdog thread:
						myOwner.wDogManager.ThreadStart();
						
						// copy the entities to register to queues
						myOwner.qDigPend.MyQueue = (LinkedList) myOwner.qDigRegistered.clone();
						myOwner.qAnlPend.MyQueue = (LinkedList) myOwner.qAnlRegistered.clone();
						myOwner.qCntPend.MyQueue = (LinkedList) myOwner.qCntRegistered.clone();
						
//						send reg init:
						if (reginit())
						{
							bIsRegistering = true;
							// tell queues to register entities
							myOwner.qDigPend.ThreadStart();
							myOwner.qAnlPend.ThreadStart();
							myOwner.qCntPend.ThreadStart();
							myOwner.qCtrPend.ThreadStart();							

							bIAmConnected = true;
							CEfaGatexClient.bIsConnected = true;
							myOwner.newLogMsg("Connected to Server - Starting Registration"); 
						}
						else
						{
							myOwner.newLogMsg("No permission to register - Switching Server");
							try {
								if (myOwner.wDogManager.isAlive())
								{
									if(myOwner.StopThisThread(myOwner.wDogManager,100)==false){
										myOwner.wDogManager.stop();
									}
									myOwner.wDogManager.myDestruct();
									myOwner.wDogManager = null;
								}
								//myOwner.myListener.stop();
								//mySocketScateX.shutdownInput();
								//mySocketScateX.shutdownOutput();
								if(mySocketScateX!=null && mySocketScateX.isConnected())
								{
									mySocketScateX.close();
									mySocketScateX = null;
								}
							} catch (Exception e) {
								myOwner.newErrMsg("Error stopping connections");
								
							}
						}
					}
					else {
						myOwner.newLogMsg("Handshaked failed");
						// continue to find a server
						// disconnect
						try {
							if(mySocketScateX!=null && mySocketScateX.isConnected())
							{
								mySocketScateX.close();
								mySocketScateX = null;
							}
							if (myOwner.myListener!=null && myOwner.myListener.isAlive())
							{
								if(myOwner.StopThisThread(myOwner.myListener,100)==false){
									myOwner.myListener.stop();
								}
								myOwner.myListener.myDestruct();
								myOwner.myListener = null;
							}
						} catch (Exception e) {
							
						}
						
//						 time to say that we cannot connect?
						if (waitTime>=myOwner.srvSwitchTimeout*1000)
						{
							if (CEfaGatexClient.bIsConnected)
							{
								myOwner.newLogMsg("CONNECTION FAILED");
								CEfaGatexClient.bIsConnected = false;
							}
							bRegDoEvent = true;
						}
					}
					
				} catch (Exception e) {
					// failed...
					if (myOwner.myListener!=null && myOwner.myListener.isAlive())
					{
						if(myOwner.StopThisThread(myOwner.myListener,100)==false){
								myOwner.myListener.stop();
						}
						myOwner.myListener.myDestruct();
						myOwner.myListener = null;
					}
					try {
						// try new connection in 2 seg
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						
					}
					waitTime+=2000;// slept 2seg...
					// time to say that we cannot connect?
					if (waitTime>=myOwner.srvSwitchTimeout*1000)
					{
						if (CEfaGatexClient.bIsConnected)
						{
							myOwner.newLogMsg("CONNECTION FAILED");
							CEfaGatexClient.bIsConnected = false;
						}
						bRegDoEvent = true;
					}
					
				}
			}
			
			// register endend?
			if(bIsRegistering == true)
			{
				// see if queues are empty
				if (myOwner.qDigPend.IsEmpty() && myOwner.qAnlPend.IsEmpty() && myOwner.qCntPend.IsEmpty())
				{
					// time to stable..
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						
					}
					
					// all registered
					if (regend())
					{
						// protection: check if queues are stopped
						if(myOwner.StopThisThread(myOwner.qAnlPend,100)==false){
							myOwner.qAnlPend.stop();
						}
						if(myOwner.StopThisThread(myOwner.qDigPend,100)==false){
							myOwner.qDigPend.stop();
						}
						if(myOwner.StopThisThread(myOwner.qCntPend,100)==false){
							myOwner.qCntPend.stop();
						}
						
						bIsRegistering = false;
						myOwner.newLogMsg("Registration Ended");
						bRegDoEvent = false;
					}
					else
					{
						myOwner.newLogMsg("RegEnd Failed - Switch Server");
						waitTime = 0;
						bIAmConnected = false;
						// failed!
						try {
							if(myOwner.myListener!=null && myOwner.myListener.isAlive())
							{
								if(myOwner.StopThisThread(myOwner.myListener,100)==false){
									myOwner.myListener.stop();
								}
								myOwner.myListener.myDestruct();
								myOwner.myListener=null;
							}
							if(myOwner.wDogManager!=null && myOwner.wDogManager.isAlive())
							{
								if(myOwner.StopThisThread(myOwner.wDogManager, 100)==false){
									myOwner.wDogManager.stop();
								}
								myOwner.wDogManager.myDestruct();
								myOwner.wDogManager=null;
							}
							
							if(mySocketScateX!=null && mySocketScateX.isConnected())
							{
								mySocketScateX.close();
								mySocketScateX = null;
							}
						} catch (Exception e) {
							
						}
					}
				}				
			}
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				
			}
			if(waitTime < myOwner.srvSwitchTimeout*1000)
				waitTime+=10;
		}
	}

	/**
	 * Disconnect all communication with ScateX. It will stop all threads and close sockets.
	 *
	 */
	public void disconnect() {
		try {
			try {
				//	stop queues
				if(myOwner.StopThisThread(myOwner.qAnlPend,100)==false){
					myOwner.qAnlPend.stop();
				}
				if(myOwner.StopThisThread(myOwner.qDigPend,100)==false){
					myOwner.qDigPend.stop();
				}
				if(myOwner.StopThisThread(myOwner.qCntPend,100)==false){
					myOwner.qCntPend.stop();
				}
				if(myOwner.StopThisThread(myOwner.qCtrPend,100)==false){
					myOwner.qCtrPend.stop();
				}
				
			} catch (Exception e) {
				
			}
			if(mySocketScateX!=null && mySocketScateX.isConnected())
			{
				mySocketScateX.close();
				mySocketScateX=null;
			}
			if(myOwner.wDogManager!=null && myOwner.wDogManager.isAlive())
			{
				if(myOwner.StopThisThread(myOwner.wDogManager, 100)==false){
					myOwner.wDogManager.stop();
				}
				myOwner.wDogManager.myDestruct();
				myOwner.wDogManager=null;
			}
			if(myOwner.myListener!=null && myOwner.myListener.isAlive())
			{
				if(myOwner.StopThisThread(myOwner.myListener,100)==false){
					myOwner.myListener.stop();
				}
				myOwner.myListener.myDestruct();
				myOwner.myListener=null;
			}
			
		} catch (Exception e) {
			//e.printStackTrace();
			
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
		try {
			if (mySocketScateX!=null)
			{
				if (mySocketScateX.isConnected()) {
					mySocketScateX.close();
					mySocketScateX = null;
				}
			}
			if(input!=null) input = null;
			if(output!=null) output = null;
		} catch (Exception e) {
			
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
