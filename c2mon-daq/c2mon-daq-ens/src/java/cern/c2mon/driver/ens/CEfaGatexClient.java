package cern.c2mon.driver.ens;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Main Class that will define the API between the systems
 * 
 * @author EFACEC
 * @version 2.0
 */
public class CEfaGatexClient extends Object {

    /** Error Msgs */
    private LinkedList qErrMsg = new LinkedList();
    /** Log Msgs */
    private LinkedList qLogMsg = new LinkedList();

    /**
     * Gets a Error Message
     * 
     * @return Error message or null if queue is empty
     */
    public String getErrMsg() {
        String str = null;
        try {
            str = (String) qErrMsg.removeFirst();
        } catch (Exception e) {
        }

        return (String) str;
    }

    /**
     * New Error Message
     * 
     * @param s
     *            - new message
     */
    public void newErrMsg(String s) {
        try {
            // String msg = new
            // String((System.currentTimeMillis()/1000)+"."+(System.currentTimeMillis()%1000)+"-"+s);
            qErrMsg.addLast(s);
            // System.out.println(msg);
        } catch (Exception e) {

        }
        s = null;
    }

    /**
     * Gets a Log Message
     * 
     * @return Log message or null if queue is empty
     */
    public String getLogMsg() {
        String str = null;

        try {
            str = (String) qLogMsg.removeFirst();
        } catch (Exception e) {
        }
        return str;
    }

    /**
     * New Log Message
     * 
     * @param s
     *            - new message
     */
    public void newLogMsg(String s) {
        try {
            // String msg = new
            // String((System.currentTimeMillis()/1000)+"."+(System.currentTimeMillis()%1000)+"-"+s);
            qLogMsg.addLast(s);
            // System.out.println(msg);
        } catch (Exception e) {
        }
        s = null;
    }

    /** Queue with analog variables waiting for register */
    CEfaSyncQueue qAnlPend = null;

    /** Queue with digital variables waiting for register */
    CEfaSyncQueue qDigPend = null;

    /** Queue with counters variables waiting for register */
    CEfaSyncQueue qCntPend = null;

    /** Controls queue to send to server */
    CEfaSyncQueue qCtrPend = null;

    /** Linked list with variables added to register */
    LinkedList qAnlRegistered, qDigRegistered, qCntRegistered, qCtrRegistered;
    /** Hashing tables to search through kidx */
    HashMap digHash, anlHash, cntHash;
    /** Hashing tables to search through Tag - Digitals */
    HashMap digTagHash = new HashMap(300);
    /** Hashing tables to search through Tag - Analogs */
    HashMap anlTagHash = new HashMap(300);
    /** Hashing tables to search through Tag - Counters */
    HashMap cntTagHash = new HashMap(300);

    /** List with Analog Events received from ScateX */
    // v2.0 private static LinkedList llAnlEve;
    private LinkedList llAnlEve;
    /** List with Digital Events received from ScateX */
    // v2.0 private static LinkedList llDigEve;
    private LinkedList llDigEve;
    /** List with Counter Events received from ScateX */
    // V2.0 private static LinkedList llCntEve;
    private LinkedList llCntEve;
    /** List with Control Events received from ScateX */
    // V2.0 private static LinkedList llCtrEve;
    private LinkedList llCtrEve;

    /**
     * Thread that will Listen communications with ScateX
     */
    CEfaListener myListener = null;

    /**
     * Manager of Communications with ScateX
     */
    CEfaSocketManager mySocket = null;

    /**
     * WDog thread that will take care of watcdog message with ScateX
     */
    CEfaWDogManager wDogManager = null;

    /** rx server timeout in seconds */
    int srvTimeout = 5;
    /** server wdog timeout in seconds */
    int srvWDogTim = 60;
    /** server maximum bytes in a message */
    int srvMaxBytes = 2000;
    /** timeout between switch of servers in seconds */
    int srvSwitchTimeout = 20;
    /** max retrys to give failure of current server */
    int maxRetrys = 3;

    /**
     * Gets Max Retries
     * 
     * @return Max Retries
     */
    public final int getMaxRetries() {
        return maxRetrys;
    }

    /** ScateX server A */
    private String srvIpA = null;

    /**
     * Get IP of Server A
     * 
     * @return server IP
     */
    public final String getsrvIpA() {
        return srvIpA;
    }

    /** ScateX server B */
    private String srvIpB = null;

    /**
     * Get IP of Server B
     * 
     * @return server IP
     */
    public final String getsrvIpB() {
        return srvIpB;
    }

    /** Say if is connected to online server */
    public static boolean bIsConnected = false;

    /** Flag to sync access to queues */
    // private boolean EventsBusy = false;

    /** Flag to indicate if is to use the register analog with jitter message */
    private boolean bUseJitterMsg = false;

    /**
     * Gets if is using jitter
     * 
     * @return true if is using jitter
     */
    public final boolean getUseJitterMsg() {
        return bUseJitterMsg;
    }

    /**
     * Sets if is to use jitter
     * 
     * @param _bUse
     *            - use or not jitter
     */
    public final void setUseJitterMsg(boolean _bUse) {
        bUseJitterMsg = _bUse;
    }

    /** event type: digital */
    public static final byte GX_EVETYP_DIG = 0;
    /** event type: measure */
    public static final byte GX_EVETYP_ANL = 1;
    /** event type: counter */
    public static final byte GX_EVETYP_CNT = 2;
    /** event type: control response */
    public static final byte GX_EVETYP_CTR_RESP = 3;

    /** Control Type: Control */
    public static final short GX_COMMAND = 4;
    /** Control Type: Setpoint */
    public static final short GX_SETPOINT = 8;

    /**
     * Restarts communications variables
     * 
     */
    public final void RestartComms() {
        if (qAnlPend != null) {
            qAnlPend.myDestruct();
            qAnlPend = null;
        }
        qAnlPend = new CEfaSyncQueue(this);
        qAnlPend.setName("qAnlPend");

        if (qDigPend != null) {
            qDigPend.myDestruct();
            qDigPend = null;
        }
        qDigPend = new CEfaSyncQueue(this);
        qDigPend.setName("qDigPend");

        if (qCntPend != null) {
            qCntPend.myDestruct();
            qCntPend = null;
        }
        qCntPend = new CEfaSyncQueue(this);
        qCntPend.setName("qCntPend");

        if (qCtrPend != null) {
            qCtrPend.myDestruct();
            qCtrPend = null;
        }
        qCtrPend = new CEfaSyncQueue(this);
        qCtrPend.setName("qCtrPend");

        if (myListener != null) {
            myListener.myDestruct();
            myListener = null;
        }
        myListener = new CEfaListener(this);
        myListener.setName("myListener");

        if (wDogManager != null) {
            wDogManager.myDestruct();
            wDogManager = null;
        }
        wDogManager = new CEfaWDogManager(this);
    }

    /**
     * Constructor
     * 
     */
    static int active = 0;

    public CEfaGatexClient() {
        active++;
        // System.out.println("new CEfaGatexClient = "+active);

        mySocket = new CEfaSocketManager(this);

        qAnlRegistered = new LinkedList();
        qDigRegistered = new LinkedList();
        qCntRegistered = new LinkedList();
        qCtrRegistered = new LinkedList();

        llAnlEve = new LinkedList();
        llDigEve = new LinkedList();
        llCntEve = new LinkedList();
        llCtrEve = new LinkedList();

        RestartComms();

    }

    /**
     * Sets Servers IP or names
     * 
     * @param srvA
     *            - Server A
     * @param srvB
     *            - Server B
     */
    public void propSetHostSrv(String srvA, String srvB) {
        srvIpA = new String(srvA);
        srvIpB = new String(srvB);
    }

    /**
     * Set Communication parameters
     * 
     * @param timout
     *            - server timeout
     * @param wdog
     *            - wdog cycle
     * @param maxbyte
     *            - maximum bytes in a message
     */
    public void propSetComms(int timout, int wdog, int maxbyte, int retrys) {
        srvWDogTim = wdog;
        srvMaxBytes = maxbyte;
        srvSwitchTimeout = timout;
        maxRetrys = retrys;
        // when gatex accepts more than 2000, remove this line
        if (srvMaxBytes > 2000) {
            srvMaxBytes = 2000;
        }
    }

    /**
     * Checks if server params are ok
     * 
     * @return true if params are valid
     */
    private boolean isValid() {
        if (srvIpA != null && srvIpB != null) {
            return true;
        }

        return false;
    }

    /**
     * It indicates GateX client that can start trying to connect to Server
     * 
     * @return true if params are valid and threads started ok
     */
    public boolean Connect() {

        if (isValid()) {
            newLogMsg("Starting to Connect.");
            mySocket.ThreadStart();
            return true; // accepted comand to start connection...
        }

        newLogMsg("It's not possible to connect. Invalid Parameters");
        bIsConnected = false;
        return false;
    }

    /**
     * Stops all communications with ScateX Server
     * 
     * @param secs
     */
    public void stopConn(int secs) {
        if (mySocket != null) {
            if (StopThisThread(mySocket, 100) == false) {
                mySocket.stop();
            }
            mySocket.disconnect();
            mySocket.myDestruct();
            mySocket = null;
        }
        bIsConnected = false;
        newLogMsg("Connection Endend");
    }

    /**
     * Is connected to a ScateX server ?
     * 
     * @return true if is connected to a Server
     * 
     *         Waits maximum "timSecs*1000" secs to know if is connected to a
     *         server
     */
    public boolean isConnected(int timSecs) {

        final long mili = System.currentTimeMillis() + ((long) timSecs) * 1000;

        do {
            if (bIsConnected) {
                return true;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        } while (mili > System.currentTimeMillis());

        return false;
    }

    /**
     * Puts a new event from ScateX in the correct Queue
     * 
     * @param eveType
     *            - type of the event
     * @param eve
     *            - event entity
     */
    public synchronized void newEvent(byte eveType, CEfaEvent eve) {
        String str = null;
        try {
            switch (eveType) {
            case CEfaGatexClient.GX_EVETYP_ANL:
                // str = "Analog Event:"+eve.getEntityRef().getSxId();
                llAnlEve.addLast(eve);
                break;
            case CEfaGatexClient.GX_EVETYP_CNT:
                // str = "Counter Event:"+eve.getEntityRef().getSxId();
                llCntEve.addLast(eve);
                break;
            case CEfaGatexClient.GX_EVETYP_DIG:
                // str = "Digital Event:"+eve.getEntityRef().getSxId();
                llDigEve.addLast(eve);
                break;
            case CEfaGatexClient.GX_EVETYP_CTR_RESP:
                llCtrEve.addLast(eve);
                str = "Control Resp Event:" + eve.getEntityRef().getSxId() + " Resp Code: " + ((CEfaEntityCtrResp) eve.getEntityRef()).getResponce();
                break;
            }

        } catch (Exception e) {

        }

        if (str != null) {
            newLogMsg(str);
        }
        str = null;
        eve = null;
    }

    /**
     * Returns Event from Queue
     * 
     * @param eveType
     *            - event type to get
     * @param testProc
     *            - not used - for simulation mode
     *            TODO This should be implemented or removed
     * @return event or null if no event
     */
    public synchronized CEfaEvent getEvent(byte eveType, int testProc) {
        CEfaEvent _temp = null;
        try {
            switch (eveType) {
            case CEfaGatexClient.GX_EVETYP_ANL:
                _temp = (CEfaEvent) llAnlEve.removeFirst();
                break;
            case CEfaGatexClient.GX_EVETYP_CNT:
                _temp = (CEfaEvent) llCntEve.removeFirst();
                break;
            case CEfaGatexClient.GX_EVETYP_DIG:
                _temp = (CEfaEvent) llDigEve.removeFirst();
                break;
            }
            return _temp;
        } catch (Exception e) {
        }

        _temp = null;
        return null;
    }

    /**
     * Get control response events
     * 
     * @param ord order of control to get response event
     * @return event or null if no event
     */
    public synchronized CEfaEvent getControlEvent(final int ord) {
        CEfaEvent temp = null;
        try {
            if (!llCtrEve.isEmpty()) {
                for (int i = 0; i < llCtrEve.size(); i++) {
                    if (((CEfaEntityCtrResp) (((CEfaEvent) llCtrEve.get(i)).getEntityRef())).getOrder() == ord) {
                        temp = (CEfaEvent) llCtrEve.remove(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
        return temp;
    }

    /**
     * Adds new entity to register
     * 
     * @param ent entity to register
     * @return true if entity was accepted
     */
    public boolean addNewEntity(final CEfaEntity ent) {
        boolean success = true;
        // only add entities if is not connected!
        if (!bIsConnected) {
            if (CEfaEntityDig.class.isInstance(ent)) {
                qDigRegistered.addLast(ent);
                digTagHash.put(ent.getSxId(), ent);
            } else if (CEfaEntityAnl.class.isInstance(ent)) {
                qAnlRegistered.addLast(ent);
                anlTagHash.put(ent.getSxId(), ent);
            } else if (CEfaEntityCnt.class.isInstance(ent)) {
                qCntRegistered.addLast(ent);
                cntTagHash.put(ent.getSxId(), ent);
            }
            else {
                // no matching type
                success = false;
            }
        }
        else {
            // cannot add while connected
            success = false;
        }
        return success;
    }

    /**
     * SendControl
     * 
     * @param ctr : Control TAG to execute
     */
    public void sendControl(int ord, String ctr) {
        CEfaEntityCtr ent_ctr = new CEfaEntityCtr();
        ent_ctr.setId(ctr, "");
        ent_ctr.setOrder(ord);
        qCtrPend.Add(ent_ctr);
    }

    /**
     * sendControl - with setpoint
     * 
     * @param ctr
     *            - control tag to execute
     * @param value
     *            - setpoint
     */
    public void sendControl(int ord, String ctr, float value) {
        CEfaEntityCtrSet ent_ctr = new CEfaEntityCtrSet();
        ent_ctr.setId(ctr, "");
        ent_ctr.setOrder(ord);
        ent_ctr.bSetValue(value);
        qCtrPend.Add(ent_ctr);
    }

    /**
     * Stops this Threads and wait for thread to end
     * 
     * @param _thread
     *            - thread to stop
     * @param milisecs
     *            - miliseconds to wait thread ending
     * @return true if thread ends normally
     */
    public boolean StopThisThread(CEfaThread _thread, int milisecs) {
        int iTimer = 0;
        if (_thread != null) {
            // ask to stop
            _thread.ThreadStop();
            while (iTimer <= milisecs) {
                if (_thread.isAlive() == false) {
                    return true; // ended ok!
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    return false;
                }
                iTimer += 1;
            }
            return false; // ups... timer finished and didn't end
        }

        return false;// not a valid thread
    }

    /**
     * myDestruct
     * 
     */
    public void myDestruct() {
        String s = null;
        CEfaEntity ent = null;
        CEfaEvent eve = null;

        // clean up memory...
        // remove from queues
        if (qErrMsg != null) {
            while (qErrMsg.isEmpty() == false) {
                s = (String) qErrMsg.removeFirst();
                s = null;
            }
        }
        // free queue
        qErrMsg = null;

        if (qLogMsg != null) {
            // String s;
            while (qLogMsg.isEmpty() == false) {
                s = (String) qLogMsg.removeFirst();
                s = null;
            }
        }
        // free queue
        qLogMsg = null;

        // synq queues
        if (qAnlPend != null) {
            if (qAnlPend.isAlive()) {
                if (StopThisThread(qAnlPend, 100) == false) {
                    qAnlPend.stop();
                }
            }
            qAnlPend.myDestruct();
            qAnlPend = null;
        }
        if (qDigPend != null) {
            if (qDigPend.isAlive()) {
                if (StopThisThread(qDigPend, 100) == false) {
                    qDigPend.stop();
                }
            }
            qDigPend.myDestruct();
            qDigPend = null;
        }
        if (qCntPend != null) {
            if (qCntPend.isAlive()) {
                if (StopThisThread(qCntPend, 100) == false) {
                    qCntPend.stop();
                }
            }
            qCntPend.myDestruct();
            qCntPend = null;
        }
        if (qCtrPend != null) {
            if (qCtrPend.isAlive()) {
                if (StopThisThread(qCtrPend, 100) == false) {
                    qCtrPend.stop();
                }
            }
            qCtrPend.myDestruct();
            qCtrPend = null;
        }
        // Linked lists
        if (qAnlRegistered != null) {
            while (qAnlRegistered.isEmpty() == false) {
                ent = (CEfaEntity) qAnlRegistered.removeFirst();
                if (ent != null) {
                    ent.myDestruct();
                }
                ent = null;
            }
        }
        if (qDigRegistered != null) {
            // CEfaEntity ent = null;
            while (qDigRegistered.isEmpty() == false) {
                ent = (CEfaEntity) qDigRegistered.removeFirst();
                if (ent != null) {
                    ent.myDestruct();
                }
                ent = null;
            }
        }
        if (qCntRegistered != null) {
            // CEfaEntity ent = null;
            while (qCntRegistered.isEmpty() == false) {
                ent = (CEfaEntity) qCntRegistered.removeFirst();
                if (ent != null) {
                    ent.myDestruct();
                }
                ent = null;
            }
        }
        if (qCtrRegistered != null) {
            // CEfaEntity ent = null;
            while (qCtrRegistered.isEmpty() == false) {
                ent = (CEfaEntity) qCtrRegistered.removeFirst();
                if (ent != null) {
                    ent.myDestruct();
                }
                ent = null;
            }
        }

        // Hashing tables
        if (digHash != null) {
            digHash.clear();
            digHash = null;
        }
        if (anlHash != null) {
            anlHash.clear();
            anlHash = null;
        }
        if (cntHash != null) {
            cntHash.clear();
            cntHash = null;
        }

        // Linked lists
        if (llAnlEve != null) {
            // CEfaEvent eve = null;
            while (llAnlEve.isEmpty() == false) {
                eve = (CEfaEvent) llAnlEve.removeFirst();
                if (eve != null) {
                    eve.myDestruct();
                }
                eve = null;
            }
            llAnlEve = null;
        }
        if (llDigEve != null) {
            // CEfaEvent eve = null;
            while (llDigEve.isEmpty() == false) {
                eve = (CEfaEvent) llDigEve.removeFirst();
                if (eve != null) {
                    eve.myDestruct();
                }
                eve = null;
            }
            llDigEve = null;
        }
        if (llCntEve != null) {
            // CEfaEvent eve = null;
            while (llCntEve.isEmpty() == false) {
                eve = (CEfaEvent) llCntEve.removeFirst();
                if (eve != null) {
                    eve.myDestruct();
                }
                eve = null;
            }
            llCntEve = null;
        }
        if (llCtrEve != null) {
            // CEfaEvent eve = null;
            while (llCtrEve.isEmpty() == false) {
                eve = (CEfaEvent) llCtrEve.removeFirst();
                if (eve != null) {
                    eve.myDestruct();
                }
                eve = null;
            }
            llCtrEve = null;
        }

        // threads
        if (wDogManager != null) {
            if (wDogManager.isAlive()) {
                if (StopThisThread(wDogManager, 100) == false) {
                    wDogManager.stop();
                }
            }
            wDogManager.myDestruct();
            wDogManager = null;
        }
        if (myListener != null) {
            if (myListener.isAlive()) {
                if (StopThisThread(myListener, 100) == false) {
                    myListener.stop();
                }
            }
            myListener.myDestruct();
            myListener = null;
        }
        if (mySocket != null) {
            if (mySocket.isAlive()) {
                mySocket.stop();
            }
            mySocket.myDestruct();
            mySocket = null;
        }

        // other vars
        if (srvIpA != null) {
            srvIpA = null;
        }
        if (srvIpB != null) {
            srvIpB = null;
        }

    }

    /**
     * finalize method
     */
    protected void finalize() {
        myDestruct();
        active--;
        try {
            super.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
