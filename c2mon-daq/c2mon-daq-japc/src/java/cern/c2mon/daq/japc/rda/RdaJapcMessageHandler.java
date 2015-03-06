package cern.c2mon.daq.japc.rda;

import cern.c2mon.daq.japc.GenericJapcMessageHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;

/**
 * a dedicated JAPC DAQ for RDA. just before initialization it sets up RBAC token
 */
public class RdaJapcMessageHandler extends GenericJapcMessageHandler {

    @Override
    protected final void beforeConnectToDataSource() throws EqIOException {
        initRbac();
    }
}