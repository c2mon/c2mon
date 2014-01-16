package cern.c2mon.server.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class TestDataInserterImpl implements TestDataInserter {
  
  private Logger LOGGER = Logger.getLogger(TestDataInserterImpl.class);
  
  private JdbcTemplate jdbcTemplate;
  
  private BufferedReader sqlInsert;
  
  private BufferedReader sqlRemove;
  
  /**
   * Loads the data from file.
   */  
  public void init() {
    sqlInsert = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/cern/c2mon/server/test/sql/cache-data-insert.sql")));
    sqlRemove = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/cern/c2mon/server/test/sql/cache-data-remove.sql")));
  }
  
  @Override
  @Transactional("testTransactionManager")
  public void insertTestData() throws IOException {
   runStatements(sqlInsert);
  }

  @Override
  @Transactional("testTransactionManager")
  public void removeTestData() throws IOException {
    runStatements(sqlRemove);
  }

  /**
   * Used to set data source in JdbcTemplate
   * @param dataSource
   */
  @Required
  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }
  
  private void runStatements(BufferedReader reader) throws IOException {
    String line = getLine(reader);
    while (line != null && !line.isEmpty()) {
      jdbcTemplate.update(line);
      line = getLine(reader);
    }
  }
  
  private String getLine(BufferedReader reader) throws IOException {
    boolean statementComplete = false;
    StringBuilder finalStatement = new StringBuilder();
    while (!statementComplete) {
      String line = reader.readLine();    
      if (line != null) {
        line = line.trim();
        if (line.startsWith("--")) { //comment
          line = "";
        }
        if (line.endsWith(";")) {          
          line = line.substring(0, line.length()-1);          
          statementComplete = true;
        } 
        finalStatement.append(" ").append(line);
      } else {
        statementComplete = true;
      }
    }        
    LOGGER.debug("Running: " + finalStatement);
    return finalStatement.toString();
  }
  
}
