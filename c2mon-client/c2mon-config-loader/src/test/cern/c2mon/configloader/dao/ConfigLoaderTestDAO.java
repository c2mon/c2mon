/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.configloader.dao;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.configloader.Configuration;

/**
 * @author wbuczak
 */
public interface ConfigLoaderTestDAO extends ConfigLoaderDAO {

    void setAppliedFlag(final long configId);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void insert(Configuration conf);

}