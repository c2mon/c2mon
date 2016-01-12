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
package cern.c2mon.daq.jec.frames;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.jec.IJECFrameController;
import cern.c2mon.daq.jec.PLCObjectFactory;
import cern.c2mon.daq.jec.plc.JECPFrames;
import cern.c2mon.daq.jec.plc.StdConstants;

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
                        frameController.revalidateSlaveTags(slaveAddress, infoFrame.GetJECCurrTimeMilliseconds());
                    }
                    else if (infoFrame.GetDataStartNumber() == StdConstants.SLAVE_INVALIDATE) {
                        getEquipmentLogger().debug("Invalidation - Slave LOST: " + slaveAddress);
                        frameController.invalidateSlaveTags(slaveAddress, infoFrame.GetJECCurrTimeMilliseconds());
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
