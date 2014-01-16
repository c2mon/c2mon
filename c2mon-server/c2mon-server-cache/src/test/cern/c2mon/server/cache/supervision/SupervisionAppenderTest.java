package cern.c2mon.server.cache.supervision;

import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * Junit test of supervision appender.
 * @author Mark Brightwell
 *
 */
public class SupervisionAppenderTest {

  /**
   * Class to test.
   */
  private SupervisionAppenderImpl supervisionAppender;
  
  /**
   * Mocks
   */
  IMocksControl mockControl = EasyMock.createControl();
  private ProcessCache processCache;
  private ProcessFacade processFacade;
  private EquipmentCache equipmentCache;
  private EquipmentFacade equipmentFacade;
  
  @Before
  public void setUp() {       
    processCache = mockControl.createMock(ProcessCache.class);
    processFacade = mockControl.createMock(ProcessFacade.class);
    equipmentCache = mockControl.createMock(EquipmentCache.class);
    equipmentFacade = mockControl.createMock(EquipmentFacade.class);
    supervisionAppender = new SupervisionAppenderImpl(processFacade, processCache, equipmentFacade, equipmentCache);
  }
  
  @Test
  public void testAddSupervisionQuality() {
    RuleTagCacheObject tag = new RuleTagCacheObject(1L);    
    SupervisionEvent event = new SupervisionEventImpl(SupervisionEntity.PROCESS, 10L, SupervisionStatus.DOWN, new Timestamp(System.currentTimeMillis()), "test message");
        
    mockControl.replay();
    
    supervisionAppender.addSupervisionQuality(tag, event);
    
    mockControl.verify();
    
    assertTrue(!tag.isValid());
    assertTrue(tag.getDataTagQuality().isInvalidStatusSet(TagQualityStatus.PROCESS_DOWN));
    assertTrue(!tag.getDataTagQuality().isInvalidStatusSet(TagQualityStatus.EQUIPMENT_DOWN));
    assertTrue(tag.getDataTagQuality().getDescription().equals("test message"));
    System.out.println(tag.getDataTagQuality().getDescription());
  }
  
  
}
