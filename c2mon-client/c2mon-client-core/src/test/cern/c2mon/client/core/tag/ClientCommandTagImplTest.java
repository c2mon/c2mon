package cern.c2mon.client.core.tag;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.shared.client.command.CommandTagHandleImpl;
import cern.c2mon.shared.client.command.CommandTagHandleImpl.Builder;
import cern.c2mon.shared.client.command.CommandTagValueException;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl;

public class ClientCommandTagImplTest {

  @Test
  public void testXMLSerialization() throws Exception {
      
    ClientCommandTagImpl<String> commandTag = createCommandTag(12342L);
    
    String xml = commandTag.getXml();
    commandTag.toString();
    
    ClientCommandTagImpl<String> newCommandTag = ClientCommandTagImpl.fromXml(xml);
    
    assertTrue(commandTag.getClientTimeout() == newCommandTag.getClientTimeout());
    assertTrue(commandTag.getId().equals(newCommandTag.getId()));
    assertTrue(commandTag.getMaxValue().equals(newCommandTag.getMaxValue()));
    assertTrue(commandTag.getHardwareAddress().equals(newCommandTag.getHardwareAddress()));
  } 
  
  /**
   * This test is useful when checking that changes made on the server are supported by the client. However
   * it is not a good idea to run this in a continuous deployment environment since the test is
   * dependent on the life test system.
   */
//  @Test
  public void startClientWithProperties() throws InterruptedException {
    System.setProperty("c2mon.client.conf.url", "http://timweb/test/conf/c2mon-client.properties");
    C2monServiceGateway.startC2monClientSynchronous();
    ClientCommandTag<Boolean> commandTag = C2monServiceGateway.getCommandManager().getCommandTag(104974L);
    assertNotNull(commandTag.getName());
    assertTrue(!commandTag.getName().equalsIgnoreCase(""));
    assertNotNull(commandTag.getProcessId());
    assertNotNull(commandTag.getHardwareAddress());
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
  private CommandTagHandleImpl<String> createCommandTagHandleImpl (final Long id) {
    Builder<String> builder = new Builder<String>(id);
    builder = builder.clientTimeout(666)
                     .dataType(String.class.getName())
                     .description("test descriptor")
                     .hardwareAddress(createHardwareAddress())
                     .maxValue("666")
                     .minValue("1")
                     .name("test name")
                     .rbacAuthorizationDetails(createAuthDetails())
                     .processId(123L);
    
    return new CommandTagHandleImpl<String>(builder);
  }
  
  /**
   * Private helper method. Used to create ClientCommandTagImpl.
   */
  private RbacAuthorizationDetails createAuthDetails() {

    RbacAuthorizationDetails authDetails = new RbacAuthorizationDetails();
    
    authDetails.setRbacClass("Manos");
    authDetails.setRbacDevice("Mark");
    authDetails.setRbacProperty("Matias");
    
    return authDetails;
  }
  
  private HardwareAddress createHardwareAddress() {
   return new SimpleHardwareAddressImpl("test-address");
  }
}
