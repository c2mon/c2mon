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
// TIM Data Aquisition System. CERN. All rights reserved.
//  
// T Nick:           Date:       Info:
// -------------------------------------------------------------------------
// D wbuczak      12/July/2004    Class generation from the model
// P jletrasi     21/July/2004    JEC Protocol Implementation
// P jletrasi     25/July/2004    Comment ameiloration
// P jletrasi     08/Sept/2004    Set Time message added
// -------------------------------------------------------------------------

// PLC connection library
package ch.cern.tim.driver.jec;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import cern.tim.driver.common.EquipmentMessageHandler;
import cern.tim.driver.tools.equipmentexceptions.EqIOException;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import ch.cern.tim.driver.jec.config.JECCommandTagChanger;
import ch.cern.tim.driver.jec.config.JECDataTagChanger;
import ch.cern.tim.driver.jec.config.JECEquipmentConfigurationChanger;
import ch.cern.tim.driver.jec.config.PLCConfiguration;
import ch.cern.tim.driver.jec.frames.JECCommandRunner;
import ch.cern.tim.jec.ConnectionData;
import ch.cern.tim.jec.JECIndexOutOfRangeException;
import ch.cern.tim.jec.JECPFrames;
import ch.cern.tim.jec.StdConstants;

/**
 * This is a specialized subclass of the general EquipmentMessageHandler. The
 * class is used as a communication driver - data sources using JEC protocol
 */
public class JECMessageHandler extends EquipmentMessageHandler implements Runnable {
    /**
     * Time to wait till the retry after a startup message could not be sent.
     */
    private static final int SEND_STARTUP_MESSAGE_FAILURE_WAIT_TIME = 1000;
    /**
     * Constant to convert milliseconds to hours.
     */
    private static final long MS_TO_HOUR_FACTOR = 3600000L;
    /**
     * Constant to recognize invalidation ability.
     */
    private static final int INVALIDATION_ABILITY_DATA_TYPE = 0x01;

    /**
     * Constant to recognize invalidation ability.
     */
    private static final int INVALIDATION_ABILITY_SEQUENCE_NUMBER = 0x0A;

    /**
     * This is the message received if a slave is lost.
     */
    public static final String HIERARCHICAL_INVALIDATION_MESSAGE = "PLC slave lost";

    /**
     * Multiplier to adjust the handler period for the connection sampler.
     */
    private static final int HANDLER_PERIOD_MULTIPLIER = 3;
    
    /**
     * This is the number of receiving errors allowed during startup till
     * startup is stopped and retried.
     */
    private static final int MAX_FAILED_RECEIVES = 5;
    
    /**
     * Only reject alives on seq. number basis if one has been sent
     * in the last MAX_REJECT_ALIVE_TIME to server.
     */
    private static final int MAX_REJECT_ALIVE_TIME = 30000;

    /**
     * The PLC configuration object to access all values of the PLC.
     */
    private final PLCConfiguration plcConfiguration = new PLCConfiguration();
    
    /**
     * The connection sampler monitors the connection to the PLC and restarts it if necessary.
     */
    private PLCConnectionSampler connectionSamplerThread;

    /**
     * The synchronization timer to keep host and PLC time synchronized.
     */
    private SynchronizationTimer synchronisationTimerThread;

    /**
     * JEC controller to configure the used frames and process them.
     */
    private JECController jecController;

    /**
     * A flag stating if the handler is currently connected to PLC or not.
     * -1 means not connected 0 means connected! See StdConstants.ERROR and 
     * StdConstants.SUCCESS
     */
    private volatile int connected = -1;

    /**
     * JEC's PLCDriver object - Interface to SEND, RECEIVE, CONNECT and
     * DISCONNECT
     */
    private PLCObjectFactory plcFactory;

    /**
     * JEC's connection data structure
     */
    private ConnectionData currentConnData;

    /**
     * JEC sender's frame - used to store all received frames
     */
    private JECPFrames sendFrame;

    /**
     * JEC receiver's frame - used to store all frames to be sent
     */
    private JECPFrames recvFrame;

    /**
     * This variable is used to store the actual host time. Needed for the JEC
     * SET TIME message.
     */
    private long actTime = 0;

    /**
     * TODO TIM-807 : no longer need this, move to JECController and use single seq number
     * This variable is used to save the supervision alive message sequence
     * number
     */
    private byte superSeqNumber = 0x00;
    
    /**
     * TODO move to JECController
     * Time of last supervision alive. Use this in conjunction to superSeqNumber
     * to filter out repeated messages from PLC (is necessary while PLC uses
     * single sequence and JEC DAQ seq per type - see TIMS-751).
     */
    private AtomicLong lastSupervisionAlive = new AtomicLong(0);

    /**
     * Field to store the currently connected PLC.
     */
    private String currentPLC;

    /**
     * JEC Command Runner to process commands.
     */
    private JECCommandRunner jecCommandRunner;

    /**
     * A timed restarter for the handler. It is used to delay a necessary
     * reconfiguration restart to avoid multiple restarts per configuration step.
     */
    private IJECRestarter jecRestarter;

    /**
     * The default constructor
     */
    public JECMessageHandler() {
        /* Nothing to do */
    }

    /**
     * This method is responsible for opening subscriptions for all supervised
     * SourceDataTags (data point elements)
     * 
     * @throws EqIOException Thrown if the connection to the PLC fails.
     */
    public void connectToDataSource() throws EqIOException {
        getEquipmentLogger().info("Entering connectToDataSource...");
        // Versioning protocol:
        // '.' indicates a major version
        // '_' indicates an update release
        getEquipmentLogger().info("TIM JEC Message Handler - Version 1.2.5 ");
        getEquipmentLogger().info("2004-2007 CERN - TS/CSE/CO - All rights reserved");
        try {
            plcConfiguration.parsePLCAddress(getEquipmentConfiguration().getAddress());
        } catch (Exception ex) {
            getEquipmentLogger().error("Unexpected error while parsing PLC address occured " + ex);
            throw new EqIOException("Error while parsing equipment address occured: " + ex);
        }
        
        logConfiguration();
        
        String protocol = plcConfiguration.getProtocol();
        try {
            plcFactory = new PLCObjectFactory(plcConfiguration);
        }
        catch (java.lang.ClassNotFoundException ex) {
            getEquipmentLogger().fatal("Class ch.cern.tim.jec." + protocol + " could not be found");
            throw new EqIOException("Class ch.cern.tim.jec." + protocol + " could not be found");
        }
        catch (java.lang.IllegalAccessException ex) {
            getEquipmentLogger().fatal("Could not access to class ch.cern.tim.jec." + protocol);
            throw new EqIOException("Could not access to class ch.cern.tim.jec." + protocol);
        }
        catch (InstantiationException ex) {
            getEquipmentLogger().fatal("Class ch.cern.tim.jec." + protocol + " could not be instantiated");
            throw new EqIOException("Class ch.cern.tim.jec." + protocol + " could not be instantiated");
        }
       
        jecCommandRunner = new JECCommandRunner(getEquipmentLogger(JECCommandRunner.class), plcFactory, getEquipmentConfiguration());
        getEquipmentCommandHandler().setCommandRunner(jecCommandRunner);        
        
        jecRestarter = new TimedJECRestarter(this);       
        connectionSamplerThread = new PLCConnectionSampler(jecRestarter, getEquipmentLogger(PLCConnectionSampler.class), 
                                                                plcConfiguration.getHandlerPeriod() * HANDLER_PERIOD_MULTIPLIER);        
        jecController = new JECController(plcFactory, connectionSamplerThread, jecCommandRunner, getEquipmentMessageSender(), getEquipmentLoggerFactory());
              
        JECDataTagChanger dataTagChanger = new JECDataTagChanger(jecController, jecRestarter);
        getEquipmentConfigurationHandler().setDataTagChanger(dataTagChanger);
        JECCommandTagChanger commandTagChanger = new JECCommandTagChanger(jecController, jecRestarter);
        getEquipmentConfigurationHandler().setCommandTagChanger(commandTagChanger);
        JECEquipmentConfigurationChanger equipmentConfigurationChanger = new JECEquipmentConfigurationChanger(jecRestarter);
        getEquipmentConfigurationHandler().setEquipmentConfigurationChanger(equipmentConfigurationChanger);
        sendFrame = plcFactory.getSendFrame(StdConstants.INIT_MSG);
        recvFrame = plcFactory.getRawRecvFrame();

        new Thread(this, "JECMessageHandlerThread").start();

        getEquipmentLogger().info("exiting connectToDataSource...");
    }
    
    /**
     * This method closes all previously opened subscriptions.
     * 
     * @throws EqIOException Throws an exception if the disconnection fails through an IO error.
     */
    public void disconnectFromDataSource() throws EqIOException {
        getEquipmentLogger().info("Disconnecting from data source");
        connectionSamplerThread.shutdown();
        jecRestarter.shutdown();        
        if (connected == StdConstants.SUCCESS) {
            connected = StdConstants.ERROR;
            plcFactory.getPLCDriver().Disconnect(currentConnData);
        }
        // makes sure that the message processor threads stop
        jecController.stopFrameProcessing();        
        synchronisationTimerThread.shutdown();          
        getEquipmentLogger().info("... successfully disconnected.");
    }
    
    /**
     * Gets all values from the PLC and sends them to the server.
     */
    @Override
    public void refreshAllDataTags() {
        // TODO Might be done via a reconnect
    }

    /**
     * Gets the value of a data tag from the PLC and sends it to the server.
     * 
     * @param dataTagId The id of the data tag to refresh. 
     */
    @Override
    public void refreshDataTag(final long dataTagId) {
        // TODO Implement this method.
    }
    
    /**
     * This is the thread's "main" method. It is responsible for acquiring and
     * processing the data coming from PLC and check connection status.
     */
    public void run() {
        getEquipmentLogger().info("Starting new JECMessageHandler's thread...");
        // Variable to store the actual connect attempt number - reseted
        int connectionAttempts = 0;
        // Try to connect
        while (!connectToPLC(getNextPLCToConnect())) {
                // If connection attempt has failed... increase the attempt number
                // Result of this equation will prevent overflow (0...max_INT)
                connectionAttempts = (connectionAttempts + 1) % Integer.MAX_VALUE;
    
                // If maximum number of attempts send commFault
                if (connectionAttempts == StdConstants.maxConnAttempt) {
                    getEquipmentLogger().info("Max number of attempts reached!...Sending CommfaultTag and continuing to retry...");
                    getEquipmentMessageSender().confirmEquipmentStateIncorrect("Maximum number of attempts to reconnect to PLC has been reached");
                    connectionAttempts = 0;
                }
                else
                    getEquipmentLogger().info("Tried to connect to " + currentPLC.toUpperCase() + " - attempt " + connectionAttempts + " of " + StdConstants.maxConnAttempt);
                
                getEquipmentLogger().warn("Could not connect to PLC! JECMessageHandler will try again in " + StdConstants.reconnectTimeout + " seconds...");
                try {
                    // Waits 5 seconds (5000ms) (hadoc value - must be optimized)
                    Thread.sleep(StdConstants.reconnectTimeout);
                } catch (InterruptedException interruptedEx) {
                    getEquipmentLogger().error("Reconnection waiting interrupted.", interruptedEx);
                }
        }     
                
        // Tries to start the method responsible for the JEC INITIALIZATION
        try {
            configurePLC();
            
            startThreads();
        }
        catch (IOException ex) {
            getEquipmentLogger().error("Error during INITIALIZATION procedure - attempting to restart the handler.", ex);
            
            //start timer to restart the JECMessageHandler until successful (tries every 5s)
            final Timer restartTimer = new Timer(true);
            restartTimer.schedule(new TimerTask() {
              
              @Override
              public void run() {
                try {
                  disconnectFromDataSource();
                } catch (EqIOException e) {
                  getEquipmentLogger().error("Error disconnecting from datasource.", e);
                }
                try {                  
                  connectToDataSource();
                } catch (EqIOException e) {                  
                  restartTimer.schedule(this, 5000);
                }
              }
            }, 5000);
            
            //this thread ends
            return;
        } 
        getEquipmentLogger().info("Sending EquipmentState OK...");
        getEquipmentMessageSender().confirmEquipmentStateOK();
        
        runFrameAquisition();
        
        getEquipmentMessageSender().confirmEquipmentStateIncorrect("Connection with JEC PLC lost");
    }
    

    /**
     * This method is responsible for the JECMessageHandler configuration -
     * Source Data Tag extraction and validation - Data and Command (BOOL and
     * ANALOG) table construction - PLC INITIALIZATION sequence
     * 
     * @throws IOException Throws an IOException if the configuration fails.
     */
    private void configurePLC() throws IOException {
        getEquipmentLogger().info("Entering ConfigureJECProcess...");
        
        configureTags();

        jecController.initArrays();

        getEquipmentLogger().info("--------------------------------------------");
        getEquipmentLogger().info("  I N I T I A L I Z I N G    J E C   P L C  ");
        getEquipmentLogger().info("--------------------------------------------");

        byte jecSequenceNumber = 1;
        sendInitMessage(jecSequenceNumber++);
        sendSetConfiguration(jecSequenceNumber++);
        sendAnalogDeadbands(jecSequenceNumber++);
        sendEndOfConfiguration(jecSequenceNumber++);
        sendGetAllData(jecSequenceNumber);

        receiveHierarchicalInvalidation();
        receiveBooleanControlMessage();
        receiveAnalogControlMessage();
        receiveStartupData();

        getEquipmentLogger().info("INIT step 10: Initialization procedure complete!");

        getEquipmentLogger().info("------------------------------------------------------------------");
        getEquipmentLogger().info("  J E C   P L C   I N I T I A L I Z A T I O N    C O M P L E T E  ");
        getEquipmentLogger().info("------------------------------------------------------------------");
        getEquipmentLogger().info("Leaving ConfigureJECProcess...");
    }

    /**
     * Logs the current configuration.
     */
    private void logConfiguration() {
        // Creates a string with separated parameters (for Logger)
        StringBuffer configurationLogMessage = new StringBuffer();
        configurationLogMessage.append("Primary PLC: " + plcConfiguration.getPlcName());
        configurationLogMessage.append(" Redundant PLC: " + plcConfiguration.getPlcNameRed());
        configurationLogMessage.append(" Protocol: " + plcConfiguration.getProtocol());
        configurationLogMessage.append(" Port: " + plcConfiguration.getPort());
        configurationLogMessage.append(" Sync type: " + plcConfiguration.getTimeSync());
        configurationLogMessage.append(" Handler Alive Period: " + plcConfiguration.getHandlerPeriod());
        configurationLogMessage.append("ms Supervison Alive Period: " + getEquipmentConfiguration().getAliveTagInterval()); 
        configurationLogMessage.append("ms Source TSAP: " + plcConfiguration.getsTsap());
        configurationLogMessage.append(" Destination TSAP: " + plcConfiguration.getdTsap() + " DP Slaves: ");
        Vector<Byte> dpSlaveAddress = plcConfiguration.getDpSlaveAddresses();
        // If DP Slave Table is not empty
        if (!dpSlaveAddress.isEmpty()) {
            // Extract all slave ID's inside the array into this string
            for (int i = 0; i < dpSlaveAddress.size(); i++) {
                // Note: These values are of type short (16bit)
                configurationLogMessage.append(dpSlaveAddress.get(i));
                if (i != (dpSlaveAddress.size() - 1)) {
                    configurationLogMessage.append(", ");
                }
            }
        } else
            configurationLogMessage.append("none");
        
        getEquipmentLogger().info(configurationLogMessage.toString());
    }

    /**
     * Starts all threads needed by the message handler.
     */
    private void startThreads() {
        getEquipmentLogger().debug("calling startThreads()...");
        jecController.startFrameProcessing();
        connectionSamplerThread.start();        
        if (synchronisationTimerThread == null) {
            synchronisationTimerThread = new SynchronizationTimer(getEquipmentLogger(SynchronizationTimer.class), plcFactory);
        }
        else {
            synchronisationTimerThread.setPause(false);
        }
        // Initializes the actTime variable with the host current time
        actTime = System.currentTimeMillis();
    }

    /**
     * This method runs the frame acquisition. It will run all the time
     * and it will only return if the data acquisition fails or the connection
     * is lost.
     */
    private void runFrameAquisition() {
        getEquipmentLogger().info("Starting acquisition procedure");
        // Data acquisition loop
        while (connected != StdConstants.ERROR) {
            JECPFrames recvFrame = plcFactory.getRawRecvFrame();
            synchronized (synchronisationTimerThread) {
                try {
                    jecSetTime(StdConstants.SET_TIME_DELAY, (byte) (sendFrame.GetSequenceNumber() + 0x01));
                } catch (IOException e) {
                    getEquipmentLogger().error("Error while trying to re-synchronize JEC PLC");
                }
                // Try to receive data
                if (plcFactory.getPLCDriver().Receive(recvFrame, StdConstants.TIMEOUT) == StdConstants.ERROR) {
                    getEquipmentLogger().error("Error during frame reception (possible timeout)");
                }
                else {
                    getEquipmentLogger().debug("Received frame: " + recvFrame.getMsgID() + ", Sequence Number: " + recvFrame.GetSequenceNumber());
                    // Checks which type of message was received
                    //always acknowledge supervision messages
                    //TODO TIM-808 remove this processing to the pushFrame in the JECController with other frame processing
                    if (recvFrame.getMsgID() == StdConstants.SUPERV_ALIVE_MSG) {
                        processSupervisionFrame(recvFrame); //also acknowledges
                    }
                    else {
                        jecController.pushFrame(recvFrame);
                    }
                }
            }
        }
        getEquipmentLogger().info("Acquisition procedure...STOPPED!!");
    }

    /**
     * Checks the received supervision frame and sends a supervision alive message.
     * TODO TIM-808 move to JECController & extract acknowledgment to pushFrame method
     * @param supervisionFrame The supervision frame received from the PLC.
     */
    private void processSupervisionFrame(final JECPFrames supervisionFrame) {
        if (supervisionFrame.GetSequenceNumber() != superSeqNumber 
            || System.currentTimeMillis() - lastSupervisionAlive.get() > MAX_REJECT_ALIVE_TIME) {
            // Acknowledge the received message
            try {
                jecController.acknowledgeReceivedMessage(sendFrame, supervisionFrame);
                getEquipmentLogger().debug("SUPERVISION ALIVE MESSAGE: Acknowledgement succeeded!");
                // Store the last sequence number of this kind of
                // message to avoid repetitions
                superSeqNumber = supervisionFrame.GetSequenceNumber();               
                getEquipmentMessageSender().sendSupervisionAlive(System.currentTimeMillis());
                lastSupervisionAlive.set(System.currentTimeMillis());
                connectionSamplerThread.updateAliveTimer();
            } catch (IOException e) {
                getEquipmentLogger().error("SUPERVISION ALIVE MESSAGE: Error while acknowledging received message to JEC");
            }
        } else {
            // Even if message is discarded, still acknowledges it
            try {
                jecController.acknowledgeReceivedMessage(sendFrame, supervisionFrame);
                getEquipmentLogger().debug("SUPERVISION ALIVE MESSAGE DISCARDED: Acknowledgement succeeded");
                // Store the last sequence number of this kind of
                // message to avoid repetitions
                superSeqNumber = supervisionFrame.GetSequenceNumber();
                connectionSamplerThread.updateAliveTimer();
            } catch (IOException e) {
                getEquipmentLogger().error("SUPERVISION ALIVE MESSAGE DISCARDED: Error while acknowledging received message to JEC");
            }
        }
    }

    /**
     * This method returns always the next PLC to connect to. Therefore
     * it looks at the current PLC: If it is the standard one it chooses
     * the redundant one. If it is the redundant one it chooses the normal
     * one. If no redundant one exists it returns always the normal one.
     * 
     * @return The next PLC to try to connect.
     */
    private String getNextPLCToConnect() {
        if (currentPLC == null || currentPLC.equals(plcConfiguration.getPlcNameRed())) {
            currentPLC = plcConfiguration.getPlcName();
        }
        else if (currentPLC.equals(plcConfiguration.getPlcName()) 
                && !plcConfiguration.getPlcNameRed().equals("")) {
            currentPLC = plcConfiguration.getPlcNameRed();
        }
        return currentPLC;
    }

    /**
     * Creates the connection data and tries to connect to the PLC.
     * 
     * @param plcName The name of the PLC to connect to.
     * @return Returns True if the connection was successfully established else false.
     */
    private boolean connectToPLC(final String plcName) {
        currentConnData = plcFactory.createConnectionData(plcName);
        connected = plcFactory.getPLCDriver().Connect(currentConnData);
        return connected == StdConstants.SUCCESS;
    }

    /**
     * Sends a set configuration message to the PLC.
     * 
     * @param sequenceNumber The Sequence number to use.
     * @throws IOException Throws an IOException if the sending to the PLC fails.
     */
    private void sendSetConfiguration(final byte sequenceNumber) throws IOException {
        getEquipmentLogger().debug("Sending SET CONFIGURATION to PLC...");
        getEquipmentLogger().info("INIT step 2: Sending CONFIGURATION message to PLC");
        
        sendFrame = jecController.getSetConfigurationMessage();
        sendFrame.SetSequenceNumber(sequenceNumber);
        
        sendStartupMessage("PLC CONFIG DATA Message", System.currentTimeMillis());
    }

    /**
     * Sends an initialization message to the server.
     * 
     * @param sequenceNumber The Sequence number to use.
     * @throws IOException Throws an IOException if the sending to the PLC fails. 
     */
    private void sendInitMessage(final byte sequenceNumber) throws IOException {
        getEquipmentLogger().info("INIT step 1: Sending INIT message to PLC");
        sendFrame.UpdateMsgID(StdConstants.INIT_MSG);
        sendFrame.SetSequenceNumber(sequenceNumber);
        sendFrame.ToHandlerAlivePeriod(plcConfiguration.getHandlerPeriod());
        sendFrame.ToSupervisionAlivePeriod(getEquipmentConfiguration().getAliveTagInterval());
        // Set the received synchronization type to be used
        String timeSync = plcConfiguration.getTimeSync();
        if (timeSync.equalsIgnoreCase("Jec"))
            sendFrame.SetSyncType(StdConstants.SYNC_JEC);
        else if (timeSync.equalsIgnoreCase("Ntp"))
            sendFrame.SetSyncType(StdConstants.SYNC_NTP);
        else
            getEquipmentLogger().error("Invalid Syncronization method received from DB");

        sendFrame.JECSynchronize();
        sendStartupMessage("INIT Message", StdConstants.NO_WATCHDOG);
    }

    /**
     * Receives the startup data from the PLC.
     * 
     * @throws IOException Throws an IOException if the sending to the PLC fails.
     */
    private void receiveStartupData() throws IOException {
        getEquipmentLogger().info("INIT step 9: Receives ALL STARTUP DATA from PLC");
        getEquipmentLogger().debug("Receiving Startup Data from PLC...");

        int nrOfBoolMessages = jecController.getNumberOfBooleanDataJECFrames();
        getEquipmentLogger().debug("NUMBER OF BOOLEAN MESSAGES EXPECTED: " + nrOfBoolMessages);
        int nrOfAnalogMessages = jecController.getNumberOfAnalogDataJECFrames();
        getEquipmentLogger().debug("NUMBER OF ANALOG MESSAGES EXPECTED: " + nrOfAnalogMessages);

        
        long watchDogTime = System.currentTimeMillis();
        // Counter for receive failures if there are too many an IOException is thrown
        int connectionFailCount = 0;
        // While there are messages to be received
        while ((nrOfBoolMessages + nrOfAnalogMessages) > 0) {
            // Tests watchdog to see if the max time was elapsed
            try {
                watchDogTestOK(watchDogTime);
                getEquipmentLogger().debug("Watchdog test: OK!");
            } catch (IOException ex) {
                getEquipmentLogger().error("ERROR WHILE RECEIVING DATA: Watchdog Timeout detected for GET DATA message");
                throw new IOException("INIT WATCHDOG: Timeout reached to Initialize PLC...restarting");
            }

            // If there's a problem while receiving:
            if (plcFactory.getPLCDriver().Receive(recvFrame, StdConstants.TIMEOUT) == StdConstants.ERROR) {
                getEquipmentLogger().error("Error occured while receiving JEC data from PLC (" + ++connectionFailCount + "/" + MAX_FAILED_RECEIVES + ")");
                if (connectionFailCount == MAX_FAILED_RECEIVES)
                    throw new IOException("Receiving all startup data failed!");
            }
            // If the reception is OK
            else {
                // If a boolean block is received
                if (recvFrame.getMsgID() == StdConstants.BOOL_DATA_MSG) {
                    if (jecController.processFrame(recvFrame)) {
                        getEquipmentLogger().debug("BOOLEAN DATA BLOCK RECEIVED");
                        getEquipmentLogger().debug("BLOCK NUMBER: " + recvFrame.GetDataStartNumber());
                        getEquipmentLogger().debug("DATA OFFSET: " + recvFrame.GetDataOffset());
                        nrOfBoolMessages--;
                    }
                }
                // If an analog block is received
                else if (recvFrame.getMsgID() == StdConstants.ANALOG_DATA_MSG) {
                    if (jecController.processFrame(recvFrame)) {
                        getEquipmentLogger().debug("ANALOG DATA BLOCK RECEIVED");
                        getEquipmentLogger().debug("BLOCK NUMBER: " + recvFrame.GetDataStartNumber());
                        getEquipmentLogger().debug("DATA OFFSET: " + recvFrame.GetDataOffset());
                        nrOfAnalogMessages--;
                    }
                }
                else
                    getEquipmentLogger().error("GET ALL DATA ERROR: Invalid data type received " + recvFrame.getMsgID());
            }
        }
        if (nrOfAnalogMessages != 0 || nrOfBoolMessages != 0) {
            throw new IOException("Error retrieving startup data.");
        }
        getEquipmentLogger().info("STARTUP DATA WAS SUCCESSFULLY RECEIVED");
    }
    
    /**
     * Receives the analog control message from the PLC.
     * 
     * @throws IOException Throws an IOException if the sending to the PLC fails.
     */
    private void receiveAnalogControlMessage() throws IOException {
        getEquipmentLogger().info("INIT step 8: Receives ANALOGUE CONTROL message from PLC");
        try {
            getStartupMessage(recvFrame, StdConstants.ANA_DATA_CTRL_MSG, "ANALOG CONTROL message", System.currentTimeMillis());
            jecController.processFrame(recvFrame);
        } catch (IOException ex) {
            getEquipmentLogger().debug(ex);
            throw new IOException(ex.getMessage());
        }
    }

    /**
     * Receives the boolean control message from the PLC.
     * 
     * @throws IOException Throws an IOException if the sending to the PLC fails.
     */
    private void receiveBooleanControlMessage() throws IOException {
        getEquipmentLogger().info("INIT step 7: Receives BOOLEAN CONTROL message from PLC");
        try {
            getStartupMessage(recvFrame, StdConstants.BOOL_DATA_CTRL_MSG, "BOOLEAN CONTROL message", System.currentTimeMillis());
            jecController.processFrame(recvFrame);
        } catch (IOException ex) {
            getEquipmentLogger().error(ex);
            throw ex;
        }
    }

    /**
     * Receives the hierarchical invalidation abilities and if possible the invalid
     * slaves from the PLC.
     * 
     * @throws IOException Throws an IOException if the sending to the PLC fails.
     */
    private void receiveHierarchicalInvalidation() throws IOException {
        getEquipmentLogger().info("INIT step 6: Detecting if PLC handles hierarchical invalidation...");
        // get first frame
        byte lastSequenceNumber;
        try {
            lastSequenceNumber = getStartupMessage(recvFrame, StdConstants.INFO_MSG, "INFO Message", System.currentTimeMillis());
        } catch (IOException ex) {
            getEquipmentLogger().error("Error while receiving INFO message: " + ex, ex);
            throw ex;
        }
        // If Sequence number is 1 and first data word is 0x0001, means that
        // this process does not support Invalidation
        if ((recvFrame.GetSequenceNumber() == 0x01) && (recvFrame.GetJECWord(1) == 0x0001)) {
            getEquipmentLogger().debug("This process does not support tag invalidation...continuing");
        }
        // If sequence number is 10 and datatype is 1, means that this process
        // supports Invalidation (multi-messages comming)
        else if ((recvFrame.GetSequenceNumber() == INVALIDATION_ABILITY_SEQUENCE_NUMBER) 
                && (recvFrame.GetDataType() == INVALIDATION_ABILITY_DATA_TYPE)) {
            getEquipmentLogger().debug("This process supports tag invalidation...wait to receive messages");
            while (recvFrame.GetDataType() != 0x02) {
                getEquipmentLogger().debug("Data Type Received for INFO message: " + recvFrame.GetDataType());
                // Temporary variable to extract the String sent by the PLC
                jecController.processFrame(recvFrame);
                // get next message
                try {
                    lastSequenceNumber = getStartupMessage(recvFrame, StdConstants.INFO_MSG, "INFO Message", System.currentTimeMillis());
                    getEquipmentLogger().debug("Validation/Invalidation INFO message received...");
                } catch (IOException ex) {
                    getEquipmentLogger().error("Error while receiving V/I INFO message: ", ex);
                    throw ex;
                }
            }

            getEquipmentLogger().debug("Last INFO message received with data type = 2...");
        }
        // First INFO message has a problem - exits throwing exception
        else {
            getEquipmentLogger().debug("Invalid info message received: wrong header parameters");
            // Stops, throwing an exception - cannot continue
            throw new IOException("INFO - Could not proceed: Invalid header");
        }
        if (lastSequenceNumber != recvFrame.GetSequenceNumber()) {
            jecController.acknowledgeReceivedMessage(sendFrame, recvFrame);
        }
    }

    /**
     * Sends the get all data message to the PLC.
     * 
     * @param jecSequenceNumber The Sequence number to use.
     * @throws IOException Throws an IOException if the sending to the PLC fails.
     */
    private void sendGetAllData(final byte jecSequenceNumber) throws IOException {
        getEquipmentLogger().info("INIT step 5: Sending GET ALL DATA message to PLC");
        getEquipmentLogger().debug("Sending GET ALL DATA MESSAGE to PLC...");
        
        sendFrame.UpdateMsgID(StdConstants.GET_ALL_DATA_MSG);
        sendFrame.SetSequenceNumber(jecSequenceNumber);

        sendStartupMessage("GET ALL DATA Message", System.currentTimeMillis());
    }

    /**
     * Informs the PLC about the end of the configuration.
     * 
     * @param jecSequenceNumber The Sequence number to use.
     * @throws IOException Throws an IOException if the sending to the PLC fails.
     */
    private void sendEndOfConfiguration(final byte jecSequenceNumber) throws IOException {
        getEquipmentLogger().info("INIT step 4: Sending END OF CONFIGURATION message to PLC");
        getEquipmentLogger().debug("Sending END OF CONFIGURATION to PLC...");
        
        sendFrame.UpdateMsgID(StdConstants.END_CFG_MSG);
        sendFrame.SetSequenceNumber(jecSequenceNumber);

        sendStartupMessage("END OF CONFIGURATION Message", System.currentTimeMillis());
    }

    /**
     * Sends the analog deadbands to the PLC.
     * 
     * @param jecSeqeunceNumber The sequence number to use for the send frame.
     * @throws IOException Throws an IOException if the sending to the PLC fails.
     */
    private void sendAnalogDeadbands(final byte jecSeqeunceNumber) throws IOException {
        getEquipmentLogger().info("INIT step 3: Sending ANALOGUE DEADBANDS message to PLC");
        int analogDBBlocksToSend = jecController.getNumberOfAnalogDataJECFrames();
        if (analogDBBlocksToSend > 0) {
            getEquipmentLogger().debug("Sending Analog DEADBANDS to PLC...");
            short blockID = 0;
            while (analogDBBlocksToSend > 0) {
                blockID = (short) (analogDBBlocksToSend - 1);
                try {
                    sendFrame = jecController.getDeadbandFrame(blockID);
                } catch (JECIndexOutOfRangeException ex) {
                    getEquipmentLogger().error("ERROR while writing data to JEC frame - " + ex);
                }
                sendFrame.SetSequenceNumber(jecSeqeunceNumber);
                sendStartupMessage("PLC DEADBAND Message", System.currentTimeMillis());
                analogDBBlocksToSend--;
            }
        }
        else
            getEquipmentLogger().debug("SKIP Sending Deadbands - No ANALOG data points defined");
    }

    /**
     * Updates the configuration for all data tags.
     */
    private void configureTags() {
        for (ISourceCommandTag sourceCommandTag : getEquipmentConfiguration().getSourceCommandTags().values()) {
            jecController.configureCommandTag(sourceCommandTag);
        }
        for (ISourceDataTag sourceDataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
            jecController.configureDataTag(sourceDataTag);
        }
        
    }

    /**
     * This method tries to receive data from PLC and check if the received
     * message is the expected one.
     * 
     * @param msgDescriptor The message descriptor of the message send.
     * TODO Is it really necessary to log the descriptor here? Because it is only provided to the method to be logged.
     * @throws IOException Throws an exception if the connection to the PLC fails.
     * @return True if the expected message was successfully received else false.
     */
    private boolean receiveAndCheck(final String msgDescriptor) throws IOException {
        boolean success;
        if (plcFactory.getPLCDriver().Receive(recvFrame, StdConstants.TIMEOUT) == StdConstants.SUCCESS) {
            if (recvFrame.getMsgID() == StdConstants.NACK_MSG) {
                if (recvFrame.GetSequenceNumber() == sendFrame.GetSequenceNumber()) {
                    // Some logging to flag a good acknowledge reception
                    getEquipmentLogger().debug(msgDescriptor + ": NACK received from PLC");
                    // Acknowledge back the received acknowledge
                    jecController.acknowledgeReceivedMessage(sendFrame, recvFrame);
                    success = true;
                }
                else {
                    getEquipmentLogger().warn("RECEIVED NON ACKNOWLEDGE IS NOT THE ONE EXPECTED!");
                    success = false;
                }
            }
            else if (recvFrame.getMsgID() == StdConstants.ACK_MSG) {
                if (recvFrame.GetSequenceNumber() == sendFrame.GetSequenceNumber()) {
                    // Some logging to flag a good acknowledge reception
                    getEquipmentLogger().debug(msgDescriptor + ": ACK received from PLC");
                    // Acknowledge back the received acknowledge
                    jecController.acknowledgeReceivedMessage(sendFrame, recvFrame);
                    success = true;
                }
                else {
                    getEquipmentLogger().warn("RECEIVED ACKNOWLEDGE IS NOT THE ONE EXPECTED!");
                    success = false;
                }
            }
            else {
                getEquipmentLogger().warn("Unexpected Message Received...DISCARDED!");
                success = false;
            }
        }
        else {
            getEquipmentLogger().error("Error while receiving response from PLC");
            success = false;
        }
        return success;
    }

    /**
     * This method is used to send the time synchronization with a predefined
     * period received from the database (in hours). This method is used to
     * guarantee a minimum error between the time in the PLC and the time in the
     * host.
     * 
     * @param delay Delay between host timestamps sent to the PLC by JEC (hours)
     * @throws IOException Throws an IOException if the synchronization with the PLC
     * fails through a connection error.
     */
    private void jecSetTime(final int delay, final byte jecSequenceNumber) throws IOException {
        // If the host actual time minus the reference time is bigger than the
        // delay converted into milliseconds (hour * 60 * 60 * 1000 = hours *
        // 3600000), then send the Set Time message
        if ((System.currentTimeMillis() - actTime) >= (delay * MS_TO_HOUR_FACTOR)) {
            JECPFrames localSendFrame = plcFactory.getSendFrame(StdConstants.SET_TIME_MSG);
            localSendFrame.SetMessageIdentifier(StdConstants.SET_TIME_MSG);
            // Set the sequence number: last sequence number + 1
            localSendFrame.SetSequenceNumber(jecSequenceNumber);
            // Assigns the actual host time to the JEC frame
            localSendFrame.JECSynchronize();
            // Some logging
            getEquipmentLogger().debug("Sending SET TIME MESSAGE...");
            // If there was a problem during send, exits reporting error
            if (plcFactory.getPLCDriver().Send(localSendFrame) == StdConstants.ERROR) {
                getEquipmentLogger().error("A problem occured while trying to send SET TIME message...");
            }
            actTime = System.currentTimeMillis();
        }
    }

    /**
     * This method is used to send the messages during initialization. If
     * there's a problem while trying to send any of these messages, the message
     * is resent until it's successfully sent. Or the difference between the 
     * System time and the provided watchdog value gets bigger than 
     * StdConstants.watchdogTimeout.
     * 
     * @param msgDescriptor - String that identifies the type of message sent
     * @param watchDog - Timeout value for the watchdog
     * @throws IOException Throws an exception if the sending fails through a connection problem
     * with the PLC.
     */
    private void sendStartupMessage(final String msgDescriptor, 
            final long watchDog) throws IOException {
        boolean successfullySent = false;
        short sendAttempts = 0;
        // Retries sending until it receives the good acknowledge
        while (!successfullySent && sendAttempts < 4) {
            // Tests the watchdog value
            if (watchDog != StdConstants.NO_WATCHDOG) {
                try {
                    watchDogTestOK(watchDog);
                    getEquipmentLogger().debug("Watchdog test: OK!");
                } catch (IOException ex) {
                    getEquipmentLogger().error("ERROR WHILE SENDING: Watchdog Timeout detected for message " + msgDescriptor);
                    throw new IOException("Watchdog timeout reached while sending " + msgDescriptor);
                }
            }

            // If there was a problem during send, exits reporting error
            if (plcFactory.getPLCDriver().Send(sendFrame) == StdConstants.ERROR) {
                getEquipmentLogger().error("A problem occured while trying to send " + msgDescriptor + "...retrying to send");
                try {
                    // Waits 1 second
                    Thread.sleep(SEND_STARTUP_MESSAGE_FAILURE_WAIT_TIME);
                    sendAttempts++;
                } catch (InterruptedException ex) {
                    getEquipmentLogger().error("ERROR - while putting thread RUN asleep...");
                }
            } else {
                getEquipmentLogger().debug("Waiting ANSWER from PLC...");
                try {
                    successfullySent = receiveAndCheck(msgDescriptor);
                }
                catch (Exception ex) {
                    getEquipmentLogger().error("INITIALIZATION ERROR: Exception", ex);
                }
            }
        }
        if (!successfullySent)
          throw new IOException("Failed to send start-up message after " + (sendAttempts - 1) + " attempts - re-initializing connection thread.");
    }

    /**
     * This method is used to get the messages during initialization. If there's
     * a problem while trying to receive any of these messages, the reception is
     * retested until it's successful.
     * 
     * @param toReceive - JEC frame for data reception
     * @param typeOfMsg - Type of message to be received
     * @param msgDescriptor - String description of the type of message expected
     * @param watchDog - Reference time when watchdog was triggered
     * @return Returns the last sequence number of the receive frame.
     * @throws IOException Throws an exception if there is a problem with the
     * connection to the PLC.
     */
    private byte getStartupMessage(final JECPFrames toReceive, final byte typeOfMsg, 
            final String msgDescriptor, final long watchDog) throws IOException {
        // Variable used to check the status of each transmission
        int receptionStatus = StdConstants.ERROR;
        // Keeps a copy of the last received message sequence number;
        byte lastMsgSeqNumber = toReceive.GetSequenceNumber();
        // Retries reception until it receives the good message
        while (receptionStatus != StdConstants.SUCCESS) {
            // Tests the watchdog value
            if (watchDog != StdConstants.NO_WATCHDOG) {
                try {
                    watchDogTestOK(watchDog);
                    getEquipmentLogger().debug("Watchdog test: OK!");
                } catch (IOException ex) {
                    getEquipmentLogger().error("ERROR WHILE RECEIVING: Watchdog Timeout detected for message " + msgDescriptor);
                    throw new IOException("Watchdog timeout reached while receiving " + msgDescriptor);
                }
            }

            if (plcFactory.getPLCDriver().Receive(toReceive, StdConstants.TIMEOUT) == StdConstants.SUCCESS) {
                if (toReceive.getMsgID() == typeOfMsg) {
                    if (lastMsgSeqNumber != toReceive.GetSequenceNumber()) {
                        getEquipmentLogger().debug(msgDescriptor + " reception completed...sending acknowledge");
                        receptionStatus = StdConstants.SUCCESS;
                    } 
                    else {
                        getEquipmentLogger().warn("This message was already received!...DISCARDED!");
                    }
                }
                else {
                    getEquipmentLogger().warn("Received message is not: " + msgDescriptor);
                }
            }
            else {
                getEquipmentLogger().error("A problem occured while trying to receive " + msgDescriptor + "...retrying");
            }
        }
        return lastMsgSeqNumber;
    }

    /**
     * This method is used to test the watchdog timeout in case of no answer
     * from the PLC If the watchdog elapses, it raises an exception and restarts
     * the DAQ
     * 
     * @param watchDogRef - Reference time when the watchdog was started to work
     * @throws IOException Throws an exception if the provided reference time is to 
     * far away from the current time.
     */
    private void watchDogTestOK(final long watchDogRef) throws IOException {
        // Test the watchdog to see if the driver didn't block
        if ((System.currentTimeMillis() - watchDogRef) > StdConstants.watchdogTimeout) {
            getEquipmentLogger().error("Watchdog detected a timeout during INIT sequence");
            throw new IOException("INIT WATCHDOG: Timeout detected while Initializing PLC...restarting");
        }
    }
    
    /**
     * Shuts down everything of the DAQ. The DAQ is not supposed to be started after that.
     * 
     * @throws EqIOException Might throw an EqIOException
     */
    @Override
    public void shutdown() throws EqIOException {
        super.shutdown();
        jecController.stopFrameProcessing();
        jecCommandRunner.stopThread();
    }
}