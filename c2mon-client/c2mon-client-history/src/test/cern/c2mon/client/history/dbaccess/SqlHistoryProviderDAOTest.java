package cern.c2mon.client.history.dbaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;

/**
 * Tests for the {@link SqlHistoryProviderDAO} class
 * 
 * @author vdeila
 * 
 */
public class SqlHistoryProviderDAOTest {

  private HistoryProvider provider;

  private Timestamp shortFromTime;
  private Timestamp fromTime;
  private Timestamp toTime;

  private static final int MAXIMUM_NUMBER_OF_RECORDS = 4000;

  @Before
  public void setUp() throws Exception {
    provider = new SqlHistoryProviderDAO(new FakeSqlSessionFactory(), null);
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    toTime = new Timestamp(calendar.getTimeInMillis());
    calendar.add(Calendar.MINUTE, -10);
    shortFromTime = new Timestamp(calendar.getTimeInMillis());
    calendar.add(Calendar.DATE, -5);
    calendar.add(Calendar.HOUR_OF_DAY, -4);
    fromTime = new Timestamp(calendar.getTimeInMillis());
  }

  private Long[] getTagIds(final int count) {
    final Long[] tagIds = new Long[count];
    for (int i = 0; i < count; i++) {
      tagIds[i] = Long.valueOf(i + 5000000L);
    }
    return tagIds;
  }

  @Test
  public void testGetHistoryMaximumNumberOfRecords() {
    final Collection<HistoryTagValueUpdate> values = provider.getHistory(getTagIds(15000), MAXIMUM_NUMBER_OF_RECORDS);
    assertEquals(MAXIMUM_NUMBER_OF_RECORDS, values.size());
  }

  @Test
  public void testGetHistoryMaximumNumberOfRecordsPerTag() {
    final Long[] tagIds = getTagIds(100);
    final Collection<HistoryTagValueUpdate> values = provider.getHistory(MAXIMUM_NUMBER_OF_RECORDS, tagIds);
    assertEquals(MAXIMUM_NUMBER_OF_RECORDS * tagIds.length, values.size());
  }
  
  @Test
  public void testGetHistoryFromToMaximumNumberOfRecordsPerTag() {
    final Long[] tagIds = getTagIds(100);
    final Collection<HistoryTagValueUpdate> values = provider.getHistory(5, tagIds, fromTime, toTime);
    assertTrue("More tags were returned than asked for", 5 * tagIds.length >= values.size());
  }

  @Test
  public void testGetHistoryFromToMaximumNumberOfRecords() {
    final Long[] tagIds = getTagIds(15000);
    final Collection<HistoryTagValueUpdate> values = provider.getHistory(tagIds, fromTime, toTime, MAXIMUM_NUMBER_OF_RECORDS);

    assertTrue(String.format("More records is returned (%d) than the maximum number of records (%d)", values.size(), MAXIMUM_NUMBER_OF_RECORDS),
        values.size() <= MAXIMUM_NUMBER_OF_RECORDS);
  }

  @Test
  public void testGetHistoryFromToOverTagLimit() {
    final Long[] tagIds = getTagIds(35000);
    final Collection<HistoryTagValueUpdate> values = provider.getHistory(tagIds, shortFromTime, toTime);
    final Set<Long> uniqueTagIds = new HashSet<Long>(tagIds.length);
    for (final HistoryTagValueUpdate value : values) {
      uniqueTagIds.add(value.getId());
    }
    
    assertEquals(tagIds.length, uniqueTagIds.size());
  }

  @Test
  public void testGetInitialValuesForTags() {
    final Long[] tagIds = getTagIds(15000);
    final Collection<HistoryTagValueUpdate> values = provider.getInitialValuesForTags(tagIds, fromTime);

    assertEquals(tagIds.length, values.size());
  }
  
}
