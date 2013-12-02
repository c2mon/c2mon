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
package cern.c2mon.driver.jec;

import java.io.IOException;

import cern.c2mon.driver.jec.plc.JECIndexOutOfRangeException;
import cern.c2mon.driver.jec.plc.JECPFrames;

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
     */
    void invalidateSlaveTags(final String slaveAddress);
    
    /**
     * Validates all tags with this slave address.
     * 
     * @param slaveAddress The address to validate.
     */
    void revalidateSlaveTags(final String slaveAddress);
    
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
