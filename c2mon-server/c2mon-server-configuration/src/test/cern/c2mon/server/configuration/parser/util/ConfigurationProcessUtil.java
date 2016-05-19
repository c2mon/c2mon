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
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;

//@Service
public class ConfigurationProcessUtil {


  public static Pair<Process, Properties> buildProcessWtihId(Long id) {
    return new Pair<Process, Properties>(Process.builder().id(id).build(), new Properties());
  }

  public static Pair<Process, Properties> buildProcessWtihPrimFields(Long id) {
    Process pro = Process.builder()
        .id(id)
        .name("Prozess")
        .description("foo")
        .statusTag(StatusTag.builder().id(0l).name("").description("").build())
        .aliveTag(AliveTag.builder().id(1l).name("").description("").build())
        .build();

    Properties props = new Properties();
    props.setProperty("name", "Prozess");
    props.setProperty("description", "foo");
    props.setProperty("stateTagId", String.valueOf(0l));
    props.setProperty("aliveInterval", String.valueOf(10000));
    props.setProperty("aliveTagId", String.valueOf(1l));
    props.setProperty("maxMessageDelay", String.valueOf(1000));
    props.setProperty("maxMessageSize", String.valueOf(100));

    return new Pair<Process, Properties>(pro, props);
  }

  public static Pair<Process, Properties> buildProcessWtihAllFields(Long id) {
    Process pro = Process.builder()
        .id(id)
        .name("P_TEST")
        .description("foo")
        .statusTag(StatusTag.builder().id(0l).name("").description("").build())
        .aliveInterval(66000)
        .aliveTag(AliveTag.builder().id(1l).name("").description("").build())
        .maxMessageDelay(6000)
        .maxMessageSize(60)
        .build();

    Properties props = new Properties();
    props.setProperty("name", "P_TEST");
    props.setProperty("description", "foo");
    props.setProperty("stateTagId", String.valueOf(0l));
    props.setProperty("aliveInterval", String.valueOf(66000));
    props.setProperty("aliveTagId", String.valueOf(1l));
    props.setProperty("maxMessageDelay", String.valueOf(6000));
    props.setProperty("maxMessageSize", String.valueOf(60));

    return new Pair<Process, Properties>(pro, props);
  }

  public static Pair<Process, Properties> buildProcessWtihoutDefaultFields(Long id) {
    Process pro = Process.builder()
        .id(id)
        .name("Prozess")
        .description("foo")
        .statusTag(StatusTag.builder().id(0l).name("").description("").build())
        .aliveTag(AliveTag.builder().id(1l).name("").description("").build())
        .build();

    Properties props = new Properties();
    props.setProperty("name", "Prozess");
    props.setProperty("description", "foo");
    props.setProperty("stateTagId", String.valueOf(0l));
    props.setProperty("aliveInterval", String.valueOf(10000));
    props.setProperty("aliveTagId", String.valueOf(1l));
    props.setProperty("maxMessageDelay", String.valueOf(1000));
    props.setProperty("maxMessageSize", String.valueOf(100));

    return new Pair<Process, Properties>(pro, props);
  }

  public static Process buildUpdateProcessNewControlTag(Long id) {
    return Process.builder()
        .id(0l)
        .aliveTag(AliveTag.builder().id(1l).name("").description("").build())
        .build();

  }

  public static Pair<Process, Properties> buildUpdateProcessWtihAllFields(Long id) {
    Process pro = Process.builder()
        .id(id)
        .name("Prozess_Update")
        .description("foo_Update")
        .aliveInterval(100000)
        .maxMessageDelay(10000)
        .maxMessageSize(101)
        .build();

    Properties props = new Properties();
    props.setProperty("name", "Prozess_Update");
    props.setProperty("description", "foo_Update");
    props.setProperty("aliveInterval", String.valueOf(100000));
    props.setProperty("maxMessageDelay", String.valueOf(10000));
    props.setProperty("maxMessageSize", String.valueOf(101));

    return new Pair<Process, Properties>(pro, props);
  }

  public static Pair<Process, Properties> buildUpdateProcessWtihSomeFields(Long id) {
    Process pro = Process.builder()
        .id(id)
        .description("foo_Update")
        .maxMessageSize(100)
        .build();

    Properties props = new Properties();
    props.setProperty("description", "foo_Update");
    props.setProperty("maxMessageSize", String.valueOf(100));

    return new Pair<>(pro, props);
  }

  public static Process buildDeleteProcess(Long id) {
    Process pro = Process.builder()
        .id(id)
        .deleted(true)
        .build();

    return pro;
  }
  // ##################### Builder #####################

  public static Pair<Process.ProcessBuilder, Properties> builderProcessWithPrimFields(Long id, Long statusTagId, Long aliveTagId) {
    Process.ProcessBuilder pro = Process.builder()
        .id(id)
        .name("P_TEST")
        .description("foo");

    Properties props = new Properties();
    props.setProperty("name", "P_TEST");
    props.setProperty("description", "foo");
    props.setProperty("stateTagId", String.valueOf(statusTagId));
    props.setProperty("aliveInterval", String.valueOf(10000));
    props.setProperty("maxMessageDelay", String.valueOf(1000));
    props.setProperty("maxMessageSize", String.valueOf(100));
    props.setProperty("aliveTagId", String.valueOf(aliveTagId));

    return new Pair<>(pro, props);
  }

  public static Pair<Process.ProcessBuilder, Properties> builderProcessWithAllFields(Long id, Long statusTagId, Long aliveTagId) {
    Process.ProcessBuilder pro = Process.builder()
        .id(id)
        .name("P_TEST")
        .description("foo")
        .aliveInterval(66000)
        .maxMessageDelay(6000)
        .maxMessageSize(60);

    Properties props = new Properties();
    props.setProperty("name", "P_TEST");
    props.setProperty("description", "foo");
    props.setProperty("stateTagId", String.valueOf(statusTagId));
    props.setProperty("aliveInterval", String.valueOf(66000));
    props.setProperty("aliveTagId", String.valueOf(aliveTagId));
    props.setProperty("maxMessageDelay", String.valueOf(6000));
    props.setProperty("maxMessageSize", String.valueOf(60));

    return new Pair<>(pro, props);
  }

  public static Pair<Process.ProcessBuilder, Properties> builderProcessUpdate(Long id) {
    Process.ProcessBuilder pro = Process.builder()
        .id(id)
        .description("foo_update")
        .maxMessageSize(100);

    Properties props = new Properties();
    props.setProperty("description", "foo_update");
    props.setProperty("maxMessageSize", String.valueOf(100));

    return new Pair<>(pro, props);
  }

}
