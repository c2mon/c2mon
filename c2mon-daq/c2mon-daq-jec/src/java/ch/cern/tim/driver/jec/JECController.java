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
package ch.cern.tim.driver.jec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.EquipmentLoggerFactory;
import cern.tim.driver.common.IEquipmentMessageSender;
import cern.tim.shared.common.datatag.DataTagDeadband;
import cern.tim.shared.common.datatag.address.PLCHardwareAddress;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import ch.cern.tim.driver.jec.address.AnalogJECAddressSpace;
import ch.cern.tim.driver.jec.address.AnalogJECProfibusWagoAddressSpace;
import ch.cern.tim.driver.jec.address.BooleanJECAdressSpace;
import ch.cern.tim.driver.jec.address.BooleanJECProfibusWagoAddressSpace;
import ch.cern.tim.driver.jec.config.IJECTagConfigurationController;
import ch.cern.tim.driver.jec.frames.AbstractJECPFrameProcessor;
import ch.cern.tim.driver.jec.frames.AnalogDataProcessor;
import ch.cern.tim.driver.jec.frames.BooleanDataProcessor;
import ch.cern.tim.driver.jec.frames.InfoMessageProcessor;
import ch.cern.tim.driver.jec.frames.JECCommandRunner;
import ch.cern.tim.driver.jec.tools.JECBinaryHelper;
import ch.cern.tim.driver.jec.tools.JECConversionHelper;
import ch.cern.tim.jec.JECIndexOutOfRangeException;
import ch.cern.tim.jec.JECPFrames;
import ch.cern.tim.jec.StdConstants;

/**
 * JEC Controller to control tag configuration and processing of frames.
 * 
 * @author Andreas Lang
 *
 */
public class JECController implements IJECFrameController, IJECTagConfigurationController {
    /**
     * Binary deadband print mask.
     */
    private static final int BINARY_DEADBAND_PRINT_MASK = 0x7FFF;

    /**
     * Time to wait after an unexpected message came in.
     */
    public static final int UNEXPECTED_MESSAGE_WAIT_TIME = 1000;

    /**
     * The different possible states of the JECController.
     * 
     * @author Andreas Lang
     *
     */
    private static enum RUNNING_STATE {
        /**
         * @param NOT_STARTED State before the controller got start up.
         * @param STOPPED State after complete shutdown of the controller.
         * @param RUNNING State while the controller is running.
         * @param PAUSED Pause state of the controller.
         */
        NOT_STARTED, STOPPED, RUNNING, PAUSED
    }

    /**
     * Current running state of the DAQ.
     */
    private RUNNING_STATE runningState = RUNNING_STATE.NOT_STARTED;

    /**
     * The command runner which sends the command to the PLC and processes the answer frames.
     */
    private JECCommandRunner jecCommandRunner;
    /**
     * Processor for boolean data frames.
     */
    private BooleanDataProcessor<BooleanJECProfibusWagoAddressSpace> booleanDataProcessor;
    /**
     * Processor for boolean control frames.
     */
    private BooleanDataProcessor<BooleanJECAdressSpace> booleanDataControlProcessor;
    /**
     * Processor for analog data frames.
     */
    private AnalogDataProcessor<AnalogJECProfibusWagoAddressSpace> analogDataProcessor;
    /**
     * Processor for analog data control frames.
     */
    private AnalogDataProcessor<AnalogJECAddressSpace> analogDataControlProcessor;
    /**
     * Processor for info frames.
     */
    private InfoMessageProcessor infoMessageProcessor;
    /**
     * Helps to calculate and access details to the BooleanJECProfibusWagoAddressSpace.
     * Boolean command tags use this address space.
     */
    private BooleanJECProfibusWagoAddressSpace booleanCommandAddressSpace = new BooleanJECProfibusWagoAddressSpace();
    /**
     * Helps to calculate and access details to the AnalogJECProfibusWagoAddressSpace.
     * Analog command tags use this address space.
     */
    private AnalogJECProfibusWagoAddressSpace analogCommandAddressSpace = new AnalogJECProfibusWagoAddressSpace();
    /**
     * The equipment logger of this class.
     */
    private EquipmentLogger equipmentLogger;
    /**
     * The equipment message sender to send to the server.
     */
    private IEquipmentMessageSender equipmentMessageSender;
    /**
     * The PLC connection sampler to update the alive timer.
     */
    private PLCConnectionSampler connectionSampler;
    /**
     * Object factory to communicate with the PLC.
     */
    private PLCObjectFactory plcFactory;
    /**
     * Frame to acknowledge messages to the PLC.
     */
    private JECPFrames acknowledgeFrame;

    /**
     * Array to store the calculated raw deadband values to be sent during the
     * configuration. This array has the same size of the analog values array.
     */
    private byte[] anaRawDeadbandValues;

    /**
     * Analog data tags kept to configure the deadbands.
     */
    private final List<ISourceDataTag> analogDataTags = new LinkedList<ISourceDataTag>();

    /**
     * List of all frame processors.
     */
    private final List<AbstractJECPFrameProcessor> frameProcessorThreads = new ArrayList<AbstractJECPFrameProcessor>(6);

    /**
     * Creates a new JECController.
     * 
     * @param plcFactory The PLCObjectFactory used o communicate with the PLC
     * @param connectionSampler Connection sampler thread.
     * @param jecCommandRunner JecCommandRunner.
     * @param equipmentMessageSender The equipment message sender to send messages to
     * the server.
     * @param equipmentLoggerFactory The equipment logger to use.
     */
    public JECController(final PLCObjectFactory plcFactory,
            final PLCConnectionSampler connectionSampler, 
            final JECCommandRunner jecCommandRunner, 
            final IEquipmentMessageSender equipmentMessageSender,
            final EquipmentLoggerFactory equipmentLoggerFactory) {
        equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());
        booleanDataProcessor = new BooleanDataProcessor<BooleanJECProfibusWagoAddressSpace>(StdConstants.BOOL_DATA_MSG, new BooleanJECProfibusWagoAddressSpace(), plcFactory, equipmentMessageSender, equipmentLoggerFactory.getEquipmentLogger(BooleanDataProcessor.class));
        frameProcessorThreads.add(booleanDataProcessor);
        booleanDataControlProcessor = new BooleanDataProcessor<BooleanJECAdressSpace>(StdConstants.BOOL_DATA_CTRL_MSG, new BooleanJECAdressSpace(), plcFactory, equipmentMessageSender, equipmentLoggerFactory.getEquipmentLogger(BooleanDataProcessor.class));
        frameProcessorThreads.add(booleanDataControlProcessor);
        analogDataProcessor = new AnalogDataProcessor<AnalogJECProfibusWagoAddressSpace>(StdConstants.ANALOG_DATA_MSG, new AnalogJECProfibusWagoAddressSpace(), plcFactory, true, equipmentMessageSender, equipmentLoggerFactory.getEquipmentLogger(AnalogDataProcessor.class));
        frameProcessorThreads.add(analogDataProcessor);
        analogDataControlProcessor = new AnalogDataProcessor<AnalogJECAddressSpace>(StdConstants.ANA_DATA_CTRL_MSG, new AnalogJECAddressSpace(), plcFactory, false, equipmentMessageSender, equipmentLoggerFactory.getEquipmentLogger(AnalogDataProcessor.class));
        frameProcessorThreads.add(analogDataControlProcessor);
        infoMessageProcessor = new InfoMessageProcessor(plcFactory, StdConstants.INFO_MSG, this, equipmentLogger);
        frameProcessorThreads.add(infoMessageProcessor);
        this.jecCommandRunner = jecCommandRunner;
        frameProcessorThreads.add(jecCommandRunner);
        this.setConnectionSampler(connectionSampler);
        this.equipmentMessageSender = equipmentMessageSender;
        this.plcFactory = plcFactory;
        acknowledgeFrame = plcFactory.getSendFrame(StdConstants.ACK_MSG);
    }

    /**
     * Starts the frame processing threads.
     */
    @Override
    public synchronized void startFrameProcessing() {
        if (runningState == RUNNING_STATE.STOPPED) {
            throw new IllegalStateException("JECFrameController has bee stopped! Create a new one and start it in order to restart the controller.");
        }
        getEquipmentLogger().info("Starting JECController threads");
        for (AbstractJECPFrameProcessor frameProcessorThread : frameProcessorThreads) {
            if (!frameProcessorThread.isAlive()) {
                frameProcessorThread.start();
            }
        }
        runningState = RUNNING_STATE.RUNNING;
    }
 
    /**
     * Stops the frame processing. This will stop all related threads. And you will
     * not be able to restart them.
     */
    @Override
    public synchronized void stopFrameProcessing() {
        getEquipmentLogger().info("Stopping JECController threads");
        for (AbstractJECPFrameProcessor frameProcessorThread : frameProcessorThreads) {
            frameProcessorThread.stopThread();
        }
        runningState = RUNNING_STATE.STOPPED;
    }

    /**
     * Sets this controller to a pause state.
     * 
     * @param pause True if the frame processing should be paused else false.
     */
    @Override
    public synchronized void setPauseFrameProcessing(final boolean pause) {
        if (runningState == RUNNING_STATE.STOPPED) {
            throw new IllegalStateException("JECFrameController has bee stopped! Create a new one and start it in order to restart the controller.");
        }
        for (AbstractJECPFrameProcessor frameProcessorThread : frameProcessorThreads) {
            frameProcessorThread.setPause(pause);
        }
        if (pause) {
            runningState = RUNNING_STATE.PAUSED;
        } else {
            runningState = RUNNING_STATE.RUNNING;
        }
    }

    /**
     * Pushes a frame into this controller.
     * @param frame The frame to process.
     * @param processImmediately True if the frame should be processed immediately.
     * @return True if the frame was successfully processed else false.
     */
    private synchronized boolean pushFrame(final JECPFrames frame, 
            final boolean processImmediately) {
        boolean success;
        AbstractJECPFrameProcessor currentProcessor = null;
        switch (frame.getMsgID()) {
        case StdConstants.BOOL_DATA_MSG:
            currentProcessor = booleanDataProcessor;
            getEquipmentLogger().debug("BOOLEAN DATA MESSAGE RECEIVED");
            updateAliveTimer();
            break;
        case StdConstants.BOOL_DATA_CTRL_MSG:
            if (!booleanDataControlProcessor.getJecAddressSpace().isEmpty()) {
                currentProcessor = booleanDataControlProcessor;
                getEquipmentLogger().debug("BOOLEAN DATA CONTROL MESSAGE RECEIVED");
                updateAliveTimer();
            }
            /*
             * This is a small hack. The PLC will return a frame for this values
             * at startup even if there are no tags configured. In this case the 
             * frame should only be acknowledged and not processed. The processing
             * would fail because an empty processor has no array to store the
             * values.
             */
            else {
                try {
                    booleanDataControlProcessor.acknowledgeReceivedMessage(frame);
                } catch (IOException e) {
                    getEquipmentLogger().error("Error while acknowleding frame.");
                }
            }
            break;
        case StdConstants.BOOL_CMD_CTRL_MSG:
            if (!booleanDataControlProcessor.getJecAddressSpace().isEmpty()) {
//                currentProcessor = booleanDataControlProcessor;
                getEquipmentLogger().info("BOOLEAN COMMAND CONTROL MESSAGE RECEIVED");
                updateAliveTimer();
            }
            /*
             * This is a small hack. The PLC will return a frame for this values
             * at startup even if there are no tags configured. In this case the 
             * frame should only be acknowledged and not processed. The processing
             * would fail because an empty processor has no array to store the
             * values.
             */
            else {
                try {
                    booleanDataControlProcessor.acknowledgeReceivedMessage(frame);
                } catch (IOException e) {
                    getEquipmentLogger().error("Error while acknowleding frame.");
                }
            }
            break;
        case StdConstants.ANALOG_DATA_MSG:
            currentProcessor = analogDataProcessor;
            getEquipmentLogger().debug("ANALOG DATA MESSAGE RECEIVED");
            updateAliveTimer();
            break;
        case StdConstants.ANA_DATA_CTRL_MSG:
            if (!analogDataControlProcessor.getJecAddressSpace().isEmpty()) {
                currentProcessor = analogDataControlProcessor;
                getEquipmentLogger().debug("ANALOG DATA CONTROL MESSAGE RECEIVED");
                updateAliveTimer();
            }
            /*
             * This is a small hack. The PLC will return a frame for this values
             * at startup even if there are no tags configured. In this case the 
             * frame should only be acknowledged and not processed. The processing
             * would fail because an empty processor has no array to store the
             * values.
             */
            else {
                try {
                    analogDataControlProcessor.acknowledgeReceivedMessage(frame);
                } catch (IOException e) {
                    getEquipmentLogger().error("Error while acknowleding frame.");
                }
            }
            break;
        case StdConstants.HANDLER_ALIVE_MSG:
            updateAliveTimer();
            break;
        case StdConstants.INFO_MSG:
            currentProcessor = infoMessageProcessor;
            try {
                infoMessageProcessor.acknowledgeReceivedMessage(frame);
            } catch (IOException e) {
                getEquipmentLogger().error("Error while acknowleding frame.");
            }
            getEquipmentLogger().debug("INFO MESSAGE SET IN THE QUEUE");
            updateAliveTimer();
            break;
        case StdConstants.CONFIRM_BOOL_CMD_MSG:
        case StdConstants.CONFIRM_BOOL_CMD_CTRL_MSG:
        case StdConstants.CONFIRM_ANA_CMD_MSG:
            currentProcessor = jecCommandRunner;
            updateAliveTimer();
            break;
        default:
            getEquipmentLogger().debug("Unexpected message received - NOT A JEC MESSAGE! - Message code: " + frame.getMsgID());
            try {
                // Even if its garbage, acknowledges it
                acknowledgeReceivedMessage(acknowledgeFrame, frame);
                // Wait one second...
                Thread.sleep(UNEXPECTED_MESSAGE_WAIT_TIME);
            } catch (InterruptedException ex) {
                getEquipmentLogger().debug("ERROR - while putting thread RUN asleep");
            } catch (IOException ex) {
                getEquipmentLogger().debug("ERROR - while acknowledging unexpected message");
            }
        }
        if (currentProcessor != null) {
            success = currentProcessor.pushJECPFrame(frame);
            if (processImmediately) {
                success = currentProcessor.processNextJECPFrame();
            }
        } else {
            success = false;
        }
        return success;
    }

    /**
     * 
     */
    private void updateAliveTimer() {
        if (getConnectionSampler() != null)
            getConnectionSampler().updateAliveTimer();
    }

    /**
     * @param equipmentLogger
     *            the equipmentLogger to set
     */
    public void setEquipmentLogger(final EquipmentLogger equipmentLogger) {
        this.equipmentLogger = equipmentLogger;
    }

    /**
     * @return the equipmentLogger
     */
    public EquipmentLogger getEquipmentLogger() {
        return equipmentLogger;
    }

    /**
     * Configures a command tag. (Updates the address space)
     * 
     * @param sourceCommandTag The command tag to configure.
     */
    @Override
    public void configureCommandTag(final ISourceCommandTag sourceCommandTag) {
        final PLCHardwareAddress plcCmdTagAddress = (PLCHardwareAddress) sourceCommandTag.getHardwareAddress();
        switch (plcCmdTagAddress.getBlockType()) {
        case PLCHardwareAddress.STRUCT_BOOLEAN_COMMAND:
            getEquipmentLogger().debug("BOOL COMMAND Tag found - " + sourceCommandTag.getId());
            booleanCommandAddressSpace.updateAddressSpace(plcCmdTagAddress);
            break;
        case PLCHardwareAddress.STRUCT_DIAG_BOOLEAN_COMMAND:
            getEquipmentLogger().debug("BOOL COMMAND CONTROL Tag found - " + sourceCommandTag.getId());
            // TODO The HashMap was never read?
            // boolCmdControlTags.put(cmdTagKey, sct);
            break;
        case PLCHardwareAddress.STRUCT_ANALOG_COMMAND:
            getEquipmentLogger().debug("ANALOG COMMAND Tag found - " + sourceCommandTag.getId());
            analogCommandAddressSpace.updateAddressSpace(plcCmdTagAddress);
            break;
        default:
            getEquipmentLogger().warn("Unknown command tag type found! Command Tag: " + sourceCommandTag.getId() + ", block-type: " + plcCmdTagAddress.getBlockType());
            break;

        }
    }

    /**
     * Configures a data tag.
     * 
     * @param sourceDataTag The tag to configure.
     */
    @Override
    public void configureDataTag(final ISourceDataTag sourceDataTag) {
        PLCHardwareAddress plcTagAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
        String tagKey = getHashtableKey(plcTagAddress.getWordId(), plcTagAddress.getBitId());
        switch (plcTagAddress.getBlockType()) {
        case PLCHardwareAddress.STRUCT_BOOLEAN:
            getEquipmentLogger().debug("BOOLEAN Tag found - " + sourceDataTag.getId());
            if (!booleanDataProcessor.containsSourceDataTag(sourceDataTag)) {
                booleanDataProcessor.addSourceDataTag(sourceDataTag);
            } else {
                getEquipmentLogger().error("Boolean key " + tagKey + " already exists in hastable!");
                getEquipmentMessageSender().sendInvalidTag(sourceDataTag, SourceDataQuality.VALUE_CORRUPTED, "There is already a BOOL tag with same wordID and bitID");
            }
            break;
        case PLCHardwareAddress.STRUCT_DIAG_BOOLEAN:
            getEquipmentLogger().debug("BOOLEAN CONTROL Tag found - " + sourceDataTag.getId());
            if (!booleanDataControlProcessor.containsSourceDataTag(sourceDataTag)) {
                booleanDataControlProcessor.addSourceDataTag(sourceDataTag);
            } else {
                getEquipmentLogger().error("Boolean control key " + tagKey + " already exists in hastable!");
                getEquipmentMessageSender().sendInvalidTag(sourceDataTag, SourceDataQuality.VALUE_CORRUPTED, "There is already a BOOL control tag with same wordID and bitID");
            }
            break;

        case PLCHardwareAddress.STRUCT_ANALOG:
            getEquipmentLogger().debug("ANALOG Data Tag found - " + sourceDataTag.getId());
            if (!analogDataProcessor.containsSourceDataTag(sourceDataTag)) {
                analogDataProcessor.addSourceDataTag(sourceDataTag);
                analogDataTags.add(sourceDataTag);
            } else {
                getEquipmentLogger().error("Analog key " + tagKey + " already exists in hastable!");
                getEquipmentMessageSender().sendInvalidTag(sourceDataTag, SourceDataQuality.VALUE_CORRUPTED, "There is already a ANALOG tag with same wordID and bitID");
            }
            break;
        // If the DataTag is an analog control value
        case PLCHardwareAddress.STRUCT_DIAG_ANALOG:
            getEquipmentLogger().debug("ANALOG CONTROL Tag found - " + sourceDataTag.getId());
            if (!analogDataControlProcessor.containsSourceDataTag(sourceDataTag)) {
                analogDataControlProcessor.addSourceDataTag(sourceDataTag);
            } else {
                getEquipmentLogger().error("Analog control key " + tagKey + " already exists in hastable!");
                getEquipmentMessageSender().sendInvalidTag(sourceDataTag, SourceDataQuality.VALUE_CORRUPTED, "There is already an ANALOG control tag with same wordID and bitID");
            }
            break;
        default:
            getEquipmentLogger().debug("Unknown tag type found!...Sending Invalid Tag");
            getEquipmentMessageSender().sendInvalidTag(sourceDataTag, SourceDataQuality.UNSUPPORTED_TYPE, "Unknown tag type found");
            break;
        }
    }

    /**
     * This method is called after configuration it. Creates the arrays to save the
     * JECFrames to. (Depending on the size of the address space)
     */
    @Override
    public void initArrays() {
        booleanDataProcessor.initArrays();
        booleanDataControlProcessor.initArrays();
        analogDataProcessor.initArrays();
        analogDataControlProcessor.initArrays();
    }

    /**
     * Immediately processes the provided frame.
     * 
     * @param frame The frame to process.
     * @return True if the frame was successfully processed else false.
     */
    @Override
    public boolean processFrame(final JECPFrames frame) {
        return pushFrame(frame, true);
    }

    /**
     * Pushes the frame into the controller to process it when the thread comes
     * to its place in the queue.
     * 
     * @param frame The frame to push.
     */
    @Override
    public void pushFrame(final JECPFrames frame) {
        if (runningState == RUNNING_STATE.STOPPED) {
            throw new IllegalStateException("JECFrameController has bee stopped! Create a new one and start it in order to restart the controller.");
        }
        // Info messages should always be processed synchronously 
        if (frame.getMsgID() == StdConstants.INFO_MSG) {
            pushFrame(frame, true);
        }
        else {
            pushFrame(frame, false);
        }
    }

    /**
     * Clears the tag configuration.
     */
    @Override
    public void clearTagConfiguration() {
        analogDataTags.clear();
        booleanCommandAddressSpace.reset();
        analogCommandAddressSpace.reset();
        booleanDataProcessor.removeAllSupervisedTags();
        booleanDataControlProcessor.removeAllSupervisedTags();
        analogDataProcessor.removeAllSupervisedTags();
        analogDataControlProcessor.removeAllSupervisedTags();
    }

    /**
     * @param equipmentMessageSender
     *            the equipmentMessageSender to set
     */
    public void setEquipmentMessageSender(final IEquipmentMessageSender equipmentMessageSender) {
        this.equipmentMessageSender = equipmentMessageSender;
    }

    /**
     * @return the equipmentMessageSender
     */
    public IEquipmentMessageSender getEquipmentMessageSender() {
        return equipmentMessageSender;
    }

    /**
     * This method is used to invalidate tags associated with a DP slave ID. In
     * case of lost of a slave (WAGO), PLC will send an INFO message with the
     * slave ID and then, all the registered tags coming from this slave will be
     * invalidated.
     * 
     * @param slaveAddress
     *            - Native address of the slave
     */
    @Override
    public void invalidateSlaveTags(final String slaveAddress) {
        booleanDataProcessor.invalidateForUnavailableSlave(slaveAddress);
        booleanDataControlProcessor.invalidateForUnavailableSlave(slaveAddress);
        analogDataProcessor.invalidateForUnavailableSlave(slaveAddress);
        analogDataControlProcessor.invalidateForUnavailableSlave(slaveAddress);
    }

    /**
     * This method is used to revalidate tags associated with a DP slave ID. In
     * case of lost and the alive of a slave (WAGO, CAMAC, etc), PLC will send
     * an INFO message with the slave ID and then, all the registered tags
     * coming from this slave will be revalidated.
     * 
     * @param slaveAddress
     *            - Slave address identifier
     */
    @Override
    public void revalidateSlaveTags(final String slaveAddress) {
        booleanDataProcessor.revalidateForUnavailableSlave(slaveAddress);
        booleanDataControlProcessor.revalidateForUnavailableSlave(slaveAddress);
        analogDataProcessor.revalidateForUnavailableSlave(slaveAddress);
        analogDataControlProcessor.revalidateForUnavailableSlave(slaveAddress);
    }

    /**
     * This method is used to generate a standard key to access the Hashtable
     * used to map DataTags with WordNumber and BitNumber - Format: x#y ; x =
     * WordNumber; y = BitNumber - Eg. Wordnumber=134, BitNumber=4 -> returns
     * 134#4 as key
     * 
     * @param wrdNum
     *            - Word Id for the tag
     * @param bitNum
     *            - Bit Id for the tag
     * @return String - Returns a string with the wordId and bitId concatenation
     */
    private String getHashtableKey(final int wrdNum, final int bitNum) {
        return Integer.toString(wrdNum) + "#" + Integer.toString(bitNum);
    }

    /**
     * Returns the number of analog data JEC frames on the PLC.
     * 
     * @return The number of frames.
     */
    @Override
    public int getNumberOfAnalogDataJECFrames() {
        return (analogDataProcessor.getJecAddressSpace().getMaxWordIdPLC() * 2 / StdConstants.JEC_DATA_SIZE) + 1;
    }

    /**
     * Returns the number of boolean data JEC frames on the PLC.
     * 
     * @return The number of frames.
     */
    @Override
    public int getNumberOfBooleanDataJECFrames() {
        return (booleanDataProcessor.getJecAddressSpace().getMaxWordId() * 2 / StdConstants.JEC_DATA_SIZE) + 1;
    }

    /**
     * Returns the SetConfiguration message JECFrame which is sent at startup 
     * to the PLC.
     * 
     * @return The SetConfiguation JECFrame.
     */
    @Override
    public JECPFrames getSetConfigurationMessage() {
        int mMDBoolModules = booleanDataProcessor.getJecAddressSpace().getNumberOfMMDModules();
        int mMDAnalogModules = analogDataProcessor.getJecAddressSpace().getNumberOfMMDModules();
        int mMDBoolCommandModules = booleanCommandAddressSpace.getNumberOfMMDModules();
        int mMDAnalogCommandModules = analogCommandAddressSpace.getNumberOfMMDModules();
        JECPFrames sendFrame = plcFactory.getBasicSetConfigurationMessage(
                booleanDataProcessor.getJecAddressSpace().getMaxWordId(),
                analogDataProcessor.getJecAddressSpace().getMaxWordIdPLC(),
                mMDBoolModules,
                mMDAnalogModules,
                mMDBoolCommandModules,
                mMDAnalogCommandModules);
        return sendFrame;
    }

    /**
     * This method is used to acknowledge every message sent by the PLC. This
     * method was created to guarantee that all messages arrive to the driver.
     * 
     * @param recvMsg JEC received message
     * @param sendFrame JECFrame to use to send to the PLC.
     * @throws IOException Throws an IOException if the connection to the PLC fails.
     */
    public void acknowledgeReceivedMessage(final JECPFrames sendFrame, 
            final JECPFrames recvMsg) throws IOException {
        // Prepare a JEC frame to be identified as an ACKNOWLEDGE MESSAGE
        sendFrame.SetMessageIdentifier(StdConstants.ACK_MSG);
        // Sticks the received Sequence Number into the new acknowledge message
        sendFrame.SetSequenceNumber(recvMsg.GetSequenceNumber());
        // Sticks the received Message Identifier into the Data Type area
        sendFrame.SetDataType((byte) (recvMsg.getMsgID()));
        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("Acknowledging message from PLC: ");
            getEquipmentLogger().debug("\tRECEIVED FRAME SEQUENCE NUMBER: " + (" 0x" + Integer.toHexString((int) recvMsg.GetSequenceNumber())));
            getEquipmentLogger().debug("\tSENT FRAME SEQUENCE NUMBER: " + (" 0x" + Integer.toHexString((int) sendFrame.GetSequenceNumber())));
            getEquipmentLogger().debug("\tNUMBER OF RETRIES: " + (" 0x" + Integer.toHexString((int) recvMsg.GetRetryNumber())));
        }

        if (plcFactory.getPLCDriver().Send(sendFrame) == StdConstants.ERROR)
            throw new IOException("A problem occured while trying to send Acknowledge message");
    }

    /**
     * Configures the deadband for an analog data tag.
     * 
     * @param analogDataTag The analog data tag to add.
     */
    private void configureDeadband(final ISourceDataTag analogDataTag) {
        PLCHardwareAddress anaTagAddress = (PLCHardwareAddress) analogDataTag.getHardwareAddress();
        int wordID = anaTagAddress.getWordId();
        float physicalMaxValue = anaTagAddress.getPhysicMaxVal();
        float physicalMinValue = anaTagAddress.getPhysicalMinVal();
        int resolutionFactor = anaTagAddress.getResolutionFactor();
        float deadbandValue = analogDataTag.getValueDeadband();
        short filteringType = analogDataTag.getValueDeadbandType();
        if (JECConversionHelper.checkSHRTValues(resolutionFactor)) {
            if (resolutionFactor == 0) {
                getEquipmentLogger().debug("Value deadband found for tag " + analogDataTag.getId() + ", to be converted in IEEE bits: " + deadbandValue);
                if (filteringType == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE) {
                    int intBits = JECBinaryHelper.maskIEEEAbsoluteFilteringType(Float.floatToIntBits(deadbandValue));                    
                    JECBinaryHelper.putIEEEAnalogValueIntoArray(anaRawDeadbandValues, intBits, wordID);
                } else if (filteringType == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) {
                    int intBits = JECBinaryHelper.maskIEEERelativeFilteringType(Float.floatToIntBits(deadbandValue));                    
                    JECBinaryHelper.putIEEEAnalogValueIntoArray(anaRawDeadbandValues, intBits, wordID);
                } else
                    // TODO Is this the desired behavior? Process deadband?
                    getEquipmentLogger().error("Data Tag Discarded: Invalid Filtering Type - TAG ID: " + analogDataTag.getId());
            } else {
                getEquipmentLogger().debug("Value deadband found for tag " + analogDataTag.getId() + ": " + deadbandValue);
                if (filteringType == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE) {
                    short rawDeadbandValue = JECConversionHelper.convertDeadbandValueToRawDeadband(physicalMaxValue, physicalMinValue, deadbandValue, resolutionFactor);
                    rawDeadbandValue = JECBinaryHelper.maskAbsoluteFilteringType(rawDeadbandValue);                    
                    JECBinaryHelper.putAnalogValueIntoArray(anaRawDeadbandValues, rawDeadbandValue, wordID);
                } else if (filteringType == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) {
                    short hrfDeadBandValue = JECBinaryHelper.maskRelativeFilteringType((short) (deadbandValue * StdConstants.MANTISSA));                    
                    JECBinaryHelper.putAnalogValueIntoArray(anaRawDeadbandValues, hrfDeadBandValue, wordID);
                } else
                    // TODO Is this the desired behavior? Process deadband?
                    getEquipmentLogger().error("Data Tag Discarded: Invalid Filtering Type - TAG ID: " + analogDataTag.getId());
            }
        }
    }

    /**
     * Gets the deadband configuration JECFrame.
     * 
     * @param blockID The block id the frame is for.
     * @return The deadband configuration JECFrame.
     * @throws JECIndexOutOfRangeException This exception is thrown if the block id
     * would not point into the array of the JECFrame.
     */
    @Override
    public JECPFrames getDeadbandFrame(final short blockID) throws JECIndexOutOfRangeException {
        anaRawDeadbandValues = new byte[analogDataProcessor.getJecAddressSpace().getJavaByteArraySize()];
        for (ISourceDataTag analogDataTag : analogDataTags) {
          try {
            configureDeadband(analogDataTag);
          } catch (IllegalArgumentException e) {
            getEquipmentLogger().error("Exception caught while configuring deadband for PLC for tag " + analogDataTag.getId() + " - will not be correctly configured on PLC side.", e);
          }            
        }
        JECPFrames sendFrame = plcFactory.getRawSendFrame();
        sendFrame.UpdateMsgID(StdConstants.SET_CFG_MSG);
        sendFrame.SetDataType(StdConstants.ANA_DATA_DEADBAND);
        sendFrame.SetDataStartNumber(blockID);
        sendFrame.ResetDataBufferOffset();

        getEquipmentLogger().debug("Array size: " + anaRawDeadbandValues.length);
        getEquipmentLogger().debug("Block ID: " + blockID);

        // Write the current block deadbands inside a JEC frame
        sendFrame.AddJECData(anaRawDeadbandValues,
                blockID * StdConstants.JEC_DATA_SIZE, 
                StdConstants.JEC_DATA_SIZE); 
        return sendFrame;
    }

    /**
     * @param connectionSampler
     *            the connectionSampler to set
     */
    public void setConnectionSampler(final PLCConnectionSampler connectionSampler) {
        this.connectionSampler = connectionSampler;
    }

    /**
     * @return the connectionSampler
     */
    public PLCConnectionSampler getConnectionSampler() {
        return connectionSampler;
    }

    /**
     * Checks if the provided hardware address is in the currently configured 
     * address range.
     * 
     * @param hardwareAddress The hardware address to check.
     * @return True if the address is in range else false.
     */
    @Override
    public boolean isInAddressRange(final PLCHardwareAddress hardwareAddress) {
        boolean isInRange;
        int wordId = hardwareAddress.getWordId();
        int bitId = hardwareAddress.getBitId();
        String nativeAddress = hardwareAddress.getNativeAddress();
        switch (hardwareAddress.getBlockType()) {
        case PLCHardwareAddress.STRUCT_BOOLEAN:
            isInRange = booleanDataProcessor.getJecAddressSpace().isInRange(hardwareAddress);
            break;
        case PLCHardwareAddress.STRUCT_DIAG_BOOLEAN:
            isInRange = booleanDataControlProcessor.getJecAddressSpace().isInRange(nativeAddress, wordId, bitId);
            break;
        case PLCHardwareAddress.STRUCT_ANALOG:
            isInRange = analogDataProcessor.getJecAddressSpace().isInRange(nativeAddress, wordId);
            break;
        case PLCHardwareAddress.STRUCT_DIAG_ANALOG:
            isInRange = analogDataControlProcessor.getJecAddressSpace().isInRange(nativeAddress, wordId);
            break;
        case PLCHardwareAddress.STRUCT_ANALOG_COMMAND:
            isInRange = analogCommandAddressSpace.isInRange(nativeAddress, wordId);
            break;
        case PLCHardwareAddress.STRUCT_BOOLEAN_COMMAND:
            isInRange = booleanCommandAddressSpace.isInRange(nativeAddress, wordId, bitId);
            break;
        case PLCHardwareAddress.STRUCT_DIAG_BOOLEAN_COMMAND:
            // TODO does this type actually exist?
        default:
            isInRange = true;
            break;
        }
        return isInRange;
    }

    /**
     * Removes a SourceCommandTag from the configuration.
     * 
     * @param sourceCommandTag The command tag to remove. 
     */
    @Override
    public void removeCommandTag(final ISourceCommandTag sourceCommandTag) {
        // Nothing to do command runner accesses the equipment configuration
    }

    /**
     * Removes a data tag from the configuration.
     * 
     * @param sourceDataTag The source data tag to remove.
     */
    @Override
    public void removeDataTag(final ISourceDataTag sourceDataTag) {
        PLCHardwareAddress hardwareAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
        switch (hardwareAddress.getBlockType()) {
        case PLCHardwareAddress.STRUCT_BOOLEAN:
            booleanDataProcessor.removeSourceDataTag(sourceDataTag);
            break;
        case PLCHardwareAddress.STRUCT_DIAG_BOOLEAN:
            booleanDataControlProcessor.removeSourceDataTag(sourceDataTag);
            break;
        case PLCHardwareAddress.STRUCT_ANALOG:
            analogDataProcessor.removeSourceDataTag(sourceDataTag);
            analogDataTags.remove(sourceDataTag);
            break;
        case PLCHardwareAddress.STRUCT_DIAG_ANALOG:
            analogDataControlProcessor.removeSourceDataTag(sourceDataTag);
            break;
        default:
            // nothing to do
            break;
        }
    }
}
