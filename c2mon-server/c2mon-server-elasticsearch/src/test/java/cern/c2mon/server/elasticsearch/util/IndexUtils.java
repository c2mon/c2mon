/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.elasticsearch.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;

public class IndexUtils {

  private IndexUtils() {
    // only static methods below
  }

  public static boolean doesIndexExist(String indexName, ElasticsearchProperties properties) throws IOException {
    HttpHead httpRequest = new HttpHead(("http://" + properties.getHost() + ":" + properties.getHttpPort() + "/" + indexName));
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse httpResponse = httpClient.execute(httpRequest);
    return httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
  }
}
