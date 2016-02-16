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
package cern.c2mon.daq.rest.controller;

import cern.c2mon.daq.rest.scheduling.PostScheduler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * This class is responsible for getting 'REST-POST' requests from clients.
 *
 * @author Franz Ritter
 */
@Controller
public class RestController {

  private PostScheduler postScheduler;

  /**
   * The method receives 'rest-post' queries.
   * In order to ensure that the message decoding is done right the user of the post query needs to specify the
   * header.
   * The Header needs to be 'Content-Type: text/plain' or 'text/json'.
   * The safest way to use this post is to use the type 'plane'.
   *
   * In order to send the message itself the data must be specified in the body of the HTTP request.
   *
   * @param id The id of the DataTag to which this message belongs
   * @param value Tha value of the message which need to be specified in the body.
   * @return The status of the request. If the request was successful to the server the request will be HttpStatus.OK.
   */
  @RequestMapping(value = "/tags/{id}", method = RequestMethod.POST)
  public HttpStatus postHandler(@PathVariable("id") Long id, @RequestBody String value) {

    HttpStatus status = postScheduler.sendValueToServer(id, value);
    return status;


  }

  public void setPostScheduler(PostScheduler scheduler) {
    this.postScheduler = scheduler;
  }

}
