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
package cern.c2mon.server.cache.control;

import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import cern.c2mon.server.common.expression.Evaluator;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
@Service
public class ControlTagFacadeImpl extends AbstractDataTagFacade<ControlTag> implements ControlTagFacade {

  private AliveTimerCache aliveTimerCache;

  @Autowired
  public ControlTagFacadeImpl(final DataTagCacheObjectFacade dataTagCacheObjectFacade,
                              final ControlTagCache controlTagCache,
                              final AlarmFacade alarmFacade,
                              final AlarmCache alarmCache,
                              final ControlTagCacheObjectFacade controlTagCacheObjectFacade,
                              final QualityConverter qualityConverter,
                              final Evaluator evaluator) {
    super(controlTagCache, alarmFacade, alarmCache, controlTagCacheObjectFacade, dataTagCacheObjectFacade, qualityConverter, evaluator);
    this.aliveTimerCache = aliveTimerCache;
  }

  @Override
  public ControlTagCacheObject createCacheObject(final Long id, final Properties properties) throws IllegalAccessException {
    ControlTagCacheObject controlTag = new ControlTagCacheObject(id);
    configureCacheObject(controlTag, properties);
    
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
    return controlTag.getAddress() != null;
  }
}
