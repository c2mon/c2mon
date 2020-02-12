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
import java.sql.Timestamp;

import static org.junit.Assert.*;

public class ControlTagConfigTest extends ConfigurationCacheLoaderTest<Process> {

  @Inject private C2monCache<AliveTag> aliveTimerCache;

  @Inject private C2monCache<CommFaultTag> commFaultTagCache;

  @Inject private C2monCache<SupervisionStateTag> stateTagCache;

  @Test
  public void updateAliveTag() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    cern.c2mon.shared.client.configuration.api.tag.AliveTag aliveTagUpdate =
      cern.c2mon.shared.client.configuration.api.tag.AliveTag.update(101L).description("new description").mode(TagMode.OPERATIONAL).build();
    Configuration configuration = new Configuration();
    configuration.addEntity(aliveTagUpdate);

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());

    // check aliveTag in the cache
    AliveTag cacheObjectAlive = aliveTimerCache.get(101L);
    AliveTag expectedObjectAlive = new AliveTag(101L, 5L, "P_INI_TEST", "PROC", null, 100L, 60000);
    assertEquals(expectedObjectAlive, cacheObjectAlive);
  }

  @Test
  public void updateStatusTag() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    StatusTag statusTagUpdate = StatusTag.update(100L).description("new description").mode(TagMode.OPERATIONAL).build();
    Configuration configuration = new Configuration();
    configuration.addEntity(statusTagUpdate);

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());
    SupervisionStateTag expectedStateTag = new SupervisionStateTag(100L, 5L, "PROC", 101L, null);
    assertEquals(expectedStateTag, stateTagCache.get(100L));
  }

  @Test
  public void updateCommFaultTag() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration createEquipment = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(createEquipment);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to update the test equipment
    cern.c2mon.shared.client.configuration.api.tag.CommFaultTag commFaultTagUpdate =
      cern.c2mon.shared.client.configuration.api.tag.CommFaultTag.update(201L).description("new description").mode(TagMode.OPERATIONAL).build();
    Configuration configuration = new Configuration();
    configuration.addEntity(commFaultTagUpdate);

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());
    CommFaultTag expectedCommFault = new CommFaultTag(201L, 15L, "E_INI_TEST","EQ", 200L, null);
    assertEquals(expectedCommFault, commFaultTagCache.get(201L));
  }
}
