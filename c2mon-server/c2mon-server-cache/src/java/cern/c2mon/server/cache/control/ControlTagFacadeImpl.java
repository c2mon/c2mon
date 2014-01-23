/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cache.control;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.ControlTagFacade;
import cern.c2mon.server.cache.datatag.AbstractDataTagFacade;
import cern.c2mon.server.cache.datatag.DataTagCacheObjectFacade;
import cern.c2mon.server.cache.datatag.QualityConverter;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.shared.common.ConfigurationException;

/**
 * Implementation of the ControlTagFacade.  
 * 
 * @author Mark Brightwell
 */
@Service
public class ControlTagFacadeImpl extends AbstractDataTagFacade<ControlTag> implements ControlTagFacade {

  /** Log4j logger instance */
  private static final Logger LOGGER = Logger.getLogger(ControlTagFacadeImpl.class);
  
  /**
   * Property that will by used as trunk. Should
   * always be overriden by server default property.
   */
  @Value("${c2mon.jms.controltag.publication.topic}")
  private String controlTagPublicationTopic = "c2mon.client.controltag.default";
  
  private AliveTimerCache aliveTimerCache;
  
  @Autowired
  public ControlTagFacadeImpl(final DataTagCacheObjectFacade dataTagCacheObjectFacade,
                              final ControlTagCache controlTagCache,
                              final AliveTimerCache aliveTimerCache,
                              final AlarmFacade alarmFacade,
                              final AlarmCache alarmCache,
                              final ControlTagCacheObjectFacade controlTagCacheObjectFacade,
                              final QualityConverter qualityConverter) {
    super(controlTagCache, alarmFacade, alarmCache, controlTagCacheObjectFacade, dataTagCacheObjectFacade, qualityConverter);
    
    this.aliveTimerCache = aliveTimerCache;
  }
  
  @Override
  public ControlTagCacheObject createCacheObject(final Long id, final Properties properties) throws IllegalAccessException {
    ControlTagCacheObject controlTag = new ControlTagCacheObject(id);
    configureCacheObject(controlTag, properties);
    
    //topic is set from property
    controlTag.setTopic(controlTagPublicationTopic);
    
    validateConfig(controlTag);    
    return controlTag;
  }

  @Override
  protected void validateConfig(final ControlTag controlTag) {
    validateTagConfig(controlTag);
    if (controlTag.getEquipmentId() != null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Equipment id cannot be set for ControlTags - unable to configure.");
    }
    if (controlTag.getAddress() != null) {
      controlTag.getAddress().validate();      
    }
  }

  @Override
  public boolean isInProcessList(ControlTag controlTag) {
    return aliveTimerCache.getKeys().contains(controlTag.getId()) && (controlTag.getAddress() != null);
  }
}
