package cern.c2mon.server.configuration.parser.configuration;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.loading.*;
import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * TODO: Tests that use these mocks are far too white-box. They should be
 * rewritten to use the actual caches and database.
 *
 * @author Franz Ritter
 */
@ComponentScan("cern.c2mon.server.configuration.parser")
public class ConfigurationParserTestMocks {

  @Bean
  public ProcessCache processCache() {
    return EasyMock.createStrictMock(ProcessCache.class);
  }

  @Bean
  public DeviceClassCache deviceClassCacheCache() {
    return EasyMock.createStrictMock(DeviceClassCache.class);
  }
  @Bean
  public DeviceCache deviceCacheCache() {
    return EasyMock.createStrictMock(DeviceCache.class);
  }

  @Bean
  public EquipmentCache equipmentCache() {
    return EasyMock.createStrictMock(EquipmentCache.class);
  }

  @Bean
  public SubEquipmentCache subEquipmentCache() {
    return EasyMock.createStrictMock(SubEquipmentCache.class);
  }

  @Bean
  public CommFaultTagCache commFaultTagCache() {
    return EasyMock.createStrictMock(CommFaultTagCache.class);
  }

  @Bean
  public AliveTimerCache aliveTimerCache() {
    return EasyMock.createStrictMock(AliveTimerCache.class);
  }

  @Bean
  public ControlTagCache controlTagCache() {
    return EasyMock.createStrictMock(ControlTagCache.class);
  }

  @Bean
  public DataTagCache dataTagCache() {
    return EasyMock.createStrictMock(DataTagCache.class);
  }

  @Bean
  public RuleTagCache ruleTagCache() {
    return EasyMock.createStrictMock(RuleTagCache.class);
  }

  @Bean
  public AlarmCache alarmCache() {
    return EasyMock.createStrictMock(AlarmCache.class);
  }

  @Bean
  public CommandTagCache commandCache() {
    return EasyMock.createStrictMock(CommandTagCache.class);
  }

  @Bean
  public SequenceDAO sequenceDAO() {
    return EasyMock.createStrictMock(SequenceDAO.class);
  }

  @Bean
  public ProcessDAO processDAO() {
    return EasyMock.createStrictMock(ProcessDAO.class);
  }

  @Bean
  public DeviceClassDAO deviceClassDAO() {
    return EasyMock.createStrictMock(DeviceClassDAO.class);
  }
  @Bean
  public DeviceDAO deviceDAO() {
    return EasyMock.createStrictMock(DeviceDAO.class);
  }

  @Bean
  public EquipmentDAO equipmentDAO() {
    return EasyMock.createStrictMock(EquipmentDAO.class);
  }

  @Bean
  public SubEquipmentDAO subEquipmentDAO() {
    return EasyMock.createStrictMock(SubEquipmentDAO.class);
  }

  @Bean
  public TagFacadeGateway tagFacadeGateway() {
    return EasyMock.createStrictMock(TagFacadeGateway.class);
  }
}
