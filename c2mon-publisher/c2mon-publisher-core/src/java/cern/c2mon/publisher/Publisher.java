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
package cern.c2mon.publisher;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.shared.client.tag.TagConfig;

/**
 * The publisher interface. A publisher project that wants to make use of the
 * publisher core functionality has to implement this interface and to anotate
 * the class as a Spring <b>@Service</b>.<br>
 * Furthermore, you have the possiblity to provide with the following Java
 * environment variable an additional path to your your spring context file which
 * will then be included into the startup context:
 * <p>
 * <code>-Dc2mon.publisher.spring.configuration.location</code>
 * @author Matthias Braeger
 */
public interface Publisher {
  
  /**
   * This method is called by the publisher core after every tag update or new 
   * tag subscription.
   * <p>
   * Please notice, that the {@link TagConfig} object is only refresh from the server
   * once the TID configuration file has changed or has been touched.
   * 
   * @param tag An new tag update received by the {@link Gateway}
   * @param cdtConfig A reference of the tag configuration which is belonging to this tag update
   */
  void onUpdate(final Tag tag, final TagConfig cdtConfig);
  
  /**
   * This method is called at shutdown of the publisher. There is no need to
   * annotate it with @PostConstruct.
   */
  void shutdown();
}
