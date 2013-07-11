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
package cern.c2mon.client.ext.history.dbaccess;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import cern.c2mon.client.ext.history.dbaccess.HistoryMapper;

/**
 * Used for testing purposes
 * 
 * @author vdeila
 *
 */
public class FakeSqlSession implements SqlSession {

  public FakeSqlSession() {
    
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getMapper(Class<T> type) {
    if (type.equals(HistoryMapper.class)) {
      final HistoryMapper mapper = new FakeHistoryMapperImpl();
      return (T) mapper;
    }
    
    throw new RuntimeException("Mapper \"" + type.getName() + "\" is not supported");
  }
  
  @Override
  public void clearCache() {
    
  }

  @Override
  public void close() {
    
  }

  @Override
  public void commit() {
    
  }

  @Override
  public void commit(boolean force) {
    
  }

  @Override
  public int delete(String statement) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public int delete(String statement, Object parameter) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public Configuration getConfiguration() {
    return null;
  }

  @Override
  public Connection getConnection() {
    throw new RuntimeException("This method is not supported");
  }
  
  @Override
  public int insert(String statement) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public int insert(String statement, Object parameter) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public void rollback() {
    throw new RuntimeException("This method is not supported");

  }

  @Override
  public void rollback(boolean force) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public void select(String statement, ResultHandler handler) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public void select(String statement, Object parameter, ResultHandler handler) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public List< ? > selectList(String statement) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public List< ? > selectList(String statement, Object parameter) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public List< ? > selectList(String statement, Object parameter, RowBounds rowBounds) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public Map< ?, ? > selectMap(String statement, String mapKey) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public Map< ?, ? > selectMap(String statement, Object parameter, String mapKey) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public Map< ?, ? > selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public Object selectOne(String statement) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public Object selectOne(String statement, Object parameter) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public int update(String statement) {
    throw new RuntimeException("This method is not supported");
  }

  @Override
  public int update(String statement, Object parameter) {
    throw new RuntimeException("This method is not supported");
  }

}
