/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.configloader;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author wbuczak
 */
public interface C2MonConfigLoaderService {

    @Transactional
    void applyConfiguration(Configuration conf);
}
