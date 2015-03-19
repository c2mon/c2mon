package cern.c2mon.statistics.generator;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the DB connection starts up correctly.
 *
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/statistics/generator/config/c2mon-statistics-generator.xml"})
public class DbConnectionTest {

  @Autowired
  StatisticsMapper mapper;

  @Test
  public void initSqlMap() {
    assertNotNull(mapper);
    mapper.getBarChartData("VSTAT_DAQ_AS");
  }

}
