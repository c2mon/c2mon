package cern.c2mon.web.configviewer.statistics;

import static org.junit.Assert.assertNotNull;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.web.configviewer.statistics.daqlog.DAQLogStatisticsMapper;


/**
 * Tests the DB connection starts up correctly.
 *
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/web/configviewer/statistics/config/c2mon-statistics-generator-test.xml"})
public class DbConnectionTest {

  @Autowired
  DAQLogStatisticsMapper mapper;

  //@Test
  public void initSqlMap() {
    assertNotNull(mapper);
    mapper.getBarChartData("VSTAT_DAQ_AS");
  }

}
