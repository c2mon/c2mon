package cern.c2mon.daq.rest;
/******************************************************************************
 * Copyright (C) 2010- CERN. All rights not expressly granted are reserved.
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

import cern.c2mon.daq.rest.webaccess.RESTConnector;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;

import org.springframework.web.client.RestTemplate;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Created by fritter on 27/01/16.
 */
public class SimpleTests {

  @Test
  public void testXml() {

    try {
      new RESTConnector();

      RestTemplate template = new RestTemplate();
      MockRestServiceServer mockServer = MockRestServiceServer.createServer(template);

      mockServer.expect(requestTo("http://www.testaddress.org/")).andExpect(method(HttpMethod.GET)).andRespond(withSuccess("resultSuccess", MediaType.TEXT_PLAIN));

      template.getForObject("http://www.testaddress.org/", String.class);

      mockServer.verify();


//      String test = RESTConnector.sendAndReceiveRequest("http://googlsdade.com/");
//      System.out.println(test);

//      boolean check = RESTConnector.isAvailable("http://ip.jsontest.com/");
//      System.out.println(check);

    } catch (RestClientException exc) {
//      System.out.println("Exception thrown");
//      System.out.println(exc.getMessage());
    }

  }


}
