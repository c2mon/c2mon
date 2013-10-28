/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.driver.ens;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import cern.c2mon.driver.common.EquipmentMessageHandler;
import cern.c2mon.driver.common.ICommandRunner;
import cern.c2mon.driver.tools.TIMDriverSimpleTypeConverter;
import cern.c2mon.driver.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.driver.tools.equipmentexceptions.EqIOException;
import cern.tim.shared.common.datatag.address.ENSHardwareAddress;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import ch.cern.tim.shared.datatag.address.impl.ENSHardwareAddressImpl;

/**
 * This is a skeleton of the specialised EquipmentMessageHandler component for
 * ENS. It is a subclass of the abstract EquipmentMessageHandler class provided
 * by the TIM DAQ Core. This Class that makes connection so SCATEX 
 * (SCATEX is a SCADA(Supervisory Control and Data Acquisition) system),
 * througth a gatex (GAte To EXternal System) server connection. Developed by EFACEC SE.
 * 
 * @version 1.3.0
 * @author EFACEC SE - Filipe Campos
 */
public class ENSMessageHandler extends EquipmentMessageHandler implements Runnable, ICommandRunner {
    /**
     * This is the time the handler waits after he receives a restart
     * signal till he performs the restart.
     */
    private static final int RESTART_WAIT_TIME = 500;

//    // TODO Remove?
//    /** debug 2 console (after V1.1.0) */
//    private static final boolean debug2Cons = false;

    /** handler software version */
    private static final String SOFTWARE_VERSION = "== EFACEC ENS Handler-V1.3.0 beta1 (2005.07.08 10:00) ==";
//
//    /** cycle to make gc in s */
//    private static final int CYC_GC = 30;
//
//    // TODO Remove?
//    /** simulation mode */
//    private static final boolean bSimulMode = false;
//    
//    private int testProc = 0, testSecs = 0;
//
//    /** garbage collector timeout */
//    private long lGcCounter = 0;

    /** last is failed flag */
    private boolean lastIsFailed = false;

    /** gatex client class */
    private CEfaGatexClient gxClient = null;

//    /**
//     * The converter object, used for convertions from smartsockets data types
//     * to TIP data types
//     */
//    private TIMDriverSimpleTypeConverter timConverter = new TIMDriverSimpleTypeConverter();

    /** maximun number of gatex (GAte To EXternal System) properties, it cames in address */
    private static final byte NUM_PROPS = 7;

    /** vector for properties */
    private String[] strProp = null;

    /** name of properties */
    private String[] strPropName = { "srv1", "srv2", "comm_tim_s", "wdog_tim_s", "max_bytes", "max_retries", "gmt_correct", };

    /**
     * The monitor used for blocking the thread in case of the finish flag
     * change
     */
    private Boolean monFinish = Boolean.FALSE;

    /** flag to request finish the process */
    private boolean flagFinish = false;

    /** flag to ask restart of gatex (GAte To EXternal System)*/
    private boolean flagRestart = false;

    /** hashtable with objects in gatex (GAte To EXternal System)*/
    private HashMap<String, CEfaDbObj>[] objsInGatex = null;

    /** maximum number entitys types in scatex, id can be repeated in df entitys */
    private static final byte NUM_SXENT = 4;
    /**
     * Position of the digital entities in the gatex (GAte To EXternal System) array.
     */
    private static final byte NUM_SXDIG = 0;
    /**
     * Position of the analog entities in the gatex (GAte To EXternal System) array.
     */
    private static final byte NUM_SXANL = 1;
    /**
     * Position of the counter entities in the gatex (GAte To EXternal System) array.
     */
    private static final byte NUM_SXCNT = 2;
    /**
     * Position of the control entities in the gatex (GAte To EXternal System) array.
     */
    private static final byte NUM_SXCTR = 3;

    /** description of entitys */
    private String[] entDesc = { "Dig", "Anl", "Cnt", "Ctr" };

    /** stats from rx value events on digs */
    private long numEveDigVal = 0;

    /** stats from rx state events on digs */
    private long numEveDigSt = 0;

    /** stats from rx value events on anls */
    private long numEveAnlVal = 0;

    /** stats from rx state events on anls */
    private long numEveAnlSt = 0;

    /** stats from rx value events on cnt */
    private long numEveCntVal = 0;

    /** stats from rx state events on cnt */
    private long numEveCntSt = 0;

    /** maximum number of consecutive dig events to process */
    private static final int ENS_MAX_EVE_DIG = 1000;

    /** maximum number of consecutive anl events to process */
    private static final int ENS_MAX_EVE_ANL = 1000;

    /** maximum number of consecutive cnt events to process */
    private static final int ENS_MAX_EVE_CNT = 1000;

    /** vector with number of entities */
    private long[] numEnt = new long[NUM_SXENT];

    /** min tag size in ens */
    private static final byte SX_TAG_MIN_SZ = 1;

    /** max tag size in ens */
    private static final byte SX_TAG_MAX_SZ = 16;

//    // TODO Remove? Useless
//    /** minimum size of heap to work */
//    private static final long minHeapSz2Work = 0; // can be 1000000
//
//    /** minimum size of heap to garbage collector */
//    private static final long minHeapSz2Gc = minHeapSz2Work * 2;

    /** control sending uniq order */
    private int ctrSendOrd = 0;

    /** The monitor for ctr order */
    private Boolean monCtrOdr = Boolean.FALSE;

    /** gmt correction to hour */
    private long gmtHourCorrection = 0;

    /**
     * The default constructor
     */
    @SuppressWarnings("unchecked")
    public ENSMessageHandler() {
        objsInGatex = new HashMap[NUM_SXENT];
        for (int j = 0; j < NUM_SXENT; j++) {
            objsInGatex[j] = new HashMap<String, CEfaDbObj>();
        }
    }

    /**
     * Sleep which will just return if it gets interrupted.
     * 
     * @param msecs milliseconds to sleep
     */
    public void mySleep(final int msecs) {
        try {
            Thread.sleep(msecs);
        } catch (InterruptedException e) {
            getEquipmentLogger().error("ENS sleep phase was interrupted.", e);
        }
    }

    /**
     * log for this class
     * 
     * @param str string to log
     */
    public void myClassLog(final String str) {
        getEquipmentLogger().debug(str);
    }

    /**
     * log for this class
     * 
     * @param str string to log
     */
    public void myClassErr(final String str) {
        getEquipmentLogger().error(str);
    }

    /**
     * set the comm failure bit
     * 
     * @param isFailed true if comm has failure
     */
    public void setCommFailure(final boolean isFailed) {
        // TODO Is there any special reason to try and catch an exception?
        try {
            if (lastIsFailed != isFailed) {
                lastIsFailed = isFailed;
                myClassLog("(Setcommfailure=" + isFailed + ")");
                if (isFailed) {
                    getEquipmentMessageSender().confirmEquipmentStateIncorrect();
                } else {
                    getEquipmentMessageSender().confirmEquipmentStateOK();
                }
                myClassLog("(Setcommfailure done)");
                if (isFailed) {
                    invalidateRtu();
                }
            }
        } catch (Exception ex) {
            myClassErr("(setCommFailure) exception -> " + ex + " - " + ex.getMessage());

        }
    }

    /**
     * This method is used for parsing and obtain all relevant information from
     * the ENS address string It shall be like this
     * srv1=ens1.cern.ch;srv2=ens2.cern
     * .ch;comm_tim_s=30;wdog_tim_s=60;max_bytes=4096;max_retrys=3
     * 
     * @return Returns true if the parsing was sucessfull else it will return false.
     * @author Filipe Campos - EFACEC SE
     */
    private boolean parseEquipmentAddress() {
        boolean success = true;
        // string properties
        strProp = new String[NUM_PROPS];

        // get the relevent information from the address string
        StringTokenizer tokens = new StringTokenizer(super.getEquipmentConfiguration().getAddress(), ";");
        String token = "";
        String token2 = "";
        while (tokens.hasMoreTokens()) {
            token = tokens.nextToken();
            StringTokenizer tokens2 = new StringTokenizer(token, "=");
            while (tokens2.hasMoreTokens()) {
                token2 = tokens2.nextToken();
                for (int i = 0; i < NUM_PROPS; i++) {
                    if (token2.equalsIgnoreCase(strPropName[i])) {
                        strProp[i] = tokens2.nextToken();
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < NUM_PROPS; i++) {
            if (strProp[i] == null) {
                myClassErr("[prop " + i + "] ->" + strProp[i]);
                success = false;
                break; // TODO Perhaps it should run anyway through all of them to provide a complete info in the log
            }
            myClassLog("[prop " + i + "] ->" + strProp[i]);
        }

        return success;
    }

    // --------------------------------------------------------------------------------------
    // BUSINESS METHODS INHERITED FROM THE CORE
    // --------------------------------------------------------------------------------------
    /**
     * This method is called by the DAQ core on start-up, after the
     * EquipmentMessageHandler has been fully configured.
     * 
     * This method must prepare a connection to the data source, create
     * subscriptions for each monitored SourceDataTag, prepare SourceCommandTags
     * for execution etc. When this method returns without throwing an
     * EqIOException, the core expects the EquipmentMessageHandler to be ready
     * to acquire data and to send commands.
     * 
     * @throws EqIOException Thrown if connection to equipment failed.
     */
    public void connectToDataSource() throws EqIOException {
        myClassLog("** connectToDataSource ** ");

        if (!parseEquipmentAddress()) {
            throw new EqIOException("Error parsing ENS address !");
        }

        // start new the gatex manager thread.
        // The rest of the configuration steps will be by thread's run() method,
        // so it will
        // not block configuration of other devices

        // new Thread(this).start();
        Thread th = new Thread(this);
        th.setName("ENS loop");
        th.start();
        getEquipmentCommandHandler().setCommandRunner(this);
        getEquipmentConfigurationHandler().setDataTagChanger(
                new ENSDataTagChanger(this));
    }

    /**
     * This method is called by the DAQ core before the DAQ process exits.
     * 
     * This method must close all open subscriptions and release all resources
     * acquired by the EquipmentMessageHandler. When this method returns without
     * throwing an EqIOException, the DAQ core assumes that the
     * EquipmentMessageHandler is ready for shutdown or for a new call to
     * connectToDataSource()
     * 
     * @throws EqIOException Thrown if the communication to disconnect failed.
     */
    public void disconnectFromDataSource() throws EqIOException {
        myClassLog("** disconnectFromDataSource ** ");
        synchronized (monFinish) {
            flagFinish = true;
        }
        long lTimeout = 10;
        while (lTimeout > 0) {
            synchronized (monFinish) {
                if (gxClient == null) {
                    myClassLog(" disconnectFromDataSource -> stop gxclient done ... ");
                    return;
                }
            }
            lTimeout--;
            this.mySleep(1000);
        }
        throw new EqIOException("disconnectFromDataSource gives 10s timeout");
    }
    
    @Override
    public void refreshAllDataTags() {
        // TODO In general this might be just a restart.
    }
    
    @Override
    public void refreshDataTag(final long dataTagId) {
        // TODO Implement this method.
    }

    /**
     * This method is called to execute a command on the EquipmentMessageHandler.
     * 
     * The EMH is responsible for executing the command. If the method returns
     * without throwing an Exception, the core assumes that the command has been
     * executed successfully.
     * 
     * TO BE DEFINED: Each command has a "source timeout". If the method does
     * not return within the number of milliseconds specified in the
     * SourceCommandTag object, execution will be interrupted by the DAQ core
     * and the core will assume that command execution has failed.
     * 
     * @param pcmdTagValue the SourceDataTagValue object
     * @throws EqCommandTagException Throws an exception if the command failed.
     */
    protected void sendCommand(final SourceCommandTagValue pcmdTagValue) 
            throws EqCommandTagException {

        myClassLog("[Rx cmd request] cmdid = " + pcmdTagValue.getId());
        ISourceCommandTag sct = getEquipmentConfiguration().getSourceCommandTags().get(pcmdTagValue.getId());

        ENSHardwareAddress ensAddress = null;
        try {
            ensAddress = (ENSHardwareAddress) sct.getHardwareAddress();
        } catch (ClassCastException ex) {
            myClassErr("Invalid source command definition. Could not cast the address to class TDSHardwareAddress");
            throw new EqCommandTagException("Invalid source command definition. Could not cast the address to class TDSHardwareAddress");
        }

        int metOrd = getCtrOrd();
        String ensTag = ensAddress.getAddress();
        String ensType = ensAddress.getDataType();
        String ensValue = pcmdTagValue.getValue().toString();
        myClassLog("ord=" + metOrd + "[sendCommand] -> (enstag=" + ensTag + ") (enstype=" + ensType + ") (ensvalue=" + ensValue + ")");

        if (ensTag == null) {
            throw new EqCommandTagException(-1, "(sendCommand) null ens address");
        }
        if (ensTag.length() < SX_TAG_MIN_SZ || ensTag.length() > SX_TAG_MAX_SZ) {
            throw new EqCommandTagException(-1, "(sendCommand) invalid tag len");
        }

        if (ensType.equals(ENSHardwareAddress.TYPE_CTRL_SIMPLE)) {
            ensValue = null;
            myClassLog("sendCommand -> ens tag =" + ensTag + " is a simple comand");
        } else if (ensType.equals(ENSHardwareAddress.TYPE_CTRL_SETPOINT)) {
            myClassLog("sendCommand -> ens tag =" + ensTag + " is a setpoint comand with value " + ensValue);
        } else {
            throw new EqCommandTagException(-1, "(sendCommand) Command type not known...");
        }

        // send command
        int rcCode = byGatexSendCommand(ensTag, ensValue, sct.getSourceTimeout() - 100, metOrd);

        if (rcCode == CEfaEntityCtrResp.GX_EVECTR_FAIL) {
            myClassErr("sendCommand -> Fail to execute control=" + ensTag);
            throw new EqCommandTagException(rcCode, "Fail to execute control=" + ensTag);
        } else if (rcCode == CEfaEntityCtrResp.GX_EVECTR_INIB) {
            myClassErr("sendCommand -> Control is inibited=" + ensTag);
            throw new EqCommandTagException(rcCode, "Control is inibited=" + ensTag);
        } else if (rcCode == CEfaEntityCtrResp.GX_EVECTR_INVALID) {
            myClassErr("sendCommand -> Control is invalid=" + ensTag);
            throw new EqCommandTagException(rcCode, "Control is invalid=" + ensTag);
        } else if (rcCode == CEfaEntityCtrResp.GX_EVECTR_OK) {
            myClassLog("sendCommand -> Control well executed=" + ensTag);
        } else {
            throw new EqCommandTagException(-1, "(sendCommand) command timeout for " + ensTag);
        }

    }

    /**
     * the thread of the class method
     */
    public void run() {
        myClassLog("========== STARTING ENS HANDLER LOOP ===========");
        flagRestart = false;
        boolean goodConfig = true;
        try {
            numEnt[NUM_SXDIG] = 0;
            numEnt[NUM_SXANL] = 0;
            numEnt[NUM_SXCNT] = 0;
            numEnt[NUM_SXCTR] = 0;

            this.configGatex();

            myClassLog("Num Dig =" + numEnt[NUM_SXDIG]);
            myClassLog("Num Anl =" + numEnt[NUM_SXANL]);
            myClassLog("Num Cnt =" + numEnt[NUM_SXCNT]);
            myClassLog("Num Ctr =" + numEnt[NUM_SXCTR]);
        } catch (Exception ex) {
            setCommFailure(true);
            myClassErr("(configGatex) exception -> " + ex + " - " + ex.getMessage());
            goodConfig = false;

        }

        if (goodConfig) {
            myClassLog("ENS DAQ LOOP WILL START NOW...");
        } else {
            myClassLog("ENS DAQ LOOP WILL NOT START SINCE HAS ERRORS...");
        }

        myClassLog(SOFTWARE_VERSION);
        while (true) {

            // stop ??
            synchronized (monFinish) {
                if (flagFinish) {
                    setCommFailure(true);
                    destroyGatex();
                    myClassLog("** going out ** ");
                    return;
                }
                if (flagRestart) {
                    myClassLog("** going to restart ** ");
                    break;
                }
            }

            // others processing
            if (goodConfig) {
                try {
                    tickGatex();
                } catch (Exception ex) {
                    myClassErr("(tickGatex) exception -> " + ex + " - " + ex.getMessage());
                }
            }
        }
        myClassLog("** tick loop ends **");
        synchronized (monFinish) {
            destroyGatex();
        }
        mySleep(RESTART_WAIT_TIME);
        new Thread(this).start();
    }

    /**
     * here config can add to internal db a entity
     * 
     * @param sdt sourcedatatag give by tim
     * @param hardAddr hardware address that have ens atributes
     * @return True if the data tag is added to the database of registered
     * objects.
     */
    private boolean configAddToDb(
            final ISourceDataTag sdt, final ENSHardwareAddressImpl hardAddr) {
        try {
            String tagName = hardAddr.getAddress();
            if (tagName == null) {
                return false;
            }
            myClassLog("\t-> ens tag =" + tagName);
            if (tagName.length() < SX_TAG_MIN_SZ || tagName.length() > SX_TAG_MAX_SZ) {
                return false;
            }

            byte typEnt = getTypeWithHardAddr(hardAddr);
            if (typEnt >= NUM_SXENT) {
                myClassErr("invalid type in  " + tagName);
                return false;
            }

            numEnt[typEnt]++;

            CEfaDbObj dbObj = (CEfaDbObj) objsInGatex[typEnt].get(tagName);
            if (dbObj == null) {
                dbObj = new CEfaDbObj();
            }

            // set the tim object
            dbObj.setTimObj(sdt);
            if (!dbObj.createEnsObj(hardAddr, sdt)) {
                myClassErr("fail to create  " + tagName);
                objsInGatex[typEnt].remove(tagName);
                return false;
            }
            objsInGatex[typEnt].put(tagName, dbObj);
            myClassLog("\t\tNew ent[" + entDesc[typEnt] + "] - " + tagName);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * this method puts the object database invalid after a comm failure to
     * ensure that events will be sent after a restart of comm
     * TODO Remove?
     */
    private void invalidateRtu() {

    }

    /**
     * get type of entity with hardware address
     * 
     * @param hardAddr hardware address that have ens atributes
     * @return The type constant of this point.
     * @author Filipe Campos - EFACEC SE
     */
    private byte getTypeWithHardAddr(final ENSHardwareAddressImpl hardAddr) {
        String entType = hardAddr.getDataType();
        if (entType.equals(ENSHardwareAddressImpl.TYPE_DIGITAL)) {
            return NUM_SXDIG;
        } else if (entType.equals(ENSHardwareAddressImpl.TYPE_ANALOG)) {
            return NUM_SXANL;
        } else if (entType.equals(ENSHardwareAddressImpl.TYPE_COUNTER)) {
            return NUM_SXCNT;
        } else if (entType.equals(ENSHardwareAddressImpl.TYPE_CTRL_SIMPLE)) {
            return NUM_SXCTR;
        } else if (entType.equals(ENSHardwareAddressImpl.TYPE_CTRL_SETPOINT)) {
            return NUM_SXCTR;
        }
        return NUM_SXENT; // TODO this an ugly reuse of this constant - should be done different
    }

    /**
     * this method has to config gatex memory db to connect to scatex
     * 
     */
    @SuppressWarnings("unchecked")
    public void configGatex() {
        myClassLog("Start to config ENS-Handler");
        // new hash map type -> type - strange? Why no List?
        HashMap<String, String>[] usedTags = new HashMap[NUM_SXENT];
        boolean goodConf = true;
        // keep last used tags
        for (int j = 0; j < NUM_SXENT; j++) {
            usedTags[j] = new HashMap<String, String>();
            Iterator<String> cursorGatex = objsInGatex[j].keySet().iterator();
            while (cursorGatex.hasNext()) {
                String str = cursorGatex.next();
                usedTags[j].put(str, str);
            }
        }
        // At this point used tags is more or less a copy of objsInGatex without the values. I still don't get why this is no List

        // go through all supervised SourceDataTags and create a map objects
        // Enumeration e = sourceDataTags.elements();
        for (ISourceDataTag sdt : getEquipmentConfiguration().getSourceDataTags().values()) {
            myClassLog("\t-> tim tag =" + sdt.getName());
            boolean goodXmlEnt = true;

            // take it's Hardware Address
            // DataTagAddress tagAddress = sdt.getAddress();
            // if (tagAddress == null) {
            // myClassErr("DataTagAddress is null for " + sdt.getName());
            // goodXmlEnt = false;
            // } else {
            ENSHardwareAddressImpl hardAddr = (ENSHardwareAddressImpl) sdt.getHardwareAddress();
            if (hardAddr == null) {
                myClassErr("getHardwareAddress return null");
                goodXmlEnt = false;
            } else {
                if (!configAddToDb(sdt, hardAddr)) {
                    goodXmlEnt = false;
                    // goodConf = false;
                } else {
                    byte entType = getTypeWithHardAddr(hardAddr);
                    // This test is redundant it is already checked in configAddToDb
                    if (entType < NUM_SXENT) {
                        /*
                         *  If a tag was successfully added to the db it is 
                         *  removed from the used tag list. At this point 
                         *  the name of the local field gets confusing. It is actually
                         *  not the map of used tags it gets then more and more a map
                         *  of unused tags.
                         */
                        usedTags[entType].remove(hardAddr.getAddress());
                    } else {
                        myClassErr("invalid type in  " + hardAddr.getAddress());
                        goodXmlEnt = false;
                    }
                }
            }
            /*
             * Here the array of used tags has evolved to the map of 
             * unused tags.
             */

            // not good xml file
            if (!goodXmlEnt) {
                // invalidate in tim
                getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, "bad xml config");
                myClassErr("invalid entity in xml config " + sdt.getName());
            }

        }

        // remove unused tags
        // keep last used tags
        for (int j = 0; j < NUM_SXENT; j++) {
            Iterator<String> cursorUsed = usedTags[j].keySet().iterator();
            while (cursorUsed.hasNext()) {
                String str = (String) cursorUsed.next();
                // TODO This is most likely wrong - only digital points are removed
                objsInGatex[NUM_SXDIG].remove(str);
                myClassLog("Remove obj from db after re-start -> " + str);
            }
        }

        // is bad conf return
        if (!goodConf) {
            return;
        }

        synchronized (monFinish) {
            // create gatex
            gxClient = new CEfaGatexClient();
            gxClient.setUseJitterMsg(true);
            gxClient.propSetComms(Integer.parseInt(strProp[2]), Integer.parseInt(strProp[3]), Integer.parseInt(strProp[4]), Integer.parseInt(strProp[5]));
            gxClient.propSetHostSrv(strProp[0], strProp[1]);
//            gxClient.setSimulMode(false);

            gmtHourCorrection = Integer.parseInt(strProp[6]);
            myClassLog("GMT correction=" + gmtHourCorrection);

            // register vars
            for (int j = 0; j < NUM_SXENT; j++) {

                Collection<CEfaDbObj> objCol = objsInGatex[j].values();
                Iterator<CEfaDbObj> cursorObjs = objCol.iterator();

                while (cursorObjs.hasNext()) {
                    CEfaDbObj dbObj = cursorObjs.next();

                    gxClient.addNewEntity(dbObj.getEnsObj());

                    dbObj = null; // useless
                }

                objCol = null; // useless
                cursorObjs = null; // useless
            }

            // connect
            gxClient.Connect();
        }
        myClassLog("ENS-Handler Config has ended...");
        myClassLog("=================================");
    }

    /**
     * this method has to destroy the db memory of gatex
     */
    public void destroyGatex() {
        try {
            myClassLog("\tDestroy gatex");
            if (gxClient != null) {
                gxClient.stopConn(3);
            }
        } catch (Exception ex) {
            myClassErr("(destroyGatex) exception -> " + ex + " - " + ex.getMessage());

        }

        if (gxClient != null) {
            gxClient.myDestruct();
        }
        gxClient = null;
    }

    /**
     * send a commando to gatex, has to check timeout
     * 
     * @param ensTag ens tag to send
     * @param ensValue ens value if it is a setpoint
     * @param rcCode result code
     * @param timeoutMs timeout in mili-seconds
     * @return false it fails to send
     * @author Filipe Campos - EFACEC SE
     */
    private int byGatexSendCommand(final String ensTag, final String ensValue,
            int timeoutMs, final int order) {
        // TODO Is there a reason that the next comment in fact lies?
        // at least 1 ms
        if (timeoutMs < 0) {
            timeoutMs = 0;
        }
        long mili = System.currentTimeMillis() + timeoutMs;

        // send command
        do {
            synchronized (monFinish) {

                if (gxClient != null) {

                    // if queue has events clean, it can not have
                    gxClient.getControlEvent(order);

                    // send control
                    if (ensValue == null) {
                        gxClient.sendControl(order, ensTag);
                    } else {
                        gxClient.sendControl(order, ensTag, Float.parseFloat(ensValue));
                    }
                    break;
                }
            }
            mySleep(1);
        } while (mili > System.currentTimeMillis());

        // wait responce
        do {
            synchronized (monFinish) {
                if (gxClient == null) {
                    return CEfaEntityCtrResp.GX_EVECTR_FAIL;
                }

                // get control event responce
                CEfaEvent ctrEve = gxClient.getControlEvent(order);
                if (ctrEve != null) {
                    // get entity of event
                    CEfaEntityCtrResp ctrResp = (CEfaEntityCtrResp) ctrEve.getEntityRef();
                    if (ctrResp == null) {
                        return CEfaEntityCtrResp.GX_EVECTR_FAIL;
                    }
                    // if tag is of sent tag
                    if (ctrResp.getSxId().equals(ensTag)) {
                        int resp = ctrResp.getResponce();
                        myClassLog("  <- RESPONCE CTR ORDER " + order + " = " + resp);
                        return resp;
                    } else {
                        myClassErr("  <- Invalid control tag in ctr responce=" + ctrResp.getSxId() + ". I was waiting for " + ensTag);
                    }
                }
            }
            mySleep(1);
        } while (mili > System.currentTimeMillis());
        return CEfaEntityCtrResp.GX_EVECTR_TIMEOUT;
    }

    /**
     * convert ens status event to status of tim and send it
     * 
     * @param ensEnt
     * @param sdt
     * @param isServerFailed
     */
    public void convertEns2TimStatus(CEfaEntity ensEnt, ISourceDataTag sdt, long utcTag) {
        Timestamp timestamp = new Timestamp(utcTag);

        if (ensEnt.getInvCode() == CEfaEntity.EFAENT_VALID) {
            return;
        }

        myClassLog("   <<Update Tim Status>>  ens id=" + ensEnt.getSxId() + " tim id=" + sdt.getId() + " with valid =" + ensEnt.getInvCode() + " at " + utcTag);

        if (ensEnt.getInvCode() == CEfaEntity.EFAENT_MOFFSCAN) {
            getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.DATA_UNAVAILABLE, "ENS value unavailable (manual off scan)", timestamp);
        } else if (ensEnt.getInvCode() == CEfaEntity.EFAENT_AOFFSCAN) {
            getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.DATA_UNAVAILABLE, "ENS value unavailable (automatic off scan)", timestamp);
        } else if (ensEnt.getInvCode() == CEfaEntity.EFAENT_INVTAG) {
//            if (debug2Cons) {
//                System.out.println("Invalid ENS tag <" + ensEnt.getSxId() + ">");
//            }
            getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, "ENS entity " + ensEnt.getSxId() + " does not exist .", timestamp);
        } else if (ensEnt.getInvCode() == CEfaEntity.EFAENT_OVERFLOW) {
            getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.DATA_UNAVAILABLE, "ENS value unavailable (overflow)", timestamp);
        } else if (ensEnt.getInvCode() == CEfaEntity.EFAENT_UNINITIALISED) {
            getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.DATA_UNAVAILABLE, "ENS entity not initialised", timestamp);
        } else {
            getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.DATA_UNAVAILABLE, "ENS value unavailable (imposed?)", timestamp);
        }
        myClassLog("   <<Update Tim Status>> done");
    }

    /**
     * this method is the gatex tick
     * 
     */
    public void tickGatex() {
        mySleep(1);
        boolean isServerFailed = false;
        // make comm failure
        synchronized (monFinish) {
            if (!gxClient.isConnected(5)) {
                isServerFailed = true;
            }
        }
        setCommFailure(isServerFailed);

        // some data
        Object objToSend = null;
        ISourceDataTag sdt = null;
        ENSHardwareAddress dataTagAddress = null;
        int iMaxEvents = 0;
        CEfaEvent efaEve = null;
        //
        // digital events
        //
        // TODO This would be a wonderful place for a method and a for loop
        iMaxEvents = ENS_MAX_EVE_DIG;
        do {
            synchronized (monFinish) {
                efaEve = this.gxClient.getEvent(CEfaGatexClient.GX_EVETYP_DIG, 0);
            }
            if (efaEve == null) {
                break;
            }

            // get dig in event
            CEfaEntityDig entDig = (CEfaEntityDig) efaEve.getEntityRef();
            if (entDig == null) {
                myClassErr("LOOSE EVENT: event has not digital");
                efaEve = null;
                continue;
            }

            // get object in hash
            CEfaDbObj dbObj = (CEfaDbObj) objsInGatex[NUM_SXDIG].get(entDig.getSxId());
            if (dbObj == null) {
                myClassErr("LOOSE EVENT: not in hash the dig " + entDig.getSxId());
                efaEve = null;
                continue;
            }

            // put this only if gatex gives repetead events
            boolean bEventValue = true;
            boolean bEventInv = true;

            // get sdt
            sdt = dbObj.getTimObj();
            if (sdt == null) {
                myClassErr("LOOSE EVENT: no sdt in the dig " + entDig.getSxId());
                continue;
            }

            // calculate ttag
            long lTTag = System.currentTimeMillis();
            if (entDig.getTTag().isTTagValid()) {
                lTTag = entDig.getTTag().getTotalMS();
                lTTag = convUtc2Local(lTTag);
            }

            Timestamp timestamp = new Timestamp(lTTag);
            // get data address
            dataTagAddress = (ENSHardwareAddress) sdt.getHardwareAddress();
            if (dataTagAddress == null) {
                getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, "Hard Addr is null.", timestamp);
                myClassErr("LOOSE EVENT: no ENSHardwareAddress in the dig " + entDig.getSxId());
                efaEve = null;
                continue;
            }

            // has to send cos
            if ((bEventInv && entDig.getInvCode() != CEfaEntity.EFAENT_VALID && entDig.getInvCode() != CEfaEntity.EFAENT_IMPOSED) && bEventValue) {
                bEventValue = false;
            }
            if (bEventInv && (entDig.getInvCode() == CEfaEntity.EFAENT_VALID || entDig.getInvCode() == CEfaEntity.EFAENT_IMPOSED)) {
                bEventValue = true;
                bEventInv = false;
            }

            if (bEventValue) {
                numEveDigVal++;
//                if (debug2Cons) {
//                    System.out.println("<dig val event> n=" + numEveDigVal);
//                }
                objToSend = TIMDriverSimpleTypeConverter.convert(sdt, (int) entDig.fGetValue());
                if (objToSend != null) {
                    // lTTag = convUtc2Local (lTTag);
                    myClassLog("   <<Update Tim Dig Value>>: ens id=" + entDig.getSxId() + " tim id=" + entDig.getOemId() + " with value =" + (int) entDig.fGetValue() + " ttag=" + lTTag);
                    if (entDig.getInvCode() == CEfaEntity.EFAENT_IMPOSED) {
                        getEquipmentMessageSender().sendTagFiltered(sdt, objToSend, lTTag, "Imposed ENS value");
                    } else {
                        getEquipmentMessageSender().sendTagFiltered(sdt, objToSend, lTTag);
                    }
                } else {
                    myClassErr("\t <<Update Tim Dig Status>> sending INVALIDATE SourceDataTagValue with quality CONVERSION_ERROR, for Tag name : " + sdt.getName() + " tag id : " + sdt.getId());
                    getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.CONVERSION_ERROR, "Unable to convert value " + (int) entDig.fGetValue() + " to data type " + sdt.getDataType(),
                            timestamp);
                    bEventInv = false;
                }
            }

            // has to send by cost
            if (bEventInv) {
                numEveDigSt++;
//                if (debug2Cons) {
//                    System.out.println("<dig sta event> n=" + numEveDigSt);
//                }
                convertEns2TimStatus(entDig, sdt, lTTag);
            }

            efaEve = null;
            objToSend = null;
            entDig = null;
            dbObj = null;
            sdt = null;
            dataTagAddress = null;

        } while (--iMaxEvents > 0);

        //
        // analog events
        //
        // TODO This would be a wonderful place for a method and a for loop
        iMaxEvents = ENS_MAX_EVE_ANL;
        do {

//            testProc++;
            synchronized (monFinish) {
                efaEve = this.gxClient.getEvent(CEfaGatexClient.GX_EVETYP_ANL, 0);
            }
            if (efaEve == null) {
                break;
            }

            // get anl in event
            CEfaEntityAnl entAnl = (CEfaEntityAnl) efaEve.getEntityRef();
            if (entAnl == null) {
                myClassErr("LOOSE EVENT: event has not analogue");
                efaEve = null;
                continue;
            }

            // myClassLog("ENS anl event -> "+entAnl.getSxId()+" value="+entAnl.fGetValue()+" valid="+entAnl.getInvCode()
            // );

            // get object in hash
            CEfaDbObj dbObj = (CEfaDbObj) objsInGatex[NUM_SXANL].get(entAnl.getSxId());
            if (dbObj == null) {
                myClassErr("LOOSE EVENT: not in hash the anl " + entAnl.getSxId());
                continue;
            }

            // put this only if gatex gives repetead events
            boolean bEventValue = true;
            boolean bEventInv = true;

            // get sdt
            sdt = dbObj.getTimObj();
            if (sdt == null) {
                myClassErr("LOOSE EVENT: no sdt in the Anl " + entAnl.getSxId());
                efaEve = null;
                continue;
            }

            // calculate ttag
            long lTTag = System.currentTimeMillis();
            if (entAnl.getTTag().isTTagValid()) {
                lTTag = entAnl.getTTag().getTotalMS();
                lTTag = convUtc2Local(lTTag);
            }

            Timestamp timestamp = new Timestamp(lTTag);
            // get data address
            dataTagAddress = (ENSHardwareAddress) sdt.getHardwareAddress();
            if (dataTagAddress == null) {
                getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, "Hardware Address is null.", timestamp);
                myClassErr("LOOSE EVENT: no ENSHardwareAddress in the Anl " + entAnl.getSxId());
                continue;
            }

            // has to send cos
            if ((bEventInv && entAnl.getInvCode() != CEfaEntity.EFAENT_VALID && entAnl.getInvCode() != CEfaEntity.EFAENT_IMPOSED) && bEventValue) {
                bEventValue = false;
            }
            if (bEventInv && (entAnl.getInvCode() == CEfaEntity.EFAENT_VALID || entAnl.getInvCode() == CEfaEntity.EFAENT_IMPOSED)) {
                bEventValue = true;
                bEventInv = false;
            }

            if (bEventValue) {
                numEveAnlVal++;
//                if (debug2Cons) {
//                    System.out.println("<anl val event> n=" + numEveAnlVal);
//                }
                objToSend = TIMDriverSimpleTypeConverter.convert(sdt, entAnl.fGetValue());
                if (objToSend != null) {
                    // lTTag = convUtc2Local (lTTag);
                    myClassLog("   <<Update Tim Anl Value>>: ens id=" + entAnl.getSxId() + " tim id=" + entAnl.getOemId() + " with value =" + (int) entAnl.fGetValue() + " ttag=" + lTTag);
                    if (entAnl.getInvCode() == CEfaEntity.EFAENT_IMPOSED) {
                        getEquipmentMessageSender().sendTagFiltered(sdt, objToSend, lTTag, "Imposed ENS value");
                    } else {
                        getEquipmentMessageSender().sendTagFiltered(sdt, objToSend, lTTag);
                    }
                } else {
                    // myClassLog("   <<Update Tim Anl Status>>:  INVALIDATE SourceDataTagValue with quality CONVERSION_ERROR, for Tag name : "+sdt.getName()
                    // +" tag id : "+sdt.getId());
                    getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.CONVERSION_ERROR, "Unable to convert value " + (int) entAnl.fGetValue() + " to data type " + sdt.getDataType(),
                            timestamp);
                    bEventInv = false;
                }
                objToSend = null;
            }

            // has to send by cost
            if (bEventInv) {
                numEveAnlSt++;
//                if (debug2Cons) {
//                    System.out.println("<anl sta event> n=" + numEveAnlSt);
//                }
                convertEns2TimStatus(entAnl, sdt, lTTag);
            }

            efaEve = null;
            objToSend = null;
            entAnl = null;
            dbObj = null;
            sdt = null;
            dataTagAddress = null;

        } while (--iMaxEvents > 0);

        // 
        // counter events
        //
        // TODO This would be a wonderful place for a method and a for loop
        iMaxEvents = ENS_MAX_EVE_CNT;
        do {

            synchronized (monFinish) {
                efaEve = this.gxClient.getEvent(CEfaGatexClient.GX_EVETYP_CNT, 0);
            }

            if (efaEve == null) {
                break;
            }

            // get cnt in event
            CEfaEntityCnt entCnt = (CEfaEntityCnt) efaEve.getEntityRef();
            if (entCnt == null) {
                myClassErr("LOOSE EVENT: event has not counter");
                efaEve = null;
                continue;
            }

            // get object in hash
            CEfaDbObj dbObj = (CEfaDbObj) objsInGatex[NUM_SXCNT].get(entCnt.getSxId());
            if (dbObj == null) {
                myClassErr("LOOSE EVENT: not in hash the cnt " + entCnt.getSxId());
                efaEve = null;
                continue;
            }

            // put this only if gatex gives repetead events
            boolean bEventValue = true;
            boolean bEventInv = true;

            // get sdt
            sdt = dbObj.getTimObj();
            if (sdt == null) {
                myClassErr("LOOSE EVENT: no sdt in the Cnt " + entCnt.getSxId());
                continue;
            }

            // calculate ttag
            long lTTag = System.currentTimeMillis();
            if (entCnt.getTTag().isTTagValid()) {
                lTTag = entCnt.getTTag().getTotalMS();
                lTTag = convUtc2Local(lTTag);
            }

            Timestamp timestamp = new Timestamp(lTTag);
            // get data address
            dataTagAddress = (ENSHardwareAddress) sdt.getHardwareAddress();
            if (dataTagAddress == null) {
                getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, "Hardware Address is null.", timestamp);
                myClassErr("LOOSE EVENT: no ENSHardwareAddress in the Cnt " + entCnt.getSxId());
                continue;
            }

            // has to send cos
            if ((bEventInv && entCnt.getInvCode() != CEfaEntity.EFAENT_VALID && entCnt.getInvCode() != CEfaEntity.EFAENT_IMPOSED) && bEventValue) {
                bEventValue = false;
            }
            if (bEventInv && (entCnt.getInvCode() == CEfaEntity.EFAENT_VALID || entCnt.getInvCode() == CEfaEntity.EFAENT_IMPOSED)) {
                bEventValue = true;
                bEventInv = false;
            }

            if (bEventValue) {
                numEveCntVal++;
                objToSend = TIMDriverSimpleTypeConverter.convert(sdt, entCnt.fGetValue());
                if (objToSend != null) {
                    // lTTag = convUtc2Local (lTTag);
                    myClassLog("   <<Update Tim Cnt Value>>: ens id=" + entCnt.getSxId() + " tim id=" + entCnt.getOemId() + " with value =" + (int) entCnt.fGetValue() + " ttag=" + lTTag);
                    if (entCnt.getInvCode() == CEfaEntity.EFAENT_IMPOSED) {
                        getEquipmentMessageSender().sendTagFiltered(sdt, objToSend, lTTag, "Imposed ENS value");
                    } else {
                        getEquipmentMessageSender().sendTagFiltered(sdt, objToSend, lTTag);
                    }
                } else {
                    // myClassLog("   <<Update Tim Cnt Status>>:  INVALIDATE SourceDataTagValue with quality CONVERSION_ERROR, for Tag name : "+sdt.getName()
                    // +" tag id : "+sdt.getId());
                    getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.CONVERSION_ERROR, "Unable to convert value " + (int) entCnt.fGetValue() + " to data type " + sdt.getDataType(),
                            timestamp);
                    bEventInv = false;
                }
            }

            // has to send by cost
            if (bEventInv) {
                numEveCntSt++;
                convertEns2TimStatus(entCnt, sdt, lTTag);
            }

            efaEve = null;
            objToSend = null;
            entCnt = null;
            dbObj = null;
            sdt = null;
            dataTagAddress = null;

        } while (--iMaxEvents > 0);

        // TODO This would be a wonderful place for a method and a for loop
        // log events
        iMaxEvents = 50000; // TODO This should be a constant
        do {
            String logMsg = null;
            String errMsg = null;
            synchronized (monFinish) {
                logMsg = this.gxClient.getLogMsg();
                errMsg = this.gxClient.getErrMsg();
            }

            if (errMsg == null && logMsg == null) {
                break;
            }

            if (logMsg != null) {
                myClassLog("[Gx client] " + logMsg);
            }

            if (errMsg != null) {
                myClassErr("[Gx client] " + errMsg);
            }

            // TODO it is useless setting them null
            logMsg = null;
            errMsg = null;

        } while (--iMaxEvents > 0);
    }

//    /**
//     * get used memory (http://www.javaworld.com)
//     * 
//     * @return used memory in bytes
//     */
//    private static long usedMemory() {
//        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//    }

//    /**
//     * make gc clean, with finalizator (http://www.javaworld.com)
//     * 
//     */
//    private static void _runGC() throws Exception {
//        long usedMem1 = usedMemory(), usedMem2 = Long.MAX_VALUE;
//        for (int i = 0; (usedMem1 < usedMem2) && (i < 100); ++i) {
//            Runtime.getRuntime().runFinalization();
//            Runtime.getRuntime().gc();
//            Thread.currentThread().yield();
//            usedMem2 = usedMem1;
//            usedMem1 = usedMemory();
//        }
//    }

//    /**
//     * my garbage collector with finalizator
//     * 
//     */
//    private void gc() {
//        System.out.println("*** gc in *** ->" + usedMemory());
//
//        try {
//            _runGC();
//        } catch (Exception e) {
//        }
//
//        System.out.println("*** gc out *** ->" + usedMemory());
//    }

//    /**
//     * check gc calling
//     */
//    private boolean myGarbageCollector() {
//
//         cyclic garbage collector
//        if (lGcCounter < System.currentTimeMillis()) {
//            myClassLog("** free mem=" + Runtime.getRuntime().freeMemory());
//
//            myClassLog("** num eve dig=" + (numEveDigVal + numEveDigSt) + " [value " + numEveDigVal + "]" + " [status " + numEveDigSt + "]");
//            myClassLog("** num eve anl=" + (numEveAnlVal + numEveAnlSt) + " [value " + numEveAnlVal + "]" + " [status " + numEveAnlSt + "]");
//            myClassLog("** num eve cnt=" + (numEveCntVal + numEveCntSt) + " [value " + numEveCntVal + "]" + " [status " + numEveCntSt + "]");
//
//            lGcCounter = System.currentTimeMillis() + 1000 * CYC_GC;
//            if (minHeapSz2Work > 0) {
//                myClassLog("** start GARBAGE COLLECTOR t(overhead)** free mem=" + Runtime.getRuntime().freeMemory());
//                gc();
//                myClassLog("** end GARBAGE COLLECTOR t(overhead)** free mem=" + Runtime.getRuntime().freeMemory());
//            }
//        }

        // low memory, start gc
//        if (Runtime.getRuntime().freeMemory() < minHeapSz2Gc) {
//            lGcCounter = System.currentTimeMillis() + 1000 * CYC_GC;
//            if (minHeapSz2Work > 0) {
//                myClassLog("** start GARBAGE COLLECTOR m(can happen a overhead)** free mem=" + Runtime.getRuntime().freeMemory());
//                gc();
//                myClassLog("** end GARBAGE COLLECTOR m(can happen a overhead)** free mem=" + Runtime.getRuntime().freeMemory());
//            }
//        }
//
//        // low memory , stop process
//        if (minHeapSz2Work > 0) {
//            if (Runtime.getRuntime().freeMemory() < minHeapSz2Work) {
//                myClassErr("Out of memory - heap size in jvm is " + Runtime.getRuntime().freeMemory());
//                synchronized (monFinish) {
//                    flagFinish = true;
//                }
//                return false;
//            }
//        }
//
//        return true;
//    }

    // TODO Find out what ctr means.
    /**
     * get next ctr order
     * 
     * @return return ctr order
     */
    public int getCtrOrd() {
        int aux = 0;
        synchronized (monCtrOdr) {
            aux = ctrSendOrd;
            ctrSendOrd++;
            if (ctrSendOrd > 10000) { // TODO This should be a constant
                ctrSendOrd = 0;
            }
        }
        return aux;
    }

    /**
     * convert utc time to local (V1.1.0)
     * 
     * @param ttag The time in UTC format.
     * @return The converted time.
     */
    public long convUtc2Local(final long ttag) {
        long ltag = ttag;
        ltag += (60 * 60 * 1000) * gmtHourCorrection;
        return ltag;
    }

    /**
     * finalize method
     */
    protected void finalize() {

        synchronized (monFinish) {
            if (gxClient != null) {
                gxClient.stopConn(3);
            }
        }

        if (gxClient != null) {
            gxClient.myDestruct();
        }
        gxClient = null;
        strProp = null;
        objsInGatex = null;
        entDesc = null;
        numEnt = null;

        try {
            super.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Wrapper method which calls the old send command method.
     * 
     * @param sourceCommandTagValue The source command to run.
     * @exception EqCommandTagException Thrown if the command fails.
     */
    @Override
    public String runCommand(
            final SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
        sendCommand(sourceCommandTagValue);
        return null; // TODO perhaps something usefull
    }

    /**
     * Restarts the ENS Message Handler
     */
    public void restart() {
        synchronized (monFinish) {
            flagRestart = true;
        }
    }
}
