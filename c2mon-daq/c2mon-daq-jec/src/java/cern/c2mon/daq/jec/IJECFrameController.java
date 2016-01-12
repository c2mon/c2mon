/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.jec;

import java.io.IOException;

import cern.c2mon.daq.jec.plc.JECIndexOutOfRangeException;
import cern.c2mon.daq.jec.plc.JECPFrames;

/**
 * The frame controller defines the methods necessary to process frames.
 * 
 * @author Andreas Lang
 *
 */
public interface IJECFrameController {

    /**
     * Starts the processing of JECFrames which are pushed into the controller.
     */
    void startFrameProcessing();

    /**
     * Stops the processing of JECFrames which are pushed into the controller.
     * There is no restart possible.
     */
    void stopFrameProcessing();

    /**
     * Sets the pausing of the controller to pause.
     * 
     * @param pause The new pause state of the controller.
     */
    void setPauseFrameProcessing(final boolean pause);
    
    /**
     * Pushes the frame into the controller. Queuing it for processing.
     * 
     * @param frame The frame to be queued.
     */
    void pushFrame(JECPFrames frame);
    
    /**
     * Processes this frame without queuing it.
     * 
     * @param frame The frame to process.
     * @return True if the frame was processed else false.
     */
    boolean processFrame(JECPFrames frame);
    
    /**
     * Initializes the arrays. This method should be called after all
     * tags are configured and before the processing starts.
     */
    void initArrays();
    
    /**
     * Invalidates all tags with this slave address.
     * 
     * @param slaveAddress The address to invalidate.
     * @param sourceTimestamp The timestamp when the request arrived.
     */
    void invalidateSlaveTags(final String slaveAddress, final long sourceTimestamp);
    
    /**
     * Validates all tags with this slave address.
     * 
     * @param slaveAddress The address to validate.
     * @param sourceTimestamp The timestamp when the request arrived.
     */
    void revalidateSlaveTags(final String slaveAddress, final long sourceTimestamp);
    
    /**
     * Returns the number of boolean data JEC frames.
     * 
     * @return The number of frames.
     */
    int getNumberOfBooleanDataJECFrames();
    
    /**
     * Returns the number of analog data JEC frames.
     * 
     * @return The number of frames.
     */
    int getNumberOfAnalogDataJECFrames();
    
    /**
     * Returns a valid JECP set configuration frame to send to the PLC.
     * 
     * @return The SetConfiguration frame.
     */
    JECPFrames getSetConfigurationMessage();
    
    /**
     * Returns a valid JECP SetDeadbands frame to send to the PLC.
     * 
     * @param blockId The position of the block the frame should configure.
     * @return The SetDeadbands frame.
     * @throws JECIndexOutOfRangeException Thrown if the block id is not in the frame range.
     */
    JECPFrames getDeadbandFrame(final short blockId) throws JECIndexOutOfRangeException;
    
    /**
     * Acknowledges a received message.
     * 
     * @param sendFrame The send frame to use to send the acknowledge to the PLC.
     * @param recvMsg The frame to put the received message in.
     * @throws IOException Thrown if the connection to the PLC fails.
     */
    void acknowledgeReceivedMessage(JECPFrames sendFrame, JECPFrames recvMsg) throws IOException;

}
