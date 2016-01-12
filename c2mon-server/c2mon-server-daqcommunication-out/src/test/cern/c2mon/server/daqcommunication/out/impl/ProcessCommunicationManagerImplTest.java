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
package cern.c2mon.server.daqcommunication.out.impl;

import javax.jms.ConnectionFactory;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.shared.util.parser.XmlParser;

/**
 * Unit test of ProcessCommunicationManager implementation.
 * 
 * @author Mark Brightwell
 *
 */
public class ProcessCommunicationManagerImplTest {

  private ProcessCommunicationManagerImpl processCommunicationManager;
  
  private IMocksControl controller;
  
  @Before
  public void init() {
    controller = EasyMock.createStrictControl();
    ProcessCache processCache = controller.createMock(ProcessCache.class);
    EquipmentCache equipmentCache = controller.createMock(EquipmentCache.class);    
    ProcessFacade processFacade = controller.createMock(ProcessFacade.class);
    JmsProcessOut jmsProcessOut = controller.createMock(JmsProcessOut.class);
    XmlParser xmlParser = controller.createMock(XmlParser.class);
    ConnectionFactory connectionFactory = controller.createMock(ConnectionFactory.class);
    processCommunicationManager = new ProcessCommunicationManagerImpl(equipmentCache, processCache, processFacade, jmsProcessOut, xmlParser, connectionFactory);
  }
  
  @Test
  public void testExecuteCommand() {
    
    controller.replay();
    
  }
  
}
