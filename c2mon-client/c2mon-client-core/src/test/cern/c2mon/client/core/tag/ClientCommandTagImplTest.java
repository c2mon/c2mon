package cern.c2mon.client.core.tag;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

import org.junit.Test;

import cern.tim.shared.client.command.CommandTagHandleImpl;
import cern.tim.shared.client.command.CommandTagValueException;
import cern.tim.shared.client.command.RbacAuthorizationDetails;

public class ClientCommandTagImplTest {

  @Test
  public void testXMLSerialization() throws Exception {
      
    ClientCommandTagImpl commandTag = createCommandTag (12342L);
    
    String xml = commandTag.getXml();
    commandTag.toString();
    
    ClientCommandTagImpl newCommandTag = ClientCommandTagImpl.fromXml(xml);
    
    assertTrue(commandTag.getClientTimeout() == newCommandTag.getClientTimeout());
    assertTrue(commandTag.getId().equals(newCommandTag.getId()));
    assertTrue(commandTag.getMaxValue().equals(newCommandTag.getMaxValue()));
  }  
  
  /**
   * Private helper method. Creates ClientCommandTagImpl.
   */  
  private ClientCommandTagImpl createCommandTag (Long id) {
    
    ClientCommandTagImpl commandTag = new ClientCommandTagImpl(id);
    commandTag.update(createCommandTagHandleImpl(id));
    
    try {
      commandTag.setValue((Object)(new String("HI!")));
    } catch (CommandTagValueException e) {
      e.printStackTrace();
    }
    
    return commandTag;
  }
  
  /**
   * Private helper method. Used to create ClientCommandTagImpl.
   */
  private CommandTagHandleImpl createCommandTagHandleImpl (final Long id) {
    
    CommandTagHandleImpl commandTagHandle = new CommandTagHandleImpl(id, "test name",
        "test description", new String("LALA").getClass().getName(), 666, "666", "666"
        , "test Host Name", createAuthDetails());
    
    return commandTagHandle;
  }
  
  /**
   * Private helper method. Used to create ClientCommandTagImpl.
   */
  private RbacAuthorizationDetails createAuthDetails () {

    RbacAuthorizationDetails authDetails = new RbacAuthorizationDetails();
    
    authDetails.setRbacClass("Manos");
    authDetails.setRbacDevice("Mark");
    authDetails.setRbacProperty("Matias");
    
    return authDetails;
  }
}
