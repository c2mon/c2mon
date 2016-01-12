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

import org.junit.Before;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.*;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.jec.TimedJECRestarter;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;

public class TimedJECRestarterTest {
    
    private TimedJECRestarter timedJECRestarter;
    private EquipmentMessageHandler messageHandler = createMock(EquipmentMessageHandler.class);
    
    @Before
    public void setUp() {
        timedJECRestarter = new TimedJECRestarter(messageHandler, 100L, 100L);
    }
    
    @Test
    public void testForceImmediateRestart() throws EqIOException, InterruptedException {
        messageHandler.disconnectFromDataSource();
        messageHandler.connectToDataSource();
        
        replay(messageHandler);
        timedJECRestarter.forceImmediateRestart();
        Thread.sleep(200L);
        verify(messageHandler);
    }

    @Test
    public void testTimedRestart() throws EqIOException, InterruptedException {
        messageHandler.disconnectFromDataSource();
        messageHandler.connectToDataSource();
        
        replay(messageHandler);
        timedJECRestarter.triggerRestart();
        Thread.sleep(300L);
        verify(messageHandler);
    }
    
    @Test
    public void testMultipleTimedRestart() throws EqIOException, InterruptedException {
        messageHandler.disconnectFromDataSource();
        messageHandler.connectToDataSource();
        
        replay(messageHandler);
        // despite of the multiple restart triggers the restart should only occurre once.
        for (int i = 0; i < 100; i++) {
            timedJECRestarter.triggerRestart();
            Thread.sleep(10); // simulate work
        }
        Thread.sleep(300L);
        verify(messageHandler);
    }
}
