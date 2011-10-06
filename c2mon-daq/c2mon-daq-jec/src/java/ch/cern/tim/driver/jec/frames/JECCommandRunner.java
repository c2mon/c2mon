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
package ch.cern.tim.driver.jec.frames;

import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.ICommandRunner;
import cern.tim.driver.common.conf.equipment.IEquipmentConfiguration;
import cern.tim.driver.tools.equipmentexceptions.EqCommandTagException;
import cern.tim.shared.common.datatag.address.PLCHardwareAddress;
import cern.tim.shared.common.type.TagDataType;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import ch.cern.tim.driver.jec.PLCObjectFactory;
import ch.cern.tim.driver.jec.tools.JECConversionHelper;
import ch.cern.tim.jec.JECIndexOutOfRangeException;
import ch.cern.tim.jec.JECPFrames;
import ch.cern.tim.jec.StdConstants;

/**
 * The command runner sends commands to the PLC and processes the answers to the command.
 * 
 * @author Andreas Lang
 *
 */
public class JECCommandRunner extends AbstractJECPFrameProcessor implements ICommandRunner {
    /**
     * The current received frame.
     */
    private JECPFrames currentReceivedFrame;
    
    /**
     * The monitor object for synchronizing sending command and report receiving
     */
    private Object syncCommandReportMonitor = new Object();
    
    /**
     * This variable is used to keep the last boolean JEC command message
     * sequence number. Is used to avoid repeated messages to be processed
     */
    private byte cmdSeqNumber = 0x01;
    
    /**
     * The equipment configuration of this handler.
     */
    private IEquipmentConfiguration equipmentConfiguration;

    /**
     * Creates a new JECCommandRunner.
     * 
     * @param equipmentLogger The equipment logger to use.
     * @param plcFactory The PLC object factory to create send frames and access the
     * driver.
     * @param equipmentConfiguration The equipment configuration for the handler.
     */
    public JECCommandRunner(final EquipmentLogger equipmentLogger,
            final PLCObjectFactory plcFactory,
            final IEquipmentConfiguration equipmentConfiguration) {
        super(-1, plcFactory, equipmentLogger);
        this.equipmentConfiguration = equipmentConfiguration;
    }

    /**
     * Processes the provided JECPFrame. It should match to the previous sent command.
     * 
     * @param jecpFrames The frame to process.
     */
    @Override
    public void processJECPFrame(final JECPFrames jecpFrames) {
        currentReceivedFrame = jecpFrames;
        nextSequenceNumber();
        synchronized (syncCommandReportMonitor) {
            syncCommandReportMonitor.notify();
        }
    }

    /**
     * Increases the sequence number.
     */
    private void nextSequenceNumber() {
        if (cmdSeqNumber == Byte.MAX_VALUE) {
            cmdSeqNumber = Byte.MIN_VALUE;
        }
        else {
            cmdSeqNumber++;
        }
    }

    /**
     * Runs the command matching the source command tag value.
     * 
     * @param sourceCommandTagValue Value to identify the command to run.
     * @throws EqCommandTagException Throws an exception if the execution of the command fails.
     * @return Returns a message about the execution of the command or null if there is no need.
     */
    @Override
    public String runCommand(final SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
        getEquipmentLogger().debug("Entering sendCommand...");
        if (isPause()) 
            throw new EqCommandTagException("Cannot send while JECCommandRunner is paused. The reason for the pause is may be a connection loss/restart.");
        ISourceCommandTag sourceCommandTag = getSourceCommandTag(sourceCommandTagValue);
        PLCHardwareAddress cmdTagAddress = (PLCHardwareAddress) sourceCommandTag.getHardwareAddress();
        JECPFrames commandFrame = getPlcFactory().getRawSendFrame();
        commandFrame.SetDataStartNumber((short) cmdTagAddress.getWordId());
        commandFrame.SetSequenceNumber(cmdSeqNumber);
        switch (sourceCommandTagValue.getDataTypeNumeric()) {
        case TagDataType.TYPE_BOOLEAN:
            prepareBooleanCommandFrame(commandFrame, sourceCommandTagValue, cmdTagAddress);
            break;
        case TagDataType.TYPE_INTEGER:
        case TagDataType.TYPE_FLOAT:
            prepareAnalogCommandFrame(commandFrame, sourceCommandTagValue, cmdTagAddress);
            break;
        default:
            throw new EqCommandTagException("Invalid Data Type for this command tag");
        }
        getEquipmentLogger().debug("Putting sendCommand thread in WAIT mode. Waiting Command Confirm...");
        int commandReportCode = sendAndWaitForCommandReply(sourceCommandTag, commandFrame);
        getEquipmentLogger().debug("Reading status report for Analog Command...");
        checkCommandReportCode(commandReportCode);
        getEquipmentLogger().debug("Exiting sendCommand...");
        return null; // No message needed
    }

    /**
     * Method prepares an analog command frame. It fills the provided command frame with the value from the
     * source command tag value.
     * 
     * @param sourceCommandTagValue The value to send in the command.
     * @param analogCommandFrame The frame to fill.
     * @param cmdTagAddress The PLCHardwareAddress to know how to prepare
     * the source command tag value.
     * @throws EqCommandTagException Throws an exception if the command
     * processing fails.
     */
    private void prepareAnalogCommandFrame(final JECPFrames analogCommandFrame, 
            final SourceCommandTagValue sourceCommandTagValue, 
            final PLCHardwareAddress cmdTagAddress) throws EqCommandTagException {
        analogCommandFrame.SetMessageIdentifier(StdConstants.ANALOG_CMD_MSG);
        analogCommandFrame.SetDataType(StdConstants.ANA_CMD_VALUE);
        float anaHRValue = ((Number) sourceCommandTagValue.getValue()).floatValue();
        getEquipmentLogger().debug("Float value sent to PLC: " + anaHRValue);
        if (JECConversionHelper.checkSHRTValues(cmdTagAddress.getResolutionFactor())) {
            // If resolution factor is 0, its a IEEE Float. Otherwise is Raw Data
            if (cmdTagAddress.getResolutionFactor() == 0) {
                analogCommandFrame.SetDataOffset((short) StdConstants.FLOAT_DATA);
                int anaIEEEValue = Float.floatToIntBits(anaHRValue);
                try {
                    // Put Command Value in the first JEC data word (0)
                    analogCommandFrame.AddJECData(anaIEEEValue, 0);
                } catch (JECIndexOutOfRangeException ex) {
                    getEquipmentLogger().error("SEND CMD: Error while trying to put IEEE command value in JEC frame: " + ex);
                    throw new EqCommandTagException("SEND CMD: Error while trying to put IEEE command value in JEC frame");
                }
                getEquipmentLogger().debug("ANALOG IEEE Command value sent: " + anaIEEEValue);
            }
            else {
                analogCommandFrame.SetDataOffset((short) StdConstants.RAW_DATA);
                short anaRawValue = (short) JECConversionHelper.convertJavaToPLCValue(anaHRValue, cmdTagAddress);
                try {
                    // Put Command Value in the first JEC data word (0)
                    analogCommandFrame.AddJECData(anaRawValue, 0);
                } catch (JECIndexOutOfRangeException ex) {
                    getEquipmentLogger().error("SEND CMD: Error while trying to put command value in JEC frame: " + ex);
                    throw new EqCommandTagException("SEND CMD: Error while trying to put command value in JEC frame");
                }
                getEquipmentLogger().debug("ANALOG Command value sent: " + anaRawValue);
            }
        } else
            getEquipmentLogger().error("CMD ERROR while updating SHRT Values from table: Invalid resolution factor");
    }

    /**
     * Method prepares an boolean command frame. It fills the provided command frame with the value from the
     * source command tag value.
     * 
     * @param sourceCommandTagValue The value to send in the command.
     * @param booleanCommandFrame The frame to fill.
     * @param cmdTagAddress The PLCHardwareAddress to know how to prepare
     * the source command tag value.
     * @throws EqCommandTagException Throws an exception if the command
     * processing fails.
     */
    private void prepareBooleanCommandFrame(final JECPFrames booleanCommandFrame, final SourceCommandTagValue sourceCommandTagValue,
            final PLCHardwareAddress cmdTagAddress) throws EqCommandTagException {
        booleanCommandFrame.SetMessageIdentifier(StdConstants.BOOL_CMD_MSG);
        booleanCommandFrame.SetDataType(StdConstants.BOOL_CMD_VALUE);
        booleanCommandFrame.SetDataOffset((short) cmdTagAddress.getBitId());
        short boolValue;
        if (sourceCommandTagValue.getValue().equals(Boolean.FALSE)) {
            boolValue = 0x00;
        }
        else {
            boolValue = 0x01;
        }
        getEquipmentLogger().debug("Boolean Command value sent to PLC: " + boolValue);

        try {
            booleanCommandFrame.AddJECData(boolValue, 0);
            // Put Command Pulse Length in the second JEC data word (1)
            booleanCommandFrame.AddJECData((short) cmdTagAddress.getCommandPulseLength(), 1);
        } catch (JECIndexOutOfRangeException ex) {
            getEquipmentLogger().error("SEND CMD: Error while trying to put command value in JEC frame: " + ex);
            throw new EqCommandTagException("SEND CMD: Error while trying to put command value in JEC frame");
        }
    }

    /**
     * Performs required checks and returns the source command tag to this source command tag
     * value.
     * 
     * @param sourceCommandTagValue The source command tag value to get the command tag.
     * @return The desired source command tag.
     * @throws EqCommandTagException Throws an exception if the command runner is in the wrong
     * state, the source command tag does not exist or the source command tag has invalid values.
     */
    private ISourceCommandTag getSourceCommandTag(final SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
        ISourceCommandTag sourceCommandTag = equipmentConfiguration.getSourceCommandTags().get(sourceCommandTagValue.getId());
        if (sourceCommandTag == null) {
            throw new EqCommandTagException("CommandTag with id " + sourceCommandTagValue.getId() + "not found in equipment configuration");
        }
        if (sourceCommandTag.getSourceTimeout() < StdConstants.minCmdTimeout)
            throw new EqCommandTagException("SEND CMD: Invalid timeout value received! (min.: " + StdConstants.minCmdTimeout + " ms)");
        return sourceCommandTag;
    }
    
    /**
     * Sends the command to the PLC and waits for the reply.
     * 
     * @param sourceCommandTag The source command tag matching the command to execute.
     * @param sendFrame The frame to send frame.
     * @return The command report code of the answer.
     * @throws EqCommandTagException Throws an exception if the execution fails.
     */
    private int sendAndWaitForCommandReply(final ISourceCommandTag sourceCommandTag, 
            final JECPFrames sendFrame) throws EqCommandTagException {
        byte commandReportCode;
        synchronized (syncCommandReportMonitor) {
            if (getPlcFactory().getPLCDriver().Send(sendFrame) == StdConstants.ERROR)
                throw new EqCommandTagException("A problem occured while trying to send BOOLEAN COMMAND msg");
            // Tries to put thread in WAIT mode
            try {
                syncCommandReportMonitor.wait(sourceCommandTag.getSourceTimeout());
            } catch (InterruptedException ex) {
                getEquipmentLogger().error("ERROR while trying to put Command Report thread asleep: " + ex);
                throw new EqCommandTagException("A problem occured while trying to put thread asleep");
            }
            commandReportCode = currentReceivedFrame.GetJECByte(1);
        }
        return commandReportCode;
    }

    /**
     * Checks the report code, logs the result and throws an 
     * EquipmentCommandTagException if the command failed.
     * 
     * @param commandReportCode The code to check
     * @throws EqCommandTagException This exception is thrown if the execution
     * of the command failed.
     */
    private void checkCommandReportCode(final int commandReportCode) throws EqCommandTagException {
        switch (commandReportCode) {
        // If its an acknowledge, is OK
        case StdConstants.ACK_MSG:
            getEquipmentLogger().debug("Command Report value received from PLC: SUCCESS");
            break;
        // If its an non acknowledge, is ERROR
        case StdConstants.NACK_MSG:
            getEquipmentLogger().error("Command Report value received from PLC: ERROR");
            throw new EqCommandTagException("Command Report value received from PLC: ERROR");
        // If its something else, is INVALID
        default:
            String errorMessage = "Invalid Command Report value received from PLC: " + commandReportCode;
            getEquipmentLogger().error(errorMessage);
            throw new EqCommandTagException(errorMessage);
        }
    }
    
    /**
     * Checks if the message id of the frame matches to a command message id.
     * 
     * @param jecpFrame The frame to check the message id from.
     * @return True if the message id is correct else false.
     */
    @Override
    public boolean isCorrectMessageId(final JECPFrames jecpFrame) {
        int msgId = jecpFrame.getMsgID();
        return StdConstants.CONFIRM_ANA_CMD_MSG == msgId 
        || StdConstants.CONFIRM_BOOL_CMD_MSG == msgId
        || StdConstants.CONFIRM_BOOL_CMD_CTRL_MSG == msgId;
    }
    
    /**
     * Checks if the jecpFrames sequence number matches the one of the last sent
     * command.
     * 
     * @param jecpFrame The frame to check.
     * @return Returns true if the frame's sequence number is correct else false.
     */
    @Override
    public boolean isInSequence(final JECPFrames jecpFrame) {
        return jecpFrame.GetSequenceNumber() == cmdSeqNumber;
    }

}
