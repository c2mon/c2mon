package cern.c2mon.server.shorttermlog.structure;

import static org.junit.Assert.*;

import org.apache.ibatis.migration.commands.NewCommand;
import org.junit.Test;

import cern.tim.server.common.datatag.DataTagCacheObject;
import cern.tim.shared.common.datatag.TagQualityStatus;

/**
 * Unit test of DataTagShortTermLogConverter.
 * 
 * @author Mark Brightwell
 *
 */
public class ConverterTest {

  /**
   * Class to test.
   */
  private DataTagShortTermLogConverter converter = new DataTagShortTermLogConverter();
  
  /**
   * Tests the quality code is correctly generated.
   */
  @Test
  public void testCodeGeneration() {
    DataTagCacheObject tag = new DataTagCacheObject(10L);
    tag.getDataTagQuality().addInvalidStatus(TagQualityStatus.INACCESSIBLE, "desc1");
    tag.getDataTagQuality().addInvalidStatus(TagQualityStatus.UNINITIALISED, "desc1");
    tag.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_OUT_OF_BOUNDS, "desc1");
    assertNotNull(tag.getDataTagQuality());
    Loggable loggable = converter.convertToLogged(tag);
    TagShortTermLog logTag = (TagShortTermLog) loggable;
    assertEquals(11, logTag.getTagQualityCode());
  }
  
}
