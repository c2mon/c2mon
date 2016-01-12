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
package cern.c2mon.daq.jec.plc;

import cern.c2mon.daq.jec.plc.ConnectionData;
import cern.c2mon.daq.jec.plc.JECPFrames;
import cern.c2mon.daq.jec.plc.PLCDriver;

public class TestPLCDriver implements PLCDriver {
    
    private JECPFrames lastSend;
    
    @Override
    public int Connect(ConnectionData param) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int Disconnect(ConnectionData param) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int Receive(JECPFrames buffer) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int Receive(JECPFrames buffer, int timeout) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int Send(JECPFrames Frame) {
        setLastSend(Frame);
        return 0;
    }

    /**
     * @param lastSend the lastSend to set
     */
    public void setLastSend(JECPFrames lastSend) {
        this.lastSend = lastSend;
    }

    /**
     * @return the lastSend
     */
    public JECPFrames getLastSend() {
        return lastSend;
    }

}
