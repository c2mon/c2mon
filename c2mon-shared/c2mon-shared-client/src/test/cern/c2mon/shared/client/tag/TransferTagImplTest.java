package cern.c2mon.shared.client.tag;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Arrays;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * @author Matthias Braeger
 */
public class TransferTagImplTest {
  
  @Before
  public void init() {
    // Set up a simple configuration that logs on the console.
    BasicConfigurator.configure();  
  }
  
  /**
   * Helper method to create a <code>TransferTagImpl</code> test object
   * @param tagValue The tag value
   * @return A new <code>TransferTagImpl</code> test object
   */
  private static TransferTagImpl createTagForValue(final Object tagValue) {
    DataTagQualityImpl tagQuality = new DataTagQualityImpl(TagQualityStatus.PROCESS_DOWN, "Process Down");
    Timestamp sourceTimestamp = new Timestamp(System.currentTimeMillis());
    Timestamp daqTimestamp = new Timestamp(System.currentTimeMillis());
    Timestamp serverTimestamp = new Timestamp(System.currentTimeMillis());
    String descr = "Test transfer tag";
    String valDesc = "Test val desc \n \t { \"test\" : \"1212\"}\n}";
    String tagName = "tag:name";
    String topicName = "topic:name";
    
    TransferTagImpl transferTag = new TransferTagImpl(
        1234L, tagValue, valDesc, tagQuality, TagMode.TEST,
        sourceTimestamp, daqTimestamp, serverTimestamp, descr, tagName, topicName);
            
    transferTag.addEquimpmentIds(Arrays.asList(234L, 4234L, 234L));
    transferTag.addProcessIds(Arrays.asList(123L, 3214L, 123L));
    
    return transferTag;
  }   
  
  @Test
  public void testFloatJsonMsg() {
    TransferTagImpl tag = createTagForValue(Float.valueOf(3.34535f));
    String gsonString = tag.toJson();
    TagUpdate receivedTag = TransferTagImpl.fromJson(gsonString);
    assertTrue(receivedTag.getValue() instanceof Float);    
    assertEquals(tag.getDescription(), receivedTag.getDescription());    
  }
  
  /**
   * Can a new Json lib be introduced on the clients and still decode current
   * server messages?
   */
  @Test
  public void testFloatBackwardsCompatible() {
    String encoded = "{\"processIds\":[123,3214]," +
    		             "\"equipmentIds\":[4234,234]," +
    		             "\"topicName\":\"topic:name\"," +
    		             "\"tagName\":\"tag:name\"," +
    		             "\"tagId\":1234," +
    		             "\"valueClassName\":\"java.lang.Float\"," +
    		             "\"tagValue\":3.34535," +
    		             "\"mode\":\"TEST\"," +
    		             "\"simulated\":false," +
    		             "\"alarmValues\":[]," +
    		             "\"tagQuality\":{\"invalidQualityStates\":{\"PROCESS_DOWN\":\"Process Down\"},\"isValid\":false}," +
    		             "\"description\":\"Test transfer tag\"," +
    		             "\"valueDescription\":\"Test val desc\"," +
    		             "\"sourceTimestamp\":1343812635352," +
    		             "\"serverTimestamp\":1343812635352," +
    		             "\"reportType\":\"RESULT\"," +
    		             "\"totalOperations\":0," +
    		             "\"currentOperation\":0," +
    		             "\"totalParts\":0," +
    		             "\"currentPart\":0}";
    
    TagUpdate receivedTag = TransferTagImpl.fromJson(encoded);
    assertEquals(receivedTag.getId(), Long.valueOf(1234));
    assertEquals(receivedTag.getProcessIds().size(), 2);
    assertTrue(receivedTag.getProcessIds().contains(123L));
    assertTrue(receivedTag.getProcessIds().contains(3214L));
    assertEquals(receivedTag.getEquipmentIds().size(), 2);
    assertTrue(receivedTag.getEquipmentIds().contains(4234L));
    assertTrue(receivedTag.getEquipmentIds().contains(234L));
    assertEquals(receivedTag.getTopicName(), "topic:name");
    assertEquals(receivedTag.getName(), "tag:name");
    assertEquals(((TransferTagValueImpl)receivedTag).getValueClassName(), "java.lang.Float");
    assertEquals(receivedTag.getValue(), Float.valueOf("3.34535"));
    assertEquals(receivedTag.getMode(), TagMode.TEST);
    assertEquals(receivedTag.getAlarms().size(), 0);
    assertEquals(receivedTag.isSimulated(), false);
    assertEquals(receivedTag.getDescription(), "Test transfer tag");
    assertEquals(((TransferTagValueImpl)receivedTag).isResult(), true);
    assertEquals(((TransferTagValueImpl)receivedTag).isErrorReport(), false);
    assertEquals(((TransferTagValueImpl)receivedTag).isProgressReport(), false);
    assertTrue(!receivedTag.getDataTagQuality().isValid());
    assertEquals(receivedTag.getDataTagQuality().isInvalidStatusSet(TagQualityStatus.PROCESS_DOWN), true);    
  }
  
  /**
   * Same test but for String encoding.
   */
  @Test
  public void testStringBackwardsCompatible() {
    String encoded = "{\"processIds\":[123,3214]," +
                     "\"equipmentIds\":[4234,234]," +
                     "\"topicName\":\"topic:name\"," +
                     "\"tagName\":\"tag:name\"," +
                     "\"tagId\":1234," +
                     "\"valueClassName\":\"java.lang.String\"," +
                     "\"tagValue\":\"This is a test String value message!\"," +
                     "\"mode\":\"TEST\"," +
                     "\"simulated\":false," +
                     "\"alarmValues\":[]," +
                     "\"tagQuality\":{\"invalidQualityStates\":{\"PROCESS_DOWN\":\"Process Down\"},\"isValid\":false}," +
                     "\"description\":\"Test transfer tag\"," +
                     "\"valueDescription\":\"Test val desc\"," +
                     "\"sourceTimestamp\":1343812635352," +
                     "\"serverTimestamp\":1343812635352," +
                     "\"reportType\":\"RESULT\"," +
                     "\"totalOperations\":0," +
                     "\"currentOperation\":0," +
                     "\"totalParts\":0," +
                     "\"currentPart\":0}";
    
    TagUpdate receivedTag = TransferTagImpl.fromJson(encoded);
    assertEquals(((TransferTagValueImpl)receivedTag).getValueClassName(), "java.lang.String");
    assertEquals(receivedTag.getValue(), "This is a test String value message!");    
  }
  
  @Test
  public void testIntegerJsonMsg() {
    String gsonString = createTagForValue(Integer.valueOf(3)).toJson();
    TagUpdate receivedTag = TransferTagImpl.fromJson(gsonString);
    assertTrue(receivedTag.getValue() instanceof Integer);
  }
  
  @Test
  public void testLongJsonMsg() {
    String gsonString = createTagForValue(Long.valueOf(23453534634563246L)).toJson();
    TagUpdate receivedTag = TransferTagImpl.fromJson(gsonString);  
    assertTrue(receivedTag.getValue() instanceof Long);
  }
  
  @Test
  public void testDoubleJsonMsg() {
    Double value = Double.valueOf(2345356324.3245325D);
    String gsonString = createTagForValue(value).toJson();
    TagUpdate receivedTag = TransferTagImpl.fromJson(gsonString); 
    assertTrue(receivedTag.getValue() instanceof Double);
    assertEquals(value, receivedTag.getValue());
  }
  
  @Test
  public void testShortJsonMsg() {
    String gsonString = createTagForValue(Short.valueOf("077")).toJson();
    TagUpdate receivedTag = TransferTagImpl.fromJson(gsonString);
    assertTrue(receivedTag.getValue() instanceof Short);
  }
  
  @Test
  public void testByteJsonMsg() {
    String gsonString = createTagForValue(Byte.valueOf((byte) 0x000A)).toJson();
    TagUpdate receivedTag = TransferTagImpl.fromJson(gsonString);
    assertTrue(receivedTag.getValue() instanceof Byte);
  }
  
  @Test
  public void testBooleanJsonMsg() {
    String gsonString = createTagForValue(Boolean.TRUE).toJson();
    TagUpdate receivedTag = TransferTagImpl.fromJson(gsonString);
    assertTrue(receivedTag.getValue() instanceof Boolean);
  }
  
  @Test
  public void testStringJsonMsg() {
    String gsonString = createTagForValue("This is a test String value message!").toJson();    
    TagUpdate receivedTag = TransferTagImpl.fromJson(gsonString);
    assertTrue(receivedTag.getValue() instanceof String);
  }
  
  @Test
  public void testRuleIdsTransfer() {
    TransferTagImpl originalTransferTag = createTagForValue(Long.valueOf(234533246L));
    String gsonString = originalTransferTag.toJson();
    TagUpdate receivedTag = TransferTagImpl.fromJson(gsonString);
  }
  
  @Test
  public void testPublicationsTransfer() {
    TransferTagImpl originalTransferTag = createTagForValue(Long.valueOf(444L));
    String gsonString = originalTransferTag.toJson();
    TagUpdate receivedTag = TransferTagImpl.fromJson(gsonString);
  }
  
}
