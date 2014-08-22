/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.plsline.impl;

import cern.c2mon.daq.almon.plsline.PlsLineResolver;

/**
 * This pls line resolver implementation is used for test purposes only.
 * 
 * @author wbuczak
 */
public class DummyPlsLineResolver implements PlsLineResolver {

    @Override
    public int resolve(String cycleName) {
        return 1;
    }

}