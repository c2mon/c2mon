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

import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.jec.IJECRestarter;
import cern.c2mon.daq.jec.PLCConnectionSampler;

public class PLCConnectionSamplerTest {

    private static final long SAMPLER_PERIOD = 100;
    private PLCConnectionSampler plcConnectionSampler;
    private IJECRestarter jecRestarter = createMock(IJECRestarter.class);
    
    @Before
    public void setUp() {
        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
        plcConnectionSampler = new PLCConnectionSampler(jecRestarter, equipmentLogger, SAMPLER_PERIOD);
    }
    
    @Test
    public void testConectionLost() throws InterruptedException {
        plcConnectionSampler.start();
        plcConnectionSampler.updateAliveTimer();
        jecRestarter.forceImmediateRestart();
        
        replay(jecRestarter);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(50);
            plcConnectionSampler.updateAliveTimer();
        }
        // no call to jecRestarter should have happened.
        Thread.sleep(300); // sleep to provoke call.
        verify(jecRestarter);
    }
}
