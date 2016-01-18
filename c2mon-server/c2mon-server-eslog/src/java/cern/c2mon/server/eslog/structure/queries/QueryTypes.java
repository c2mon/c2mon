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
package cern.c2mon.server.eslog.structure.queries;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Retrieve the types present in ElasticSearch for a specific index.
 * @author Alban Marguet.
 */
@Slf4j
public class QueryTypes extends Query {

  public QueryTypes(Client client) {
    super(client);
  }

  public List<String> getListOfAnswer(String index) {
    List<String> result = new ArrayList<>();

    if (index != null) {
      Iterator<ObjectCursor<String>> typesIt = getIndexWithMetadata(index).keys().iterator();
      addTypesToResult(typesIt, result);
      log.info("QueryTypes - Got a list of types, size= " + result.size());
    }
    return result;
  }

  private void addTypesToResult(Iterator<ObjectCursor<String>> typesIt, List<String> result) {
    while(typesIt.hasNext()) {
      result.add(typesIt.next().value);
    }
  }
}