/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.configloader.requestor;

import cern.c2mon.configloader.Configuration;
import cern.c2mon.shared.client.configuration.ConfigurationReport;

/**
 * @author wbuczak
 */
public interface ServerReconfigurationRequestor {

    ConfigurationReport applyConfiguration(final Configuration configuration);

}