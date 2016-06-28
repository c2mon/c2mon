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
package cern.c2mon.client.core.configuration.util;

import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;

import java.util.List;

/**
 * @author Franz Ritter
 */
public class ConfigurationUtil {

  /**
   * Validates the Configuration if its created through the update builder provided by the Configuration POJO.
   * If not throw an Exception to inform the client to use the correct builder.
   *
   * @param config The update configuration object build by the client.
   */
  public static <T extends ConfigurationEntity> void validateIsUpdate(List<T> configurationObjects) {
    for (T config : configurationObjects) {
      if (!config.isUpdated()) {
        throw new IllegalArgumentException(config.getClass() + " Created through the wrong builder pattern. " +
            "Please use the 'update' builder provided by the class of the object");
      }
    }
  }

  /**
   * Validates the Configuration if its created through the create builder provided by the Configuration POJO.
   * If not throw an Exception to inform the client to use the correct builder.
   *
   * @param config The create configuration object build by the client.
   */
  public static <T extends ConfigurationEntity> void validateIsCreate(List<T> configurationObjects) {
    for (T config : configurationObjects) {
      if (!config.isCreated()) {
        throw new IllegalArgumentException(config.getClass() + " Created through the wrong builder pattern. " +
            "Please use the 'create' builder provided by the class of the object");
      }
    }
  }
}
