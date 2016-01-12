/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.ext.history.dbaccess;

import java.sql.Connection;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;

/**
 * Used for testing purposes
 * 
 * @author vdeila
 *
 */
public class FakeSqlSessionFactory implements SqlSessionFactory {

  /** The sql session returned */
  private final SqlSession sqlSession;
  
  public FakeSqlSessionFactory() {
    this.sqlSession = new FakeSqlSession(); 
  }

  @Override
  public Configuration getConfiguration() {
    return null;
  }

  @Override
  public SqlSession openSession() {
    return sqlSession;
  }

  @Override
  public SqlSession openSession(boolean autoCommit) {
    return sqlSession;
  }

  @Override
  public SqlSession openSession(Connection connection) {
    return sqlSession;
  }

  @Override
  public SqlSession openSession(TransactionIsolationLevel level) {
    return sqlSession;
  }

  @Override
  public SqlSession openSession(ExecutorType execType) {
    return sqlSession;
  }

  @Override
  public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
    return sqlSession;
  }

  @Override
  public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
    return sqlSession;
  }

  @Override
  public SqlSession openSession(ExecutorType execType, Connection connection) {
    return sqlSession;
  }

}
