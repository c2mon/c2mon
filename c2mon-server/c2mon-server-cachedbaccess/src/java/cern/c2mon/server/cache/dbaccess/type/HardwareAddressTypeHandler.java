/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.cache.dbaccess.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.shared.common.datatag.address.impl.HardwareAddressImpl;

/**
 * iBatis TypeHandler for HardwareAddress. Used in CommandTag
 * iBatis XML file. 
 * 
 * @author Mark Brightwell
 *
 */
public class HardwareAddressTypeHandler implements TypeHandler {

  @Override
  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    HardwareAddressFactory hardwareAddressFactory = HardwareAddressImpl.getInstance();
    String hardwareAddressString;
    HardwareAddress hardwareAddress;
    if ((hardwareAddressString = rs.getString(columnName)) != null) {
      hardwareAddress = hardwareAddressFactory.fromConfigXML(hardwareAddressString);
    }
    else {
      hardwareAddress = null;
    }
    return hardwareAddress; 
  }

  @Override
  public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setParameter(PreparedStatement ps, int parameterIndex, Object hardwareAddress, JdbcType jdbcType) throws SQLException {
    if (hardwareAddress != null) {
      ps.setString(parameterIndex, ((HardwareAddress) hardwareAddress).toConfigXML());          
    } else {
      ps.setString(parameterIndex, null);
    }
  }

}
