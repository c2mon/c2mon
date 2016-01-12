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
package cern.c2mon.server.client.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

/**
 * This class is responsible for logging all uncatched
 * exceptions of the <code>TagRequestHandler</code>.
 *
 * @author Matthias Braeger
 */
@Service("tagRequestErrorHandler")
public class ClientRequestErrorHandler implements ErrorHandler {

  /** Log4j logger */
  private static final Logger LOG = LoggerFactory.getLogger(ClientRequestErrorHandler.class);
  
  @Override
  public void handleError(final Throwable exception) {
    LOG.error("A problem occured while handling a tag request.", exception);
  }

}
