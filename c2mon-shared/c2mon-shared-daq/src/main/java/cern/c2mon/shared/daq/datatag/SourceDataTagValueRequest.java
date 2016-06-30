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
package cern.c2mon.shared.daq.datatag;


import cern.c2mon.shared.daq.messaging.ServerRequest;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Topic;


//imported as in into TIM2

/**
 * This class represents a request to a DAQ process to send the
 * last know values of a number of DataTags.
 *
 * A request is generally created by the application server, typically
 * by BigBrother (when an alive tag is received again after it had
 * expired).
 *
 * The DAQ needs to be able to handle three types of request:
 * <ul>
 * <li><b>DataTag</b>: request the value of an individual DataTag</li>
 * <li><b>Equipment</b>: request the values of all DataTags attached to an equipment</li>
 * <li><b>Process</b>: request the values of all DataTags attached to a DAQ process</li>
 * </ul>
 * The DAQ process is expected to respond with a SourceDataTagValueResponse
 * sent to the server on the topic contained in the request.
 *
 * @author stowisek
 */
@Data
public class SourceDataTagValueRequest implements ServerRequest {

  private static final Logger LOG = LoggerFactory.getLogger(SourceDataTagValueRequest.class);

  /**
   * Identifier of the process/equipment/datatag for which the tag values are requested.
   */
  private Long id;

  /**
   * Request type: TYPE_PROCESS, TYPE_EQUIPMENT or TYPE_DATATAG
   */
  private DataTagRequestType type;

  /**
   * Topic on which the DAQ is expected to publish the response to this
   * request.
   */
  protected Topic replyTopic;

  public enum DataTagRequestType {
    PROCESS,
    EQUIPMENT,
    DATATAG;
  }

  /**
   * Constructor
   *
   * @param pType request type
   * @param pId   identifier of the equipment/process/datatag for which the values are requested
   */
  public SourceDataTagValueRequest(final DataTagRequestType pType, final Long pId) {
    this.id = pId;
    this.type = pType;
  }

  public SourceDataTagValueRequest() {
  }
}
