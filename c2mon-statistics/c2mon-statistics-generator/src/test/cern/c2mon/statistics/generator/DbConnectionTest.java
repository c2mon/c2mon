package cern.c2mon.statistics.generator;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Tests the DB connection starts up correctly.
 * 
 * @author Mark Brightwell
 *
 */
public class DbConnectionTest {

  @Test
  public void initSqlMap() {
    SqlMapClient client = SqlMapper.getSqlMapInstance();
    assertNotNull(client);
  }
  
}
