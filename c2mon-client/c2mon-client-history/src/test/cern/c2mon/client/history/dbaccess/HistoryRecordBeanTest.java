package cern.c2mon.client.history.dbaccess;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.history.dbaccess.beans.HistoryRecordBean;
import cern.c2mon.client.history.dbaccess.util.BeanConverterUtil;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.datatag.TagQualityStatus;

/**
 * Tests the {@link HistoryRecordBean}
 * 
 * @author vdeila
 *
 */
public class HistoryRecordBeanTest {

  /**
   * Class to test
   */
  private HistoryRecordBean bean;
  
  private static final Long TAGID = 65468L; 
  private static final Long TAG_TIME = 664946519L;
  private static final Long DAQ_TIME = 654653452L;
  private static final Long LOG_TIME = 654654121L;
  private static final Long SERVER_TIME = 654654654L;
  private static final boolean FROM_INITIAL_SNAPSHOT = false;
  private static final String TAG_VALUE = "The tag value";
  private static final String TAG_VALUE_DESCRIPTION = "The tag value description";  
  private static final String TAG_DATA_TYPE = "String";
  private static final short TAG_MODE = 1;
  private static final String TAG_NAME = "A name-2345-dsfh";
  private static final TagQualityStatus TAG_QUALITY_STATUS = TagQualityStatus.PROCESS_DOWN;
  
  @Before
  public void setUp() throws Exception {
    bean = new HistoryRecordBean(TAGID);
    bean.convertIntoLocalTimeZone();
    bean.setFromInitialSnapshot(FROM_INITIAL_SNAPSHOT);
    bean.setTagTime(new Timestamp(TAG_TIME));
    bean.setDaqTime(new Timestamp(DAQ_TIME));
    bean.setLogDate(new Timestamp(LOG_TIME));
    bean.setServerTime(new Timestamp(SERVER_TIME));
    bean.setTagValue(TAG_VALUE);
    bean.setTagValueDesc(TAG_VALUE_DESCRIPTION);
    bean.setTagDataType(TAG_DATA_TYPE);
    bean.setTagMode(TAG_MODE);
    bean.setTagName(TAG_NAME);
    bean.setDataTagQuality(new DataTagQualityImpl(TAG_QUALITY_STATUS, "A hardcoded description"));
  }

  @Test
  public void testHistoryRecordBean() {
    assertEquals(TAGID, bean.getTagId());
    assertEquals(FROM_INITIAL_SNAPSHOT, bean.isFromInitialSnapshot());
    assertEquals(TAG_TIME, Long.valueOf(bean.getTagTime().getTime()));
    assertEquals(DAQ_TIME, Long.valueOf(bean.getDaqTime().getTime()));
    assertEquals(LOG_TIME, Long.valueOf(bean.getLogDate().getTime()));
    assertEquals(SERVER_TIME, Long.valueOf(bean.getServerTime().getTime()));
    assertEquals(TAG_VALUE, bean.getTagValue());    
    assertEquals(TAG_VALUE_DESCRIPTION, bean.getTagValueDesc());    
    assertEquals(TAG_DATA_TYPE, bean.getTagDataType());
    assertEquals(TAG_MODE, bean.getTagMode());
    assertEquals(TAG_NAME, bean.getTagName());
    assertEquals(true, bean.getDataTagQuality().isInvalidStatusSet(TAG_QUALITY_STATUS));
  }
  
  @Test
  public void testToTagValueUpdate() {
    final TagValueUpdate value = BeanConverterUtil.toTagValueUpdate(bean, null);
    assertEquals(TAGID, value.getId());
    assertEquals(TAG_TIME, Long.valueOf(value.getSourceTimestamp().getTime()));
    assertEquals(SERVER_TIME, Long.valueOf(value.getServerTimestamp().getTime()));
    assertEquals(TAG_VALUE, value.getValue());
    assertEquals(true, value.getDataTagQuality().isInvalidStatusSet(TAG_QUALITY_STATUS));
  }
}
