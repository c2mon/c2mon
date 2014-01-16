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
