package cern.c2mon.server.shorttermlog.management;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

/**
 * JMX bean for managing the STL datasource.
 * 
 * @author Mark Brightwell
 *
 */
@Service
@ManagedResource(objectName="cern.c2mon:type=datasource,name=shortTermLogManagement")
public class ShortTermLogManagement {

  /**
   * The cache datasource to close down.
   */
  private BasicDataSource stlDataSource;
  
  /**
   * Autowired constructor.
   * @param stlDataSource the short-term-log datasource
   */
  @Autowired
  public ShortTermLogManagement(@Qualifier("stlDataSource") BasicDataSource stlDataSource) {
    super();
    this.stlDataSource = stlDataSource;
  }
  
  /**
   * For management only.
   * @return the number of active DB connections in the short-term-log datasource pool
   */
  @ManagedOperation(description="The number of active DB connections in the short-term-log datasource pool.")
  public int getNumActiveDbConnections() {
    return stlDataSource.getNumActive();
  }
  
}
