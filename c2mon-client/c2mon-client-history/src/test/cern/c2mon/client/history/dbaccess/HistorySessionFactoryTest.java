package cern.c2mon.client.history.dbaccess;

import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistoryProviderType;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.common.history.SupervisionEventRequest;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.history.HistoryProviderFactory;
import cern.c2mon.client.history.dbaccess.exceptions.HistoryException;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionEntity;

/**
 * Tests {@link HistoryProviderFactory} and the myBatis configurations. Tries to
 * retrieve something from the database to test the sql commands.
 * 
 * @author vdeila
 * 
 */
public class HistorySessionFactoryTest {

  private static final Long TAG_IDS[] = new Long[] { 44029L, 44031L, 44033L, 44034L, 46381L, 100523L };

  private static final int MAXIMUM_TOTAL_RECORDS = 10;

  /** The TIMESPAN which will be used for the queries */
  private static final Timespan TIMESPAN = 
    new Timespan(
        new GregorianCalendar(2011, 06, 05, 12, 00).getTime(),
        new GregorianCalendar(2011, 06, 07, 12, 00).getTime());
  
  /** Supervision events to request */
  private static final Collection<SupervisionEventRequest> SUPERVISION_REQUESTS =
    Arrays.asList(new SupervisionEventRequest[] {
        new SupervisionEventRequest(5083L, SupervisionEntity.EQUIPMENT),
        new SupervisionEventRequest(5084L, SupervisionEntity.EQUIPMENT),
        new SupervisionEventRequest(5072L, SupervisionEntity.EQUIPMENT),
        new SupervisionEventRequest(4038L, SupervisionEntity.PROCESS)
    }); 
  
  @Test
  public void testCreateHistoryProvider() throws HistoryException {
    HistoryProviderFactory.getInstance().createHistoryProvider(HistoryProviderType.HISTORY_SHORT_TERM_LOG);
  }

  @Test
  public void testRequestFromTest() throws HistoryException {
    final HistoryProvider provider = HistoryProviderFactory.getInstance().createHistoryProvider(HistoryProviderType.HISTORY_SHORT_TERM_LOG);

    final Collection<HistoryTagValueUpdate> values = provider.getHistory(TAG_IDS, MAXIMUM_TOTAL_RECORDS);
    Assert.assertTrue("More records was returned than requested", values.size() <= MAXIMUM_TOTAL_RECORDS);
    
    provider.getInitialValuesForTags(TAG_IDS, TIMESPAN.getStart());
    provider.getInitialSupervisionEvents(TIMESPAN.getStart(), SUPERVISION_REQUESTS);
    provider.getSupervisionEvents(TIMESPAN.getStart(), TIMESPAN.getEnd(), SUPERVISION_REQUESTS);
  }

}
