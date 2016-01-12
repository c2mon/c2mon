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

package cern.c2mon.configloader.dao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.configloader.Configuration;

/**
 * db-access operations performed by C2MON configuration loader
 * 
 * @author wbuczak
 */
public interface ConfigLoaderDAO {

    /**
     * returns list of configurations which are not yet applied to the server
     * 
     * @return list of config ids
     */
    @Transactional(readOnly = true)
    List<Configuration> getConfigurationsForLoading();

    /**
     * updates existing configuration record once the configuration is applied
     * 
     * @param configId
     * @param userName
     * @param applytimestamp
     * @param status
     */
    @Transactional
    void update(long configId, String userName);
}
