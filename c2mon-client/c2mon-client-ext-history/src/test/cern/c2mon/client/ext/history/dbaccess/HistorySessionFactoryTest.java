package cern.c2mon.client.ext.history.dbaccess;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

import cern.c2mon.client.ext.history.HistoryProviderFactoryImpl;
import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.SupervisionEventRequest;
import cern.c2mon.client.ext.history.common.Timespan;
import cern.c2mon.client.ext.history.common.exception.HistoryProviderException;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionEntity;

/**
 * Tests {@link HistoryProviderFactoryImpl} and the myBatis configurations. Tries to
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
  public void testCreateHistoryProvider() throws HistoryProviderException {
    HistoryProvider provider = new HistoryProviderFactoryImpl().createHistoryProvider();
  }

  @Test
  public void testRequestFromTest() throws HistoryProviderException {
    final HistoryProvider provider = new HistoryProviderFactoryImpl().createHistoryProvider();

    final Collection<HistoryTagValueUpdate> values = provider.getHistory(TAG_IDS, MAXIMUM_TOTAL_RECORDS);
    assertTrue("More records was returned than requested", values.size() <= MAXIMUM_TOTAL_RECORDS);
    
    provider.getInitialValuesForTags(TAG_IDS, TIMESPAN.getStart());
    provider.getInitialSupervisionEvents(TIMESPAN.getStart(), SUPERVISION_REQUESTS);
    provider.getSupervisionEvents(TIMESPAN.getStart(), TIMESPAN.getEnd(), SUPERVISION_REQUESTS);
  }

}
