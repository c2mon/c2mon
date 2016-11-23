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
package cern.c2mon.server.configuration.mybatis;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import cern.c2mon.shared.client.configuration.ConfigConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import cern.c2mon.server.configuration.dao.ConfigurationDAO;
import cern.c2mon.shared.client.configuration.ConfigurationDescriptor;
import cern.c2mon.shared.client.configuration.ConfigurationElement;

import javax.sql.DataSource;

/**
 * Mybatis implementation of the ConfigurationDAO for the server
 * configuration module.
 *
 * @author Mark Brightwell
 *
 */
@Service
public class ConfigurationDAOImpl implements ConfigurationDAO {

  /**
   * The Mybatis mapper.
   */
  @Autowired
  private ConfigurationMapper configurationMapper;

  @Autowired
  private DataSource configurationDataSource;

  @Override
  public String getConfigName(int configId) {
    return configurationMapper.getConfigName(configId);
  }

  @Override
  public ConfigurationDescriptor getConfiguration(int configId) {
    //TODO implement this method for retrieving a configuration from the DB
    throw new UnsupportedOperationException("Method not implemented yet.");
  }

  @Override
  public List<ConfigurationDescriptor> getConfigurations() {
    // TODO implement this method for retrieving a list of configurations from the DB
    throw new UnsupportedOperationException("Method not implemented yet.");
  }

  @Override
  public List<ConfigurationElement> getConfigElements(int configId) {
    JdbcTemplate template = new JdbcTemplate(configurationDataSource);

    // This was rewritten because the MyBatis mapper query wasn't working,
    // and this method is actually much simpler.
    String sql = "SELECT SEQID, CONFIGID, MODETYPE, ELEMENTTYPE, ELEMENTPKEY " +
                 "FROM TIMCONFIGELT WHERE CONFIGID = ? ORDER BY SEQID";

    List<ConfigurationElement> elements = template.query(sql, (rs, rowNum) -> {
      ConfigurationElement element = new ConfigurationElement();
      element.setSequenceId(rs.getLong(1));
      element.setConfigId(rs.getLong(2));
      element.setAction(ConfigConstants.Action.valueOf(rs.getString(3)));
      element.setEntity(ConfigConstants.Entity.valueOf(rs.getString(4).toUpperCase()));
      element.setEntityId(rs.getLong(5));

      String select = "SELECT ELEMENTFIELD, ELEMENTVALUE FROM TIMCONFIGVAL WHERE SEQID = ?";
      List<Map<String, Object>> propertiesMap = template.queryForList(select, element.getSequenceId());

      Properties properties = new Properties();
      propertiesMap.forEach(map -> properties.put(map.get("ELEMENTFIELD"), map.get("ELEMENTVALUE")));
      element.setElementProperties(properties);

      return element;
    }, configId);

    return elements;
  }

  @Override
  public void saveStatusInfo(ConfigurationElement configurationElement) {
    configurationMapper.saveStatusInfo(configurationElement);
  }

  @Override
  public void markAsApplied(int id) {
    configurationMapper.markAsApplied(id);
  }
}
