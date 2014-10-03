package cern.c2mon.daq.japc.rda;

import javax.security.auth.login.LoginException;

import cern.accsoft.security.rba.login.LoginPolicy;
import cern.c2mon.daq.japc.GenericJapcMessageHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.rba.util.relogin.RbaLoginService;

/**
 * a dedicated JAPC DAQ for RDA. just before initialization it sets up RBAC token
 */
public class RdaJapcMessageHandler extends GenericJapcMessageHandler {

    // authentication by location is enabled
    static String user = "not-used";
    static String pass = "not-used";
    String rbacAppName = "JAPC-RDA-DAQ";

    private static boolean rbacInitilaized = false;

    @Override
    protected final void beforeConnectToDataSource() throws EqIOException {

        if (!rbacInitilaized) {
            try {

                RbaLoginService service = new RbaLoginService();
                service.setUser(user);
                service.setPassword(pass);
                service.setLoginPolicy(LoginPolicy.LOCATION);
                service.setApplicationName(rbacAppName);
                service.setAutoRefresh(true);
                service.startAndLogin();

                rbacInitilaized = true;

            } catch (LoginException ex) {
                throw new EqIOException("RBAC initialization failed! " + ex.getMessage());
            }

        }
    }

}
