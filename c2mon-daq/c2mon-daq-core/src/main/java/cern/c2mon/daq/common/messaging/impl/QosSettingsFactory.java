/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.daq.common.messaging.impl;

import javax.jms.DeliveryMode;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.QosSettings;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.shared.common.datatag.DataTagValueUpdate;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;

/**
 * Util class to extract Quality-of-Service settings from a {@link SourceDataTagValue}
 * 
 * @author Matthias Braeger
 */
@Slf4j
final class QosSettingsFactory {
  
  private QosSettingsFactory() {}
  
  /**
   * We Take the first {@link SourceDataTagValue} object from collection to determine
   * the Quality-of-Service settings for the message sending
   * @param sourceDataTagValue the first tag extracted from {@link DataTagValueUpdate}
   * @return the Quality-of-Service settings for determine the {@link JmsTemplate}
   */
  static QosSettings extractQosSettings(SourceDataTagValue sourceDataTagValue) {
    QosSettings settings = new QosSettings();
    
    settings.setPriority(sourceDataTagValue.getPriority());
    settings.setTimeToLive(sourceDataTagValue.getTimeToLive());

    if (sourceDataTagValue.isGuaranteedDelivery()) {
      log.debug("\t sending PERSISTENT message");
      settings.setDeliveryMode(DeliveryMode.PERSISTENT);
    } else {
      log.debug("\t sending NON-PERSISTENT message");
      settings.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    }
    
    return settings;
  }
}
