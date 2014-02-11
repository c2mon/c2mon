/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.ssh;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.c2mon.daq.ssh.tools.SSHHelper;
import cern.c2mon.daq.ssh.tools.SSHXMLExecutionFeedback;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;

/**
 * This class implements a set of JUnit tests for <code>SSHMessageHandler</code>. All tests that require
 * SSHMessageHandler's pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
 * annotation, specifying the XML file to be used.
 * 
 * @see
 * @see cern.c2mon.daq.jmx.JMXMessageHandler
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
    public void testParseSSHExecutionFeedback() throws EqCommandTagException, EqIOException {
      sshHandler.connectToDataSource();
      String xmlFeedback = "<?xml version = \"1.0\"?><execution-status><status-code>0</status-code><status-description><![CDATA[OK]]></status-description></execution-status>";
    
      SSHHelper helper = sshHandler.getSSHHelper();
      SSHXMLExecutionFeedback  sshXMLExecutionFeedback = 
          helper.parseSSHExecutionFeedback(xmlFeedback);
      assertEquals(sshXMLExecutionFeedback.statusCode, 0);
    }
}
