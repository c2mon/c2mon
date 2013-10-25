/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.driver.ssh;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cern.c2mon.driver.ssh.SSHMessageHandler;
import cern.c2mon.driver.test.GenericMessageHandlerTst;
import cern.c2mon.driver.test.UseConf;
import cern.c2mon.driver.test.UseHandler;

/**
 * This class implements a set of JUnit tests for <code>SSHMessageHandler</code>. All tests that require
 * SSHMessageHandler's pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
 * annotation, specifying the XML file to be used.
 * 
 * @see
 * @see cern.c2mon.driver.jmx.JMXMessageHandler
 * @author wbuczak
 */
@UseHandler(SSHMessageHandler.class)
public class SSHMessageHandlerTest extends GenericMessageHandlerTst {

    SSHMessageHandler sshHandler;

    @Override
    protected void beforeTest() throws Exception {

        sshHandler = (SSHMessageHandler) msgHandler;

    }

    @Override
    protected void afterTest() throws Exception {
        // TODO Auto-generated method stub

    }

    @Test
    @UseConf("e_ssh_test1.xml")
    public void test1() {
        //TODO: a set of tests for SSHMessageHandler needs to be provided
        assertTrue(true);
    }
}
