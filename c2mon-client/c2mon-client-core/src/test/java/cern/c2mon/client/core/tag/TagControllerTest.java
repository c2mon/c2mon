package cern.c2mon.client.core.tag;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Arrays;

import org.junit.Test;

import cern.c2mon.client.core.tag.utils.TestTagUpdate;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * Tests for the TagController.
 * 
 * @author Ivan Prieto Barreiro
 */
public class TagControllerTest {

  private static final long TAG_ID = 1234L;
  private static final Timestamp CURRENT_TIME = new Timestamp(System.currentTimeMillis());
  
  @Test
  public void testUpdateWithProcessDown() throws RuleFormatException {
    Long processId = 666L;
    
    TagController tagController = new TagController(TAG_ID);
    
    TestTagUpdate tagUpdate1 = TestTagUpdate.create();
    tagUpdate1.setServerTimestamp(CURRENT_TIME);
    tagUpdate1.setDaqTimestamp(CURRENT_TIME);
    tagUpdate1.setSourceTimestamp(CURRENT_TIME);
    tagUpdate1.setProcessIds(Arrays.asList(processId));
    tagUpdate1.getDataTagQuality().removeInvalidStatus(TagQualityStatus.UNINITIALISED);
    
    tagController.update(tagUpdate1);
    
    assertTrue("The data tag should be valid", tagController.getTagImpl().getDataTagQuality().isValid());
    
    SupervisionEvent processDownEvent = new SupervisionEventImpl(
        SupervisionConstants.SupervisionEntity.PROCESS,
        processId,
        "",
        SupervisionConstants.SupervisionStatus.DOWN,
        CURRENT_TIME,
        "Process down!");
    
    tagController.onSupervisionUpdate(processDownEvent);
    assertFalse("The data tag should be invalid", tagController.getTagImpl().getDataTagQuality().isValid());
  }
  
  
  @Test
  public void testUpdateWithEquipmentDown() throws RuleFormatException {
    Long equipmentId = 777L;
    
    TagController tagController = new TagController(TAG_ID);
    
    TestTagUpdate tagUpdate1 = TestTagUpdate.create();
    tagUpdate1.setServerTimestamp(CURRENT_TIME);
    tagUpdate1.setDaqTimestamp(CURRENT_TIME);
    tagUpdate1.setSourceTimestamp(CURRENT_TIME);
    tagUpdate1.setEquipmentIds(Arrays.asList(equipmentId));
    tagUpdate1.getDataTagQuality().removeInvalidStatus(TagQualityStatus.UNINITIALISED);
    
    tagController.update(tagUpdate1);
    
    assertTrue("The data tag should be valid", tagController.getTagImpl().getDataTagQuality().isValid());
    
    SupervisionEvent processDownEvent = new SupervisionEventImpl(
        SupervisionConstants.SupervisionEntity.EQUIPMENT,
        equipmentId,
        "",
        SupervisionConstants.SupervisionStatus.DOWN,
        CURRENT_TIME,
        "Equipment down!");
    
    tagController.onSupervisionUpdate(processDownEvent);
    assertFalse("The data tag should be invalid", tagController.getTagImpl().getDataTagQuality().isValid());
  }
  
  
  @Test
  public void testUpdateWithSubEquipmentDown() throws RuleFormatException {
    Long subEquipmentId = 888L;
    
    TagController tagController = new TagController(TAG_ID);
    
    TestTagUpdate tagUpdate1 = TestTagUpdate.create();
    tagUpdate1.setServerTimestamp(CURRENT_TIME);
    tagUpdate1.setDaqTimestamp(CURRENT_TIME);
    tagUpdate1.setSourceTimestamp(CURRENT_TIME);
    tagUpdate1.setSubEquipmentIds(Arrays.asList(subEquipmentId));
    tagUpdate1.getDataTagQuality().removeInvalidStatus(TagQualityStatus.UNINITIALISED);
    
    tagController.update(tagUpdate1);
    
    assertTrue("The data tag should be valid", tagController.getTagImpl().getDataTagQuality().isValid());
    
    SupervisionEvent processDownEvent = new SupervisionEventImpl(
        SupervisionConstants.SupervisionEntity.SUBEQUIPMENT,
        subEquipmentId,
        "",
        SupervisionConstants.SupervisionStatus.DOWN,
        CURRENT_TIME,
        "Equipment down!");
    
    tagController.onSupervisionUpdate(processDownEvent);
    assertFalse("The data tag should be invalid", tagController.getTagImpl().getDataTagQuality().isValid());
  }
}
