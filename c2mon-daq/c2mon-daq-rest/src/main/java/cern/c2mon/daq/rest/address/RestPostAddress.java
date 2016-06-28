/*
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
 */
package cern.c2mon.daq.rest.address;

import cern.c2mon.shared.common.datatag.address.impl.HardwareAddressImpl;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Franz Ritter
 */

/**
 * This class holds all information of the PostHardwareAddress.
 * The information of the PostHardwareAddress holds the information
 * in which interval a new post-message according to a DataTag is expected.
 * </p>
 * To create an instance of this class the use factory method of {@link RestAddressFactory}.
 */
@Getter
@Setter
public class RestPostAddress extends HardwareAddressImpl {

  /**
   * The frequency determines in which time interval a post message is expected.
   * If no message is received in the given interval the daq will send an invalid message of the corresponding tag to the server.
   */
  private Integer frequency;

  /**
   * Constructor which is used by the {@link RestAddressFactory} to create an instance of this class.
   *
   * @param frequency The frequency for the post.
   */
  protected RestPostAddress(Integer frequency){

    this.frequency = frequency;
  }

}
