package cern.c2mon.server.test;

import java.io.IOException;

/**
 * Bean available for inserting/removing a set of test data into a database.
 * 
 * @author Mark Brightwell
 *
 */
public interface TestDataInserter {

  /**
   * Inserts the data. Run the removal method first if
   * the data may be partially present already.
   * @throws IOException 
   */
  void insertTestData() throws IOException;
  
  /**
   * Removes the data.
   * @throws IOException 
   */
  void removeTestData() throws IOException;
  
}
