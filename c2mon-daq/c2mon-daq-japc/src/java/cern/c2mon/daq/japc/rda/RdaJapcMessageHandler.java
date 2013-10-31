package cern.c2mon.daq.japc.rda;

import javax.security.auth.login.LoginException;

import cern.accsoft.security.rba.login.LoginPolicy;
import cern.c2mon.daq.japc.GenericJapcMessageHandler;
import cern.rba.util.relogin.RdaRbaLoginService;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;

/**
 * a dedicated JAPC DAQ for RDA. just befor initialization it sets up RBAC token
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

                RdaRbaLoginService service = new RdaRbaLoginService();
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
    
    
    public static final void main(String[] args) throws Exception {
        RdaRbaLoginService service = new RdaRbaLoginService();
        service.setUser(user);
        service.setPassword(pass);
        service.setLoginPolicy(LoginPolicy.EXPLICIT);
        service.setApplicationName("JAPC-RDA-DAQ");
        service.setAutoRefresh(true);
        service.startAndLogin();        
    }

}
