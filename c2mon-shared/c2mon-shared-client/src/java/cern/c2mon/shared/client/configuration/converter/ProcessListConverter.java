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
package cern.c2mon.shared.client.configuration.converter;

import java.util.HashSet;
import java.util.Set;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import cern.c2mon.shared.common.process.ProcessConfiguration;

/**
 * Class used during the deserialisation of a {@link ProcessConfiguration} to
 * turn a comma-separated list of process names to a set, as it sn't possible to
 * do directly with a SimpleXML annotation.
 *
 * @author Justin Lewis Salmon
 */
public class ProcessListConverter implements Converter<Set<String>> {

  @Override
  public Set<String> read(InputNode node) throws Exception {
    return convert(node.getValue());
  }

  @Override
  public void write(OutputNode node, Set<String> processList) throws Exception {
  }

  /**
   * Convert a comma-separated string to a set of tokens.
   *
   * @param list the comma-separated list
   * @return the set of tokens
   */
  public Set<String> convert(String list) {
    Set<String> processList = new HashSet<>();

    for (String process : list.substring(1, list.length() - 1).split(", ")) {
      if (process.length() > 0) {
        processList.add(process);
      }
    }

    return processList;
  }
}
