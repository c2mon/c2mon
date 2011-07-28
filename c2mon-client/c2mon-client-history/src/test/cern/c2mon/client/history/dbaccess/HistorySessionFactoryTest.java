package cern.c2mon.client.history.dbaccess;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistoryProviderType;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.history.HistoryProviderFactory;
import cern.c2mon.client.history.dbaccess.exceptions.HistoryException;

/**
 * Tests {@link HistoryProviderFactory} and the myBatis configurations. Tries to
 * retrieve something from the database.
 * 
 * @author vdeila
 * 
 */
public class HistorySessionFactoryTest {

  private static final Long TAG_IDS[] = new Long[] { 44029L, 44031L, 44033L, 44034L, 46381L, 100523L };

  private static final int MAXIMUM_TOTAL_RECORDS = 10;

  @Test
  public void testCreateHistoryProvider() throws HistoryException {
    HistoryProviderFactory.getInstance().createHistoryProvider(HistoryProviderType.HISTORY_SHORT_TERM_LOG);
  }

  @Test
  public void testRequestFromTest() throws HistoryException {
    final HistoryProvider provider = HistoryProviderFactory.getInstance().createHistoryProvider(HistoryProviderType.HISTORY_SHORT_TERM_LOG);

    final Collection<HistoryTagValueUpdate> values = provider.getHistory(TAG_IDS, MAXIMUM_TOTAL_RECORDS);
    Assert.assertTrue("More records was returned than requested", values.size() <= MAXIMUM_TOTAL_RECORDS);
  }

}
