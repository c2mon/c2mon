package cern.c2mon.client.ext.history.dbaccess;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * All the tests in the package
 * 
 * @author vdeila
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  HistoryRecordBeanTest.class,
  HistorySessionFactoryTest.class
})
public class AllDbAccessTests {

}
