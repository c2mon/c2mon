package cern.c2mon.server.test;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/test/server-test-data.xml",
                       "classpath:cern/c2mon/server/test/server-test-properties.xml"})
@TransactionConfiguration(transactionManager = "testTransactionManager", defaultRollback = true)
@Transactional("testTransactionManager")
public class TestDataInserterTest {

  @Autowired
  private TestDataInserter testDataInserter;
  
  @Test 
  @DirtiesContext
  public void testDataRemove() throws IOException {
    testDataInserter.removeTestData();
  }
  
  @Test
  @DirtiesContext
  public void testDataInsert() throws IOException {
    testDataInserter.removeTestData();
    testDataInserter.insertTestData();
  }
  
}
