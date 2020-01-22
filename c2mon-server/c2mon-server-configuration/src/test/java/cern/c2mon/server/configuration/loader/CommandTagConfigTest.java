package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.CommandTagMapper;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.configuration.helper.ObjectEqualityComparison;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import org.easymock.EasyMock;
import org.junit.Test;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class CommandTagConfigTest extends ConfigurationCacheLoaderTest<CommandTag> {

  @Inject
  private C2monCache<CommandTag> commandTagCache;

  @Inject
  private CommandTagMapper commandTagMapper;

  @Test
  public void testCreateAndUpdateCommandTag() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
    NoSuchFieldException, NoSimpleValueParseException {
    // the mocked ProcessCommmunicationManager can return an empty report
    // (expect 3 calls)
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(3);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    CommandTagCacheObject cacheObject = (CommandTagCacheObject) commandTagCache.get(10000L);

    CommandTagCacheObject expectedObject = new CommandTagCacheObject(10000L, "Test CommandTag", "test description", "String", DataTagConstants.MODE_TEST);
    // expectedObject.setAuthorizedHostsPattern("*");
    expectedObject.setEquipmentId(150L);
    expectedObject.setClientTimeout(30000);
    expectedObject.setExecTimeout(6000);
    expectedObject.setSourceRetries(2);
    expectedObject.setSourceTimeout(200);
    RbacAuthorizationDetails details = new RbacAuthorizationDetails();
    details.setRbacClass("RBAC class");
    details.setRbacDevice("RBAC device");
    details.setRbacProperty("RBAC property");
    expectedObject.setAuthorizationDetails(details);
    expectedObject
      .setHardwareAddress(HardwareAddressFactory
        .getInstance()
        .fromConfigXML(
          "<HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl\"><opc-item-name>PLC_B_CMD_ACQ_DEF_5A6</opc-item-name><command-pulse-length>100</command-pulse-length></HardwareAddress>"));
    ObjectEqualityComparison.assertCommandTagEquals(expectedObject, cacheObject);

    // test update
    report = configurationLoader.applyConfiguration(5);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    CommandTagCacheObject cacheObjectUpdated = (CommandTagCacheObject) commandTagCache.get(10000L);

    expectedObject.setName("Test CommandTag Updated");
    expectedObject.getAuthorizationDetails().setRbacClass("new RBAC class");
    expectedObject.getAuthorizationDetails().setRbacDevice("new RBAC device");
    expectedObject
      .setHardwareAddress(HardwareAddressFactory
        .getInstance()
        .fromConfigXML(
          "<HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl\"><opc-item-name>PLC_B_CMD_ACQ_DEF_5A6</opc-item-name><command-pulse-length>150</command-pulse-length></HardwareAddress>"));
    ObjectEqualityComparison.assertCommandTagEquals(expectedObject, cacheObjectUpdated);
  }

  @Test
  public void testRemoveCommand() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
    NoSuchFieldException, NoSimpleValueParseException {
    // check as expected
    assertTrue(commandTagCache.containsKey(11000L));
    assertNotNull(commandTagMapper.getItem(11000L));
    EasyMock.expect(mockManager.sendConfiguration(EasyMock.isA(Long.class), EasyMock.isA(List.class))).andReturn(new ConfigurationChangeEventReport());

    // rung test
    replay(mockManager);
    ConfigurationReport report = configurationLoader.applyConfiguration(9);

    // check successful
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(commandTagCache.containsKey(11000L));
    assertNull(commandTagMapper.getItem(11000L));
    verify(mockManager);
  }
}
