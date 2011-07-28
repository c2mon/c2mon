package cern.c2mon.client.history.dbaccess;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  HistoryRecordBeanTest.class,
  SqlHistoryProviderDAOTest.class,
  HistorySessionFactoryTest.class
})
public class AllTests {

}
