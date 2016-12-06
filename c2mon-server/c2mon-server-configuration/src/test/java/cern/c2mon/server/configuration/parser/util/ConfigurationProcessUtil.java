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
package cern.c2mon.server.configuration.parser.util;

import java.util.Properties;

import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;
import cern.c2mon.shared.common.metadata.Metadata;

//@Service
public class ConfigurationProcessUtil {

  /**
   * Expected generated id is 1.
   * Expected status tag id is 101
   * Expected alive tag id is 100
   */
  public static Process buildCreateBasicProcess(Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Process process = Process.create("P_TEST").build();

    properties.setProperty("name", "P_TEST");
    properties.setProperty("description", "<no description provided>");
    properties.setProperty("statusTagId", String.valueOf(100L));
    properties.setProperty("aliveInterval", String.valueOf(10000));
    properties.setProperty("aliveTagId", String.valueOf(101l));
    properties.setProperty("maxMessageDelay", String.valueOf(1000));
    properties.setProperty("maxMessageSize", String.valueOf(100));

    return process;
  }

  /**
   * Expected status tag id is 101
   * Expected alive tag id is 100
   */
  public static Process buildCreateAllFieldsProcess(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Process process = Process.create("P_TEST" + id)
        .id(id)
        .description("foo")
        .aliveTag(AliveTag.create("P_TEST" + id + ":ALIVE").build(), 70000)
        .statusTag(StatusTag.create("P_TEST" + id + ":STATUS").build())
        .maxMessageDelay(2000)
        .maxMessageSize(200)
        .build();

    properties.setProperty("name", "P_TEST"+id);
    properties.setProperty("description", "foo");
    properties.setProperty("statusTagId", String.valueOf(100l));
    properties.setProperty("aliveInterval", String.valueOf(70000));
    properties.setProperty("aliveTagId", String.valueOf(101l));
    properties.setProperty("maxMessageDelay", String.valueOf(2000));
    properties.setProperty("maxMessageSize", String.valueOf(200));

    return process;
  }
  public static Process buildUpdateProcessWithAllFields(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Process process = Process.update(id)
        .description("foo")
        .aliveInterval(70000)
        .maxMessageSize(200)
        .maxMessageDelay(2000)
        .build();

    properties.setProperty("description", "foo");
    properties.setProperty("aliveInterval", String.valueOf(70000));
    properties.setProperty("maxMessageDelay", String.valueOf(2000));
    properties.setProperty("maxMessageSize", String.valueOf(200));

    return process;
  }

  public static Process buildUpdateProcessWithSomeFields(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Process process = Process.update(id)
        .description("foo_Update")
        .maxMessageSize(101)
        .build();

    properties.setProperty("description", "foo_Update");
    properties.setProperty("maxMessageSize", String.valueOf(101));

    return process;
  }

  public static Process buildUpdateProcessWithSomeFields(String name, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Process process = Process.update(name)
        .description("foo_Update")
        .maxMessageSize(101)
        .build();
    int a;

    properties.setProperty("name", name);
    properties.setProperty("description", "foo_Update");
    properties.setProperty("maxMessageSize", String.valueOf(101));

    return process;
  }

  public static Process buildDeleteProcess(Long id) {
    Process deleteProcess = new Process();
    deleteProcess.setId(id);
    deleteProcess.setDeleted(true);

    return deleteProcess;
  }
}
