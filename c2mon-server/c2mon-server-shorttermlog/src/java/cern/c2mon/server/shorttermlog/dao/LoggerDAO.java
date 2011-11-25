/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.shorttermlog.dao;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.shorttermlog.mapper.LoggerMapper;

/**
 * Common DAO implementation for objects that need storing in a STL table using
 * the fallback mechanism.
 * 
 * @author Mark Brightwell
 *
 * @param <T> the object that is being logged in the STL table
 */
public class LoggerDAO<T extends IFallback> implements IDBPersistenceHandler {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(LoggerDAO.class);
  
  /** 
   * Maximum number of statements that will be executed in each SQL batch 
   **/
  private static final int RECORDS_PER_BATCH = 500;
  
  /**
   * The iBatis factory used to acquire database sessions.
   */
  private SqlSessionFactory sqlSessionFactory;
    
  /**
   * Auto-commit mapper for single queries.
   */
//  private LoggerMapper<T> loggerMapper;
  
  /**
   * The mapper class name used for creating the batch logger mapper from the session.
   */
  private Class< ? extends LoggerMapper<T>> mapperInterface;
    
  /**
   * 
   * @param sqlSessionFactory
   * @param loggerMapper the auto-commit mapper for single queries/inserts
   * @throws ClassNotFoundException 
   */
  public LoggerDAO(SqlSessionFactory sqlSessionFactory, String mapperInterface) throws ClassNotFoundException {
    super();
    this.sqlSessionFactory = sqlSessionFactory;   
    Class< ? > tmpInterface = Class.forName(mapperInterface);
    if (LoggerMapper.class.isAssignableFrom(tmpInterface)) {
      this.mapperInterface = (Class<? extends LoggerMapper<T>>) tmpInterface;
    } else {
      throw new IllegalArgumentException("Unexpected class name passed to LoggerDAO constructor - unable to instantiate.");
    }

//    if (tmpInterface instanceof LoggerMapper) {
//      Class<? extends LoggerMapper> mappper = tmpInterface;
//    }
  }

  /**
   * Inserts into the database a set of rows containing the data coming in
   * several IFallback objects
   * 
   * @param data
   *            List of IFallback object whose data has to be inserted in the
   *            DB
   * @throws IDBPersistenceException
   *             An exception is thrown in case an error occurs during the
   *             data insertion. The exception provides also the number of
   *             already committed objects
   */  
  @SuppressWarnings("unchecked") //add generics to persistence manager
  public final void storeData(final List data) throws IDBPersistenceException {
      SqlSession session = null;
      int size = data.size();
      int commited = 0;
      T dtShortTermLog;

      try {
          // We use batch set of statements to improve performance
          session = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Obtained batch transacted SQL session (session: " + session.toString() + ")");
          }        
          LoggerMapper<T> persistenceMapper = session.getMapper(mapperInterface);
          
          // Iterate through the list of DataTagCacheObjects to insert
          // them one by one
          for (int i = 0; i != size; i++) {
              if ((0 == i % RECORDS_PER_BATCH) && i > 0) {
                  if (LOGGER.isDebugEnabled()) {
                      LOGGER.debug("storeData([Collection]) : Commiting rows for i=" + i);
                  }
                  session.commit();                  
                  commited = i;                  
              }
                  
              if (data.get(i) != null) {
                  dtShortTermLog = (T) data.get(i);
                  if (LOGGER.isDebugEnabled()) {
                      LOGGER.debug("Logging object with ID: " + dtShortTermLog.getId());
                  }
                  persistenceMapper.insertLog(dtShortTermLog);                  
              }
          }
          // Commit the transaction
          session.commit();
          commited = size;          
      } catch (DataAccessException e) {
          LOGGER.error(
                  "storeData([Collection]) : Error executing/closing prepared statement for "
                  + data.size() + " dataTags", e);
          try {
              session.rollback();
              session.close();
          } catch (Exception sql) {
              LOGGER
              .error(
                      "storeData([Collection]) : Error rolling back transaction.",
                      sql);
          }
          throw new IDBPersistenceException(e.getMessage(), commited);            
      } finally {
          try {
              session.rollback();
              session.close();
          } catch (Exception e) {
              LOGGER.error("storeData([Collection]) : Error rolling back transaction.", e);
          }
      }
  }

  @Override
  public String getDBInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @Transactional("stlTransactionManager")
  public void storeData(IFallback object) throws SQLException {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      LoggerMapper<T> loggerMapper = session.getMapper(mapperInterface);
      loggerMapper.insertLog((T) object);
      session.commit();
    } catch (DataAccessException e) {
      String message = "Exception caught while writing to short-term-log";
      LOGGER.error(message, e);
      session.rollback();
      throw new SQLException(message, e);
    } finally {
      session.close();
    }
   
  }
  
}


  
