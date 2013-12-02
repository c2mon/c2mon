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

import java.util.Vector;

import cern.c2mon.driver.jec.config.PLCConfiguration;
import cern.c2mon.driver.jec.plc.ConnectionData;
import cern.c2mon.driver.jec.plc.JECPFrames;
import cern.c2mon.driver.jec.plc.PLCDriver;
import cern.c2mon.driver.jec.plc.StdConstants;

/**
 * The PLC object factory allows to create objects for the communication with the PLC
 * matching the PLCConfiguration.
 * 
 * @author Andreas Lang
 *
 */
public class PLCObjectFactory {
    /**
     * The PLCConfiguration containing connection settings.
     */
    private PLCConfiguration plcConfiguration;
    /**
     * The PLCDriver.
     */
    private final PLCDriver plcDriver;
    
    /**
     * Creates a new PLCObjectFactory.
     * 
     * @param plcConfiguration The configuration to use.
     * 
     * @throws InstantiationException Thrown if there is a problem instantiating the driver class.
     * @throws IllegalAccessException Thrown if there is an access problem instantiating the driver class.
     * @throws ClassNotFoundException Thrown if the driver class could not be found.
     */
    public PLCObjectFactory(final PLCConfiguration plcConfiguration) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.plcConfiguration = plcConfiguration;
        plcDriver = (PLCDriver) Class.forName(PLCDriver.class.getPackage().getName() + "." + plcConfiguration.getProtocol()).newInstance();
    }
    
    /**
     * Returns the PLCDriver.
     * 
     * @return The PLCDriver.
     */
    public PLCDriver getPLCDriver() {
        return plcDriver;
    }
    
    /**
     * Returns an empty JECFrames with the provided message type.
     * It will per call create a new frame.
     * 
     * @return The new JECPFrame.
     */
    public JECPFrames getRawRecvFrame() {
        String protocol = plcConfiguration.getProtocol();
        JECPFrames jecpFrames;
        if (protocol.equalsIgnoreCase("SiemensISO") || protocol.equalsIgnoreCase("SiemensTCP")) {
            jecpFrames = new JECPFrames((byte) 0, StdConstants.SIEMENS_OFFSET);
        }
        else {
            jecpFrames = new JECPFrames((byte) 0, 0);
        }
        return jecpFrames;
    }
    
    /**
     * Returns a new send frame with the provided message type.
     * 
     * @param msgType The message type to use.
     * @return The new created JECFrames.
     */
    public JECPFrames getSendFrame(final byte msgType) {
        String protocol = plcConfiguration.getProtocol();
        JECPFrames frame;
        if (protocol .equalsIgnoreCase("SiemensISO") || protocol.equalsIgnoreCase("SiemensTCP")) {
            frame = new JECPFrames(msgType, StdConstants.SIEMENS_OFFSET);
        }
        else {
            frame = new JECPFrames(msgType, StdConstants.SCHNEIDER_OFFSET);
        }
        return frame;
    }
    
    /**
     * Gets a raw send frame which means the message type is just set
     * to a default and has to be reset yourself.
     * 
     * @return The new created frame.
     */
    public JECPFrames getRawSendFrame() {
        return getSendFrame((byte) 0);
    }

    /**
     * Creates the connection data object according to the PLCConfiguration.
     * 
     * @param plcName The name of the PLC the connection data should be for.
     * @return The connection data object.
     */
    public ConnectionData createConnectionData(final String plcName) {
        ConnectionData connectionData;
        if (plcConfiguration.getProtocol().equalsIgnoreCase("SiemensISO")) {
            connectionData = new ConnectionData(
                    plcName, plcConfiguration.getPort(), plcConfiguration.getsTsap(), 
                    plcConfiguration.getdTsap(), true);
        }
        else {
            connectionData = new ConnectionData(plcName, plcConfiguration.getPort());
        }
        return connectionData;
    }
    
    /**
     * Creates a new configuration message frame. You only have to set the sequence number.
     * 
     * @param booleanDataLength The boolean data length.
     * @param analogDataLength The analog data length.
     * @param mMDBoolModules The number of MMD boolean modules.
     * @param mMDAnalogModules The number of MMD analog modules.
     * @param mMDBoolCommandModules  The number of MMD boolean command modules.
     * @param mMDAnalogCommandModules The number of MMD analog command modules.
     * @return The created set configuration message.
     */
    public JECPFrames getBasicSetConfigurationMessage(final int booleanDataLength,
            final int analogDataLength, final int mMDBoolModules, 
            final int mMDAnalogModules, final int mMDBoolCommandModules, 
            final int mMDAnalogCommandModules) {
        Vector<Byte> dpSlaveAddress = plcConfiguration.getDpSlaveAddresses();
        JECPFrames sendFrame = getSendFrame(StdConstants.SET_CFG_MSG);
        sendFrame.UpdateMsgID(StdConstants.SET_CFG_MSG);
        sendFrame.SetDataType(StdConstants.PLC_CONF_DATA);
        sendFrame.SetPLCConfig(
                booleanDataLength,
                analogDataLength,
                mMDBoolModules,
                mMDAnalogModules,
                mMDBoolCommandModules,
                mMDAnalogCommandModules,
                dpSlaveAddress.size(), 
                dpSlaveAddress);
        return sendFrame;
    }

}
