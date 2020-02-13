package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.configuration.util.TestConfigurationProvider;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.client.tag.TagMode;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.*;

public class ControlTagConfigTest extends ConfigurationCacheLoaderTest<Process> {

  @Inject private C2monCache<AliveTag> aliveTimerCache;

  @Inject private C2monCache<CommFaultTag> commFaultTagCache;

  @Inject private C2monCache<SupervisionStateTag> stateTagCache;

  @Test
  public void createAliveTag() {
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);

    AliveTag expectedObjectAlive = new AliveTag(101L, 5L, "P_INI_TEST", "PROC", null, 100L, 60000);
    expectedObjectAlive.setName("P:ALIVE");
    expectedObjectAlive.setDescription("<no description provided>");
    expectedObjectAlive.setMode((short) TagMode.OPERATIONAL.ordinal());
    expectedObjectAlive.setDataType("java.lang.Long");
    expectedObjectAlive.setValue(true);

    AliveTag cacheObject = aliveTimerCache.get(101L);
    // We need to set this, as it was non deterministic
    expectedObjectAlive.setLastUpdate(cacheObject.getLastUpdate());

    assertEquals(expectedObjectAlive, cacheObject);
  }

  @Test
  public void updateAliveTag() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);

    // TEST:
    cern.c2mon.shared.client.configuration.api.tag.AliveTag aliveTagUpdate =
      cern.c2mon.shared.client.configuration.api.tag.AliveTag.update(101L).description("new description").mode(TagMode.OPERATIONAL).build();
    Configuration configuration = new Configuration();
    configuration.addEntity(aliveTagUpdate);

    AliveTag expectedObjectAlive = new AliveTag(101L, 5L, "P_INI_TEST", "PROC", null, 100L, 60000);
    expectedObjectAlive.setName("P:ALIVE");
    expectedObjectAlive.setDescription("new description");
    expectedObjectAlive.setMode((short) TagMode.OPERATIONAL.ordinal());
    expectedObjectAlive.setDataType("java.lang.Long");
    expectedObjectAlive.setValue(true);

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());

    AliveTag cacheObject = aliveTimerCache.get(101L);
    // We need to set this, as it was non deterministic
    expectedObjectAlive.setLastUpdate(cacheObject.getLastUpdate());

    assertEquals(expectedObjectAlive, cacheObject);
  }

  @Test
  public void updateStatusTag() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);

    // TEST:
    StatusTag statusTagUpdate = StatusTag.update(100L).description("new description").mode(TagMode.OPERATIONAL).build();
    Configuration configuration = new Configuration();
    configuration.addEntity(statusTagUpdate);
    SupervisionStateTag expectedStateTag = new SupervisionStateTag(100L, 5L, "PROC", 101L, null);
    expectedStateTag.setName("P:STATUS");
    expectedStateTag.setDescription("new description");
    expectedStateTag.setLogged(true);
    expectedStateTag.setMode((short) TagMode.OPERATIONAL.ordinal());
    expectedStateTag.setDataType("java.lang.String");

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());
    assertEquals(expectedStateTag, stateTagCache.get(100L));
  }

  @Test
  public void updateCommFaultTag() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration createEquipment = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(createEquipment);

    cern.c2mon.shared.client.configuration.api.tag.CommFaultTag commFaultTagUpdate =
      cern.c2mon.shared.client.configuration.api.tag.CommFaultTag.update(201L).description("new description").mode(TagMode.OPERATIONAL).build();
    Configuration configuration = new Configuration();
    configuration.addEntity(commFaultTagUpdate);

    // Unsure if this aliveTagId is flaky because it relies on autogen'd sequence number
    CommFaultTag expectedCommFault = new CommFaultTag(201L, 15L, "E_INI_TEST","EQ", 200L, 300000L);
    expectedCommFault.setDescription("new description");
    expectedCommFault.setName("E:Comm");
    expectedCommFault.setLogged(true);
    expectedCommFault.setMode((short) TagMode.OPERATIONAL.ordinal());

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());

    assertEquals(expectedCommFault, commFaultTagCache.get(201L));
  }
}
