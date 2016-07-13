///******************************************************************************
// * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
// *
// * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
// * C2MON is free software: you can redistribute it and/or modify it under the
// * terms of the GNU Lesser General Public License as published by the Free
// * Software Foundation, either version 3 of the license.
// *
// * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// * more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
// *****************************************************************************/
//package cern.c2mon.client.apitest;
//
//import static java.lang.String.format;
//
//import java.io.Console;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import javax.security.auth.login.LoginException;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import cern.accsoft.security.rba.RBASubject;
//import cern.accsoft.security.rba.login.DefaultCallbackHandler;
//import cern.accsoft.security.rba.login.LoginPolicy;
//import cern.accsoft.security.rba.login.RBALoginContext;
//import cern.c2mon.client.apitest.db.Dmn2DbServiceGateway;
//import cern.c2mon.client.common.tag.ClientCommandTag;
//import cern.c2mon.client.core.C2monCommandManager;
//import cern.c2mon.client.core.C2monServiceGateway;
//import cern.c2mon.client.core.C2monSessionManager;
//import cern.rba.util.holder.ClientTierSubjectHolder;
//import cern.c2mon.shared.client.command.CommandReport;
//
//public class C2MONClientCommandExecutionTest {
//
//    private static Logger log = LoggerFactory.getLogger(C2MONClientCommandExecutionTest.class);
//
//    private static final String APP_NAME = "DMN2";
//
//    static Console console = null;
//
//    static Map<Long, ClientCommandTag<?>> clientCommandsMap = new HashMap<Long, ClientCommandTag<?>>();
//
//    static C2monSessionManager sessionManager;
//    static C2monCommandManager commandsManager;
//
//    /**
//     * Performs login from the command line.
//     *
//     * @return RBAToken if login is successful or throw an exception if it fails
//     * @throws LoginException if authentication fails
//     */
//    private static RBASubject login() throws LoginException {
//        final String userName = console.readLine("Enter your NICE user name: ");
//        char[] userPassw = console.readPassword("Enter your NICE password: ");
//        DefaultCallbackHandler callbackHandler = new DefaultCallbackHandler(APP_NAME, userName, userPassw);
//        // Use explicit login context which performs login only using username/passwd
//        RBALoginContext ctx = new RBALoginContext(LoginPolicy.EXPLICIT, callbackHandler);
//        ctx.login();
//        // Obfuscate the passwd array
//        Arrays.fill(userPassw, '0');
//        userPassw = null;
//        return ctx.getRBASubject();
//    }
//
//    /**
//     * Log4j Logger
//     */
//    protected static final Logger TAG_LOG = LoggerFactory.getLogger(C2MONClientCommandExecutionTest.class);
//
//    public static void main(String[] args) {
//
//        try {
//
//            console = System.console();
//            if (console == null) {
//                log.error("No console, can not read username and passwd from the command line.");
//                System.exit(1);
//            }
//
//            if (args.length < 1) {
//               log.error("No console, can not read username and passwd from the command line.");
//               console.printf("computer name expected!");
//               System.exit(1);
//            }
//
//            log.debug("db.properties: " + System.getProperty("db.properties"));
//            log.debug("jms.properties: " + System.getProperty("jms.properties"));
//
//            log.info("before Dmn2DbServiceGateway.init()");
//
//            Dmn2DbServiceGateway.init();
//            log.info("after Dmn2DbServiceGateway.init()");
//
//            // C2monServiceGateway.startC2monClient("file:" + System.getProperty("jms.properties"));
//
//            log.info("authenticate with RBAC..");
//            ClientTierSubjectHolder.setRBASubject(login());
//            log.info("done");
//
//            log.info(format("getting db. list of registered commands for computer: %s", args[0]));
//            List<CommandDef> commands = Dmn2DbServiceGateway.getDbAccessService().getRegisteredCommands(args[0]);
//
//            sessionManager = C2monServiceGateway.getSessionManager();
//
//            if (!sessionManager.getLoggedUserNames().isEmpty())
//              log.info(format("sessionManager user: %s isLogged: %s", sessionManager.getLoggedUserNames().iterator().next(), sessionManager
//                    .isUserLogged(sessionManager.getLoggedUserNames().iterator().next())));
//
//            commandsManager = C2monServiceGateway.getCommandManager();
//
//            Set<Long> cmdIds = new HashSet<Long>();
//
//            for (CommandDef cd : commands) {
//                console.printf("Command id:%d name:%s type:%d datatype:%d\n", cd.getCommandTagId(), cd.getUniqueName(),
//                        cd.getCommandType(), cd.getDataType());
//                cmdIds.add(cd.getCommandTagId());
//            }
//
//            log.info("requesting client command-tag objects from C2MON srv..");
//
//            for (ClientCommandTag<?> ct : commandsManager.getCommandTags(cmdIds)) {
//                clientCommandsMap.put(ct.getId(), ct);
//
//            }
//            log.info("done");
//
//            while (true) {
//                // execute commands
//                String idStr = console.readLine("Enter command id to execute: ");
//                Long id = Long.parseLong(idStr);
//
//                ClientCommandTag<?> ct = clientCommandsMap.get(id);
//
//                try {
//
//                    if (ct == null) {
//                        console.printf("command not found");
//                        continue;
//                    }
//
//                    String valStr = console.readLine("Enter command value: ");
//                    Object val = null;
//
//                    if (ct.getValueType().equals(Integer.class)) {
//                        val = Integer.parseInt(valStr);
//                    } else if (ct.getValueType().equals(Long.class)) {
//                        val = Long.parseLong(valStr);
//                    } else if (ct.getValueType().equals(Boolean.class)) {
//                        val = Boolean.parseBoolean(valStr);
//                    } else if (ct.getValueType().equals(Float.class)) {
//                        val = Float.parseFloat(valStr);
//                    } else if (ct.getValueType().equals(Double.class)) {
//                        val = Double.parseDouble(valStr);
//                    } else {
//                        val = valStr;
//                    }
//
//                    log.info(format("executing command: %d, value:%s", id, val));
//                    CommandReport rep = commandsManager.executeCommand(sessionManager.getLoggedUserNames().iterator().next(), ct.getId(), val);
//                    log.info("done, report received");
//                    console.printf("command report status: %d, txt: %s, report-txt: %s", rep.getStatus().getStatus(),
//                            rep.getStatusText(), rep.getReportText());
//                } catch (Exception ex) {
//                    log.error("incorrect input", ex);
//                }
//
//            }
//
//        } catch (Exception e) {
//            log.error(e.toString());
//            System.exit(-1);
//        }
//
//    }
//}