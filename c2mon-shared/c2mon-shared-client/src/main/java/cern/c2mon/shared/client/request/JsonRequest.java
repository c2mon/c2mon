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
package cern.c2mon.shared.client.request;

import java.util.Collection;

import com.google.gson.JsonSyntaxException;

/**
 * This interface shall be implemented by all request objects that
 * are supporting Json message serialization and deserialization
 * as communication between the C2MON server and the C2MON client
 * API. 
 *
 * @param <T> Specifies the <code>Interface</code> for the result type.
 * @author Matthias Braeger
 */
public interface JsonRequest<T extends ClientRequestResult> {

  /**
   * @return A Json string representation of this object
   */
  String toJson();

  /**
   * This method can deserialize the Json response message that is being sent by the
   * C2MON server. Depending on the request it will be either a collection of
   * <code>TransferTag</code>, <code>TransferTagValue</code> or
   * <code>SupervisionEvent</code> objects.
   * @param json Json string representation of a <code>ClientRequestResult</code> class
   * @return The deserialized Json message
   * @throws JsonSyntaxException This exception is raised when Gson attempts to read
   *                             (or write) a malformed JSON element.
   */
  Collection<T> fromJsonResponse(final String json) throws JsonSyntaxException;
  
  public boolean isObjectRequest () ;
  
  public Object getObjectParameter () ;
}
