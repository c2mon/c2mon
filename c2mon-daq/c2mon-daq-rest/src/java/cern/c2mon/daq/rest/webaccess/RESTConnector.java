/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.rest.webaccess;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * This class is responsible to make RESTful requests to web sites.
 * All logic of sending request as well transforming the answers to
 * a specific format is provided by this class.
 *
 * @author Franz Ritter
 */
public class RESTConnector {

  private static RestTemplate restTemplate = new RestTemplate();

  /**
   * Send a request to the given URL and receives a answer.
   * The Answer is saved and returned as String.
   * @param url the Url of the web service with a REST functionality
   * @return the answer from the Server
   */
  public static String sendAndReceiveRequest(String url){

    ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
    if(entity.hasBody() && entity.getStatusCode().is2xxSuccessful()){
      return entity.getBody();
    } else {
      throw new RestClientException("url: " + url + " causes problems. Body is not available or request is not successful.");
    }
  }

  public static RestTemplate getRestTemplate(){
    return restTemplate;
  }

}
