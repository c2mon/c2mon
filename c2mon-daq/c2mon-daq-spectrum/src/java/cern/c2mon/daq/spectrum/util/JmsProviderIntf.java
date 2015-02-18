/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum.util;

import javax.jms.Connection;

public interface JmsProviderIntf {

    Connection getConnection();

}
