/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2012 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.publisher;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.shared.client.tag.TagConfig;

/**
 * The publisher interface. A publisher project that wants to make use of the
 * publisher core functionality has to implement this interface and to anotate
 * the class as a Spring <b>@Service</b>.<br>
 * Furthermore, you have the possiblity to provide with the following Java
 * environment variable an additional path to your your spring context file which
 * will then be included into the startup context:
 * <p>
 * <code>-Dpublisher.spring.configuration.location</code>
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
   * @param cdt An new tag update received by the {@link Gateway}
   * @param cdtConfig A reference of the tag configuration which is belonging to this tag update
   */
  void onUpdate(final ClientDataTagValue cdt, final TagConfig cdtConfig);
  
  /**
   * This method is called at shutdown of the publisher. There is no need to
   * annotate it with @PostConstruct.
   */
  void shutdown();
}
