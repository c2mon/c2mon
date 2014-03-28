/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.configloader.requestor;

import java.util.List;

import cern.c2mon.configloader.Configuration;

/**
 * The <code>TestReconfigurationRequestor</code> is used for test purposes only. It extends the
 * <code>ServerReconfigurationRequestor</code> interface with a possibility to access reconfiguration records
 * 
 * @author wbuczak
 */
public interface TestReconfigurationRequestor extends ServerReconfigurationRequestor {
    List<Configuration> getReconfigurationRequests();
}