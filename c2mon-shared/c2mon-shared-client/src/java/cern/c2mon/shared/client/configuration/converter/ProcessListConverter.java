/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
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
 ******************************************************************************/
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