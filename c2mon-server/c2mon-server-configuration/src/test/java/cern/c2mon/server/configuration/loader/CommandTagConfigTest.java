package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.CommandTagMapper;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.configuration.helper.ObjectEqualityComparison;
import cern.c2mon.server.configuration.parser.util.ConfigurationCommandTagUtil;
import cern.c2mon.server.configuration.util.TestConfigurationProvider;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;

import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

public class CommandTagConfigTest extends ConfigurationCacheLoaderTest<CommandTag> {

  @Inject
  private C2monCache<CommandTag> commandTagCache;

  @Inject
  private CommandTagMapper commandTagMapper;

  @Inject
  private ProcessService processService;

  @Test
  public void testCreateAndUpdateCommandTag() {
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
  public void testRemoveCommand() {
    // check as expected
    assertTrue(commandTagCache.containsKey(11000L));
    assertNotNull(commandTagMapper.getItem(11000L));

    // rung test
    ConfigurationReport report = configurationLoader.applyConfiguration(9);

    // check successful
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(commandTagCache.containsKey(11000L));
    assertNull(commandTagMapper.getItem(11000L));
    verify(mockManager);
  }

  @Test
  public void createCommandTag() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration createEquipment = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(createEquipment);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to add the test DataTag
    cern.c2mon.shared.client.configuration.api.tag.CommandTag commandTag = ConfigurationCommandTagUtil.buildCreateAllFieldsCommandTag(500L, null);
    commandTag.setEquipmentId(15L);

    Configuration configuration = new Configuration();
    configuration.addEntity(commandTag);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare to the an expected cacheObject
    CommandTagCacheObject cacheObjectCommand = (CommandTagCacheObject) commandTagCache.get(500L);
    CommandTagCacheObject expectedCacheObjectCommand = cacheObjectFactory.buildCommandTagCacheObject(500L, commandTag);

    ObjectEqualityComparison.assertCommandTagEquals(expectedCacheObjectCommand, cacheObjectCommand);

    verify(mockManager);
  }

  @Test
  public void updateCommandTag() {

    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration createEquipment = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(createEquipment);
    Configuration createCommandTag = TestConfigurationProvider.createCommandTag();
    configurationLoader.applyConfiguration(createCommandTag);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to update the test CommandTag
    cern.c2mon.shared.client.configuration.api.tag.CommandTag commandTagUpdate = cern.c2mon.shared.client.configuration.api.tag.CommandTag.update(500L)
      .hardwareAddress(new SimpleHardwareAddressImpl("updateAddress"))
      .minimum(50)
      .rbacClass("updateClass")
      .description("new description").build();
    Configuration configuration = new Configuration();
    configuration.addEntity(commandTagUpdate);

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare to the an expected cacheObject
    CommandTagCacheObject cacheObjectCommand = (CommandTagCacheObject) commandTagCache.get(500L);
    CommandTagCacheObject expectedCacheObjectCommand = cacheObjectFactory.buildCommandTagUpdateCacheObject(cacheObjectCommand, commandTagUpdate);

    ObjectEqualityComparison.assertCommandTagEquals(expectedCacheObjectCommand, cacheObjectCommand);

    verify(mockManager);
  }
}
