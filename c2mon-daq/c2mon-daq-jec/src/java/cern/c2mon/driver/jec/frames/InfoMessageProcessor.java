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
package cern.c2mon.driver.jec.frames;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.driver.jec.IJECFrameController;
import cern.c2mon.driver.jec.PLCObjectFactory;
import cern.c2mon.driver.jec.plc.JECPFrames;
import cern.c2mon.driver.jec.plc.StdConstants;

/**
 * This class is used to process info frames.
 */
public class InfoMessageProcessor extends AbstractJECPFrameProcessor {
    /**
     * JECFrame controller to validate and invalidate tags.
     */
    private IJECFrameController frameController;
    
    /**
     * Creates a new InfoMessageProcessor.
     * 
     * @param plcFactory The PLC object factory to create objects to communicate 
     * with the PLC.
     * @param supervisedMessagesId The message id supervised by this processor.
     * @param frameController The frame controller for validation and invalidation.
     * @param equipmentLogger The logger of this class.
     */
    public InfoMessageProcessor(final PLCObjectFactory plcFactory, final int supervisedMessagesId, 
            final IJECFrameController frameController, final EquipmentLogger equipmentLogger) {
        super(supervisedMessagesId, plcFactory, equipmentLogger);
        this.frameController = frameController;
    }

    /**
     * Processes the provided infoFrame.
     * 
     * @param infoFrame The frame to process.
     */
    @Override
    public void processJECPFrame(final JECPFrames infoFrame) {
        try {
            getEquipmentLogger().debug("INFO DATA BUFFER IS RUNNING...");
            getEquipmentLogger().debug("INFO MESSAGE RECEIVED");
            String slaveAddress = null;
            switch (infoFrame.GetDataType()) {
            // If it's a slave lost message (0x01)
            case StdConstants.DP_SLAVE_LOST:
                slaveAddress = infoFrame.GetJECString(0);
                if (!(slaveAddress.equalsIgnoreCase("ERROR"))) {
                    if (infoFrame.GetDataStartNumber() == StdConstants.SLAVE_VALIDATE) {
                        getEquipmentLogger().debug("Revalidation - Slave LOST: " + slaveAddress);
                        frameController.revalidateSlaveTags(slaveAddress);
                    }
                    else if (infoFrame.GetDataStartNumber() == StdConstants.SLAVE_INVALIDATE) {
                        getEquipmentLogger().debug("Invalidation - Slave LOST: " + slaveAddress);
                        frameController.invalidateSlaveTags(slaveAddress);
                    } 
                    else {
                        getEquipmentLogger().debug("ERROR: Invalid VALIDATE/INVALIDATE INFO code in 'Data Start Number'");
                        getEquipmentLogger().debug("Received value: " + infoFrame.GetDataStartNumber() + " (in HEX):" + Integer.toHexString(infoFrame.GetDataStartNumber()));
                        throw new Exception("Corrupted INFO message received - wrong Data Start Number");
                    }
                } else {
                    getEquipmentLogger().debug("ERROR while extracting INFO string");
                }
                break;
                // INSERT HERE OTHER INFO MESSAGE TYPES
            default:
                getEquipmentLogger().error("INVALID INFO message - unknown data type");
                break;
            }
        } catch (Exception e) {
            getEquipmentLogger().error("run() : unexpected Exception", e);
        }
    }
}
