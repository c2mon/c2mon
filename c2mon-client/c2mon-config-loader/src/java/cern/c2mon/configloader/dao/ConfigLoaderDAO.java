/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

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