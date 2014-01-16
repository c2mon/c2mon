package cern.c2mon.server.cache.datatag;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagConstants;

/**
 * Unit test of DataTagCacheObjectFacadeImpl
 * 
 * @author Mark Brightwell
 *
 */
public class DataTagCacheObjectFacadeImplTest {

  /**
   * To test
   */
  private DataTagCacheObjectFacadeImpl facade;
  
  @Before
  public void setUp() {
    facade = new DataTagCacheObjectFacadeImpl();
  }
  
  @Test
  public void testValueDescriptionTruncation() {    
    DataTagCacheObject dataTag = new DataTagCacheObject(Long.valueOf(2), "test name", "Float", DataTagConstants.MODE_OPERATIONAL);
    
    //provided description from source
    char[] chars = new char[DataTagCacheObjectFacade.MAX_DESC_LENGTH + 1];
    Arrays.fill(chars, 'v');
    chars[DataTagCacheObjectFacade.MAX_DESC_LENGTH] = 'a';
    String valueDesc = new String(chars);
    
    //expected description in tag
    char[] charsTag = new char[DataTagCacheObjectFacade.MAX_DESC_LENGTH];
    Arrays.fill(charsTag, 'v');  
    String takenDesc = new String(charsTag);
    
    facade.updateValue(dataTag, 1f, valueDesc);
    
    assertEquals(dataTag.getValueDescription(), takenDesc);
    
    
  }
  
}
