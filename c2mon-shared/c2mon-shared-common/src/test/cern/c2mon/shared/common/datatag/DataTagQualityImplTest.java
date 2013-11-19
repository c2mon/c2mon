package cern.c2mon.shared.common.datatag;

import static org.junit.Assert.*;
import org.junit.Test;

import cern.c2mon.util.json.GsonFactory;

import com.google.gson.Gson;

public class DataTagQualityImplTest {

  private static final String VALID_DESCR = "OK";
  
  @Test
  public void testDefaultConstructor() {
    DataTagQuality tagQuality = new DataTagQualityImpl();
    assertFalse(tagQuality.isInitialised());
    assertEquals(1, tagQuality.getInvalidQualityStates().size());
  }
  
  @Test(expected=NullPointerException.class)
  public void testCopyConstructor() {
    DataTagQuality validTagQuality = new DataTagQualityImpl();
    validTagQuality.validate();
    DataTagQuality tagQualityCopy = new DataTagQualityImpl(validTagQuality);
    assertTrue(tagQualityCopy.isValid());

    String description = "Equipment is down!";
    DataTagQuality invalidTagQuality = new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN, description);
    tagQualityCopy = new DataTagQualityImpl(invalidTagQuality);
    assertFalse(tagQualityCopy.isAccessible());
    assertEquals(1, tagQualityCopy.getInvalidQualityStates().size());
    assertTrue(tagQualityCopy.isInvalidStatusSet(TagQualityStatus.EQUIPMENT_DOWN));
    assertEquals(description, tagQualityCopy.getInvalidQualityStates().get(TagQualityStatus.EQUIPMENT_DOWN));
    
    // Test null pointer exception
    DataTagQuality nullPointer = null;
    tagQualityCopy = new DataTagQualityImpl(nullPointer);
  }
  
  
  @Test
  public void testSetQualityStates() {
    DataTagQuality originalTagQuality = new DataTagQualityImpl();
    String equipmentDown = "Equipment down";
    originalTagQuality.setInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, equipmentDown);
    String outOfBounds = "Value is out of bounds";
    originalTagQuality.addInvalidStatus(TagQualityStatus.VALUE_OUT_OF_BOUNDS, outOfBounds);
    
    DataTagQuality updatedTagQuality = new DataTagQualityImpl();
    updatedTagQuality.addInvalidStatus(TagQualityStatus.VALUE_EXPIRED);
    // overwrite all states with given map
    updatedTagQuality.setInvalidStates(originalTagQuality.getInvalidQualityStates());
    
    assertEquals(originalTagQuality.getInvalidQualityStates().size(), updatedTagQuality.getInvalidQualityStates().size());
    assertTrue(originalTagQuality.equals(updatedTagQuality));
  }

  
  @Test
  public void testQualityStatusConstructor() {
    DataTagQuality tagQuality = new DataTagQualityImpl(TagQualityStatus.PROCESS_DOWN);
    assertFalse(tagQuality.isAccessible());
    assertEquals(1, tagQuality.getInvalidQualityStates().size());
    
    // Test null pointer initialization
    tagQuality = new DataTagQualityImpl((TagQualityStatus) null);
    assertFalse(tagQuality.isInitialised());
  }
  
  
  @Test
  public void testQualityStatusWithDescriptionConstructor() {
    String description = "The DAQ process is down";
    DataTagQuality tagQuality = new DataTagQualityImpl(TagQualityStatus.PROCESS_DOWN, description);
    assertFalse(tagQuality.isAccessible());
    assertEquals(1, tagQuality.getInvalidQualityStates().size());
    assertEquals(description, tagQuality.getDescription());
    
    // Test null pointer initialization
    tagQuality = new DataTagQualityImpl((TagQualityStatus) null, null);
    assertFalse(tagQuality.isInitialised());
  }
  
  @Test
  public void testIsValid() {
    DataTagQuality validTagQuality = new DataTagQualityImpl();
    validTagQuality.validate();
    assertTrue(validTagQuality.isValid());
    assertEquals(VALID_DESCR, validTagQuality.getDescription());
    
    validTagQuality.addInvalidStatus(TagQualityStatus.UNKNOWN_REASON);
    assertFalse(validTagQuality.isValid());
  }
  
  @Test
  public void testIsExistingTag() {
    DataTagQuality tagQuality = new DataTagQualityImpl();
    tagQuality.validate();
    assertTrue(tagQuality.isExistingTag());
    
    tagQuality.addInvalidStatus(TagQualityStatus.UNKNOWN_REASON);
    assertTrue(tagQuality.isExistingTag());
    
    tagQuality.addInvalidStatus(TagQualityStatus.UNDEFINED_TAG);
    assertFalse(tagQuality.isExistingTag());
    
    tagQuality.removeInvalidStatus(TagQualityStatus.UNDEFINED_TAG);
    assertTrue(tagQuality.isExistingTag());
  }
  
  @Test
  public void testIsAccessible() {
    DataTagQuality tagQuality = new DataTagQualityImpl();
    assertTrue(tagQuality.isAccessible());
    
    tagQuality.setInvalidStatus(TagQualityStatus.INACCESSIBLE);
    assertFalse(tagQuality.isAccessible());
    tagQuality.setInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN);
    assertFalse(tagQuality.isAccessible());
    tagQuality.setInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN);
    assertFalse(tagQuality.isAccessible());
    tagQuality.setInvalidStatus(TagQualityStatus.PROCESS_DOWN);
    assertFalse(tagQuality.isAccessible());
    tagQuality.setInvalidStatus(TagQualityStatus.SERVER_HEARTBEAT_EXPIRED);
    assertFalse(tagQuality.isAccessible());
    tagQuality.setInvalidStatus(TagQualityStatus.JMS_CONNECTION_DOWN);
    assertFalse(tagQuality.isAccessible());
    
    tagQuality.validate();
    assertTrue(tagQuality.isAccessible());
    
    tagQuality.addInvalidStatus(TagQualityStatus.INACCESSIBLE);
    assertFalse(tagQuality.isAccessible());
    tagQuality.addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN);
    assertFalse(tagQuality.isAccessible());
    tagQuality.addInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN);
    assertFalse(tagQuality.isAccessible());
    tagQuality.addInvalidStatus(TagQualityStatus.PROCESS_DOWN);
    assertFalse(tagQuality.isAccessible());
    tagQuality.addInvalidStatus(TagQualityStatus.SERVER_HEARTBEAT_EXPIRED);
    assertFalse(tagQuality.isAccessible());
    tagQuality.addInvalidStatus(TagQualityStatus.JMS_CONNECTION_DOWN);
    assertFalse(tagQuality.isAccessible());
    
    tagQuality.removeInvalidStatus(TagQualityStatus.INACCESSIBLE);
    assertFalse(tagQuality.isAccessible());
    tagQuality.removeInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN);
    assertFalse(tagQuality.isAccessible());
    tagQuality.removeInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN);
    assertFalse(tagQuality.isAccessible());
    tagQuality.removeInvalidStatus(TagQualityStatus.PROCESS_DOWN);
    assertFalse(tagQuality.isAccessible());
    tagQuality.removeInvalidStatus(TagQualityStatus.SERVER_HEARTBEAT_EXPIRED);
    assertFalse(tagQuality.isAccessible());
    tagQuality.removeInvalidStatus(TagQualityStatus.JMS_CONNECTION_DOWN);
    assertTrue(tagQuality.isAccessible());
  }
  
  @Test
  public void testIsInitialised() {
    DataTagQuality tagQuality = new DataTagQualityImpl();
    assertFalse(tagQuality.isInitialised());
    
    tagQuality.setInvalidStatus(TagQualityStatus.VALUE_OUT_OF_BOUNDS);
    assertTrue(tagQuality.isInitialised());
    
    tagQuality = new DataTagQualityImpl();
    tagQuality.validate();
    assertTrue(tagQuality.isInitialised());
    
    tagQuality = new DataTagQualityImpl();
    tagQuality.addInvalidStatus(TagQualityStatus.VALUE_OUT_OF_BOUNDS);
    assertFalse(tagQuality.isInitialised());
  }
  
  @Test
  public void testIsInvalidStatusSet() {
    DataTagQuality tagQuality = new DataTagQualityImpl();
    assertTrue(tagQuality.isInvalidStatusSet(TagQualityStatus.UNINITIALISED));
    
    for (TagQualityStatus status : TagQualityStatus.values()) {
      tagQuality.addInvalidStatus(status);
      assertTrue(tagQuality.isInvalidStatusSet(status));
    }
    for (TagQualityStatus status : TagQualityStatus.values()) {
      assertTrue(tagQuality.isInvalidStatusSet(status));
    }
    
    tagQuality.validate();
    for (TagQualityStatus status : TagQualityStatus.values()) {
      assertFalse(tagQuality.isInvalidStatusSet(status));
    }
  }
  
  @Test
  public void testGetDescription() {
    DataTagQuality tagQuality = new DataTagQualityImpl();
    assertEquals("", tagQuality.getDescription());
    
    tagQuality.validate();
    assertEquals(VALID_DESCR, tagQuality.getDescription());
    
    // Setting the status shall overwrite the others
    
    String undefined = "Tag is not defined";
    assertTrue(tagQuality.setInvalidStatus(TagQualityStatus.UNDEFINED_TAG, undefined));
    assertEquals(undefined, tagQuality.getDescription());
    
    String subEquipmentDown = "Sub-equipment is down";
    assertTrue(tagQuality.setInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN, subEquipmentDown));
    assertEquals(subEquipmentDown, tagQuality.getDescription());
    
    String equipmentDown = "Equipment is down";
    assertTrue(tagQuality.setInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, equipmentDown));
    assertEquals(equipmentDown, tagQuality.getDescription());
    
    String processDown = "DAQ process is down";
    assertTrue(tagQuality.setInvalidStatus(TagQualityStatus.PROCESS_DOWN, processDown));
    assertEquals(processDown, tagQuality.getDescription());
    
    // Adding the status shall return the description of the highest severity
    assertTrue(tagQuality.addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, equipmentDown));
    assertEquals(processDown, tagQuality.getDescription());
    
    assertTrue(tagQuality.addInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN, subEquipmentDown));
    assertEquals(processDown, tagQuality.getDescription());
    
    assertTrue(tagQuality.addInvalidStatus(TagQualityStatus.UNDEFINED_TAG, undefined));
    assertEquals(undefined, tagQuality.getDescription());
    
    tagQuality.removeInvalidStatus(TagQualityStatus.PROCESS_DOWN);
    assertEquals(undefined, tagQuality.getDescription());
    
    tagQuality.removeInvalidStatus(TagQualityStatus.UNDEFINED_TAG);
    assertEquals(equipmentDown, tagQuality.getDescription());
    
    tagQuality.removeInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN);
    assertEquals(subEquipmentDown, tagQuality.getDescription());
    
    tagQuality.removeInvalidStatus(TagQualityStatus.SUBEQUIPMENT_DOWN);
    assertEquals(VALID_DESCR, tagQuality.getDescription());
    
    // test concatenation of error messages with same severity
    final String separator = "; "; 
    String outOfBounds = "Value is out of bounds";
    assertTrue(tagQuality.addInvalidStatus(TagQualityStatus.VALUE_OUT_OF_BOUNDS, outOfBounds));
    assertEquals(outOfBounds, tagQuality.getDescription());
    
    String unknownReason = "Unknown";
    assertTrue(tagQuality.addInvalidStatus(TagQualityStatus.UNKNOWN_REASON, unknownReason));
    if ((outOfBounds + separator + unknownReason).equalsIgnoreCase(tagQuality.getDescription())) {
      assertTrue(true);
    }
    else if ((unknownReason + separator + outOfBounds).equalsIgnoreCase(tagQuality.getDescription())) {
      assertTrue(true);
    }
    else {
      assertTrue("Wrong result for getDescription()", false);
    }
  }
  
  
  @Test
  public void testCloneMethod() {
    String description = "Equipment is down!";
    DataTagQuality invalidTagQuality = new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN, description);
    DataTagQuality tagQualityCopy = null;
    try {
      tagQualityCopy = invalidTagQuality.clone();
      assertNotNull(tagQualityCopy);
      assertFalse(tagQualityCopy.isAccessible());
      assertEquals(1, tagQualityCopy.getInvalidQualityStates().size());
      assertTrue(tagQualityCopy.isInvalidStatusSet(TagQualityStatus.EQUIPMENT_DOWN));
      assertEquals(description, tagQualityCopy.getInvalidQualityStates().get(TagQualityStatus.EQUIPMENT_DOWN));
    }
    catch (CloneNotSupportedException e) {
      assertTrue(e.getMessage(), false);
    }
  }
  
  @Test
  public void testEqualsMethod() {
    String description = "Equipment is down!";
    DataTagQuality invalidTagQuality1 = new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN, description);
    DataTagQuality invalidTagQuality2 = new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN, description);
    DataTagQuality validTagQuality = new DataTagQualityImpl();
    validTagQuality.validate();
    
    assertTrue(invalidTagQuality1.equals(invalidTagQuality2));
    assertTrue(invalidTagQuality2.equals(invalidTagQuality1));
    assertFalse(validTagQuality.equals(invalidTagQuality1));
    
    invalidTagQuality2.setInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, "Some other reason");
    assertFalse(invalidTagQuality1.equals(invalidTagQuality2));
    assertFalse(invalidTagQuality2.equals(invalidTagQuality1));
  }
  
  @Test
  public void testToString() {
    DataTagQuality tagQuality = new DataTagQualityImpl();
    tagQuality.validate();
    assertEquals(VALID_DESCR, tagQuality.toString());
    
    String separator = "+";
    String equipmentDown = "EQUIPMENT_DOWN";
    tagQuality.addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN);
    assertEquals(equipmentDown, tagQuality.toString());
    
    String valueExpired = "VALUE_EXPIRED";
    tagQuality.addInvalidStatus(TagQualityStatus.VALUE_EXPIRED);
    if ((valueExpired + separator + equipmentDown).equalsIgnoreCase(tagQuality.toString())) {
      assertTrue(true);
    }
    else if ((equipmentDown + separator + valueExpired).equalsIgnoreCase(tagQuality.toString())) {
      assertTrue(true);
    }
    else {
      assertTrue("Wrong result for toString()", false);
    }
  }
  
  
  @Test
  public void testJsonSerializationDeserialization() {
    Gson gson = GsonFactory.createGson();
    DataTagQuality validTagQuality = new DataTagQualityImpl();
    validTagQuality.validate();
    
    String gsonMessage = gson.toJson(validTagQuality);
    DataTagQuality deserializedQuality = gson.fromJson(gsonMessage, DataTagQualityImpl.class);
    assertTrue(deserializedQuality.isValid());
    assertEquals(0, deserializedQuality.getInvalidQualityStates().size());
    
    DataTagQuality invalidTagQuality = new DataTagQualityImpl();
    String equipmentDown = "Equipment down";
    invalidTagQuality.setInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, equipmentDown);
    String outOfBounds = "Value is out of bounds";
    invalidTagQuality.addInvalidStatus(TagQualityStatus.VALUE_OUT_OF_BOUNDS, outOfBounds);
    assertFalse(invalidTagQuality.isValid());
    assertTrue(invalidTagQuality.isInitialised());
    
    gsonMessage = gson.toJson(invalidTagQuality);
    deserializedQuality = gson.fromJson(gsonMessage, DataTagQualityImpl.class);
    assertFalse(deserializedQuality.isValid());
    assertTrue(deserializedQuality.isInitialised());
    assertTrue(deserializedQuality.isInvalidStatusSet(TagQualityStatus.EQUIPMENT_DOWN));
    assertEquals(equipmentDown, deserializedQuality.getInvalidQualityStates().get(TagQualityStatus.EQUIPMENT_DOWN));
    assertTrue(deserializedQuality.isInvalidStatusSet(TagQualityStatus.VALUE_OUT_OF_BOUNDS));
    assertEquals(outOfBounds, deserializedQuality.getInvalidQualityStates().get(TagQualityStatus.VALUE_OUT_OF_BOUNDS));
  }
  
  @Test
  public void testStatusAndDescriptionComparisonFalse() {
    DataTagQuality quality = new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN);
    assertFalse(quality.isInvalidStatusSetWithSameDescription(TagQualityStatus.INACCESSIBLE, ""));    
  }
  
  /**
   * Also test null and "" are treated the same as descriptions.
   */
  @Test
  public void testStatusAndDescriptionComparisonTrue() {
    DataTagQuality quality = new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN, null);
    assertTrue(quality.isInvalidStatusSetWithSameDescription(TagQualityStatus.EQUIPMENT_DOWN, ""));
  }
  
  /**
   * As with test number 1 but switched null and "".
   */
  @Test
  public void testStatusAndDescriptionComparisonTrue2() {
    DataTagQuality quality = new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN, "");
    assertTrue(quality.isInvalidStatusSetWithSameDescription(TagQualityStatus.EQUIPMENT_DOWN, null));    
  }
  
}
