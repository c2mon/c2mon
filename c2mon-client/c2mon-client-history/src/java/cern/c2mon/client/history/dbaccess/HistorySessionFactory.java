/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.history.dbaccess;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.history.ClientDataTagRequestCallback;
import cern.c2mon.client.history.dbaccess.exceptions.HistoryException;

/**
 * Factory to retrieve a {@link SqlSessionFactory}, {@link SqlSession}
 * or a {@link HistoryProvider}.<br/>
 * You can check if the data source is available by calling the
 * <code>isDatasourceAvailable()</code> function
 * 
 * @author vdeila
 */
public final class HistorySessionFactory {

  /** The default driver used if non is specified */
  private static final String DEFAULT_JDBC_TIMSTLOG_DRIVER = "oracle.jdbc.OracleDriver";
  
  /** Singleton instance */
  private static HistorySessionFactory instance = null;

  /**
   * @return An instance
   */
  public static HistorySessionFactory getInstance() {
    if (instance == null) {
      instance = new HistorySessionFactory();
    }
    return instance;
  }

  /**
   * Is Singleton and therefore private
   */
  private HistorySessionFactory() {
    if (System.getProperty(HistorySystemProperties.JDBC_RO_DRIVER) == null) {
      System.setProperty(HistorySystemProperties.JDBC_RO_DRIVER, DEFAULT_JDBC_TIMSTLOG_DRIVER);
    }
  }

  /** The file path to the ibatis configuration file */
  private static final String CONFIGURATION_FILE = "cern/c2mon/client/history/dbaccess/config/history-ibatis.xml";

  /** Session factory is the factory from apache created from the xml files */
  private SqlSessionFactory sessionFactory = null;

  /**
   * 
   * @return An instance of the {@link SqlSessionFactory} which is used to do
   *         queries against the sql database
   * @throws HistoryException
   *           If the configuration file could not be read. Or if the system
   *           properties for the data source is not set.
   */
  public SqlSessionFactory getSqlSessionFactory() throws HistoryException {
    if (sessionFactory == null) {
      // Checks if the data source is available
      if (isDatasourceAvailable()) {
        // Creates a new SqlSessionFactory with the data source properties
        Reader reader = null;
        try {
          reader = Resources.getResourceAsReader(CONFIGURATION_FILE);
          sessionFactory = new SqlSessionFactoryBuilder().build(reader, System.getProperties());
        }
        catch (IOException e) {
          throw new HistoryException("Error while reading the iBatis configurations..", e);
        }
      }
      else {
        // Throws an exception as the data source is not available
        throw new HistoryException("The system properties for the TIM data source is not available. Please contact TIM support.");
      }
    }
    return sessionFactory;
  }

  /**
   * 
   * @return An open session. Must be closed when finished using it.
   * @throws HistoryException
   *           If the configuration file could not be read. Or if the system
   *           properties for the data source is not set.
   */
  public SqlSession openSession() throws HistoryException {
    return getSqlSessionFactory().openSession();
  }

  /**
   * 
   * @param clientDataTagRequestCallback
   *          callback for the history provider to get access to attributes in
   *          the {@link ClientDataTagValue}. Like the
   *          {@link ClientDataTagValue#getType()}.
   * 
   * @return A {@link HistoryProvider} which can be used to easily get history
   *         data
   * @throws HistoryException
   *           If the configuration file could not be read. Or if the system
   *           properties for the data source is not set.
   */
  public HistoryProvider createHistoryProvider(final ClientDataTagRequestCallback clientDataTagRequestCallback) throws HistoryException {
    return new SqlHistoryProviderDAO(getSqlSessionFactory(), clientDataTagRequestCallback);
  }
  
  /**
   * 
   * @param clientDataTagRequestCallback
   *          callback for the history provider to get access to attributes in
   *          the {@link ClientDataTagValue}. Like the
   *          {@link ClientDataTagValue#getType()}.
   *          
   * @return A {@link HistoryProvider} which can be used to easily get event history data
   * @throws HistoryException
   *           If the configuration file could not be read. Or if the system
   *           properties for the data source is not set.
   */
  public HistoryProvider createHistoryEventsProvider(final ClientDataTagRequestCallback clientDataTagRequestCallback) throws HistoryException {
    throw new HistoryException("The History Events is not yet supported..");
  }

  /**
   * 
   * @return <code>true</code> if the data source connection string is available
   */
  public boolean isDatasourceAvailable() {
    return 
      System.getProperty(HistorySystemProperties.JDBC_RO_DRIVER) != null
      && System.getProperty(HistorySystemProperties.JDBC_RO_URL) != null
      && System.getProperty(HistorySystemProperties.JDBC_RO_USERNAME) != null
      && System.getProperty(HistorySystemProperties.JDBC_RO_PASSWORD) != null;
  }

}
