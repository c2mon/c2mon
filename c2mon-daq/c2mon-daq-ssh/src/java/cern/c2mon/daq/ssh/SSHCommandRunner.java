/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2013 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.ssh;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import cern.c2mon.daq.common.ICommandRunner;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.daq.ssh.tools.SSHHelper;
import cern.c2mon.daq.ssh.tools.SSHXMLExecutionFeedback;
import cern.tim.shared.common.datatag.address.SSHHardwareAddress;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagValue;

import com.mindbright.ssh2.SSH2ConsoleRemote;
import com.mindbright.ssh2.SSH2SimpleClient;

/**
 * The command runner sends commands to the PLC and processes the answers to the command.
 * 
 * @author vilches
 *
 */
public class SSHCommandRunner implements ICommandRunner {
  
  /**
   * SSH Helper class with some helping methods.
   */
  private SSHHelper sshHelper;

  /**
   * Creates a new SSHCommandRunner.
   * 
   * @param sshHelper 
   */
  public SSHCommandRunner(final SSHHelper sshHelper) {
    this.sshHelper = sshHelper;
  }

  /**
   * Runs the command matching the source command tag value.
   * 
   * @param sourceCommandTagValue Value to identify the command to run.
   * @throws EqCommandTagException Throws an exception if the execution of the command fails.
   * @return Returns a message about the execution of the command or null if there is no need.
   */
  @Override
  public String runCommand(final SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
    ISourceCommandTag sct = null;

    try {
      sct = this.sshHelper.getEquipmentConfiguration().getSourceCommandTags().get(sourceCommandTagValue.getId());
    } catch (Exception ex) {
      this.sshHelper.getEquipmentLogger().error("runCommand - A problem occured while trying to get the SourceCommandTag object from the list of registered commands", ex);
      throw new EqCommandTagException(new StringBuffer("Could not execute the command").append(
          "A problem occured while trying to get the SourceCommandTag object from the list of registered commands. Exection message").append(ex.getMessage()).toString());
    }

    SSHHardwareAddress address = null;

    // try to get the command-tag's address
    try {
      address = (SSHHardwareAddress) sct.getHardwareAddress();
    } catch (ClassCastException ex) {
      this.sshHelper.getEquipmentLogger().error("runCommand - Invalid source command definition. Could not cast the address to class SSHHardwareAddress");
      throw new EqCommandTagException("Invalid source command definition. Could not cast the address to class SSHHardwareAddress");
    }

    String server = this.sshHelper.establishHost(address);
    String key = this.sshHelper.establishSshKey(address);
    String keyPassword = this.sshHelper.establishSshPasshprase(address);
    String user = this.sshHelper.establishUser(address);
    String passwd = this.sshHelper.establishUserPassword(address);

    int port = this.sshHelper.getServerPort(server);

    String cmdLine = address.getSystemCall();

    if (this.sshHelper.getEquipmentLogger().isDebugEnabled()) {
      this.sshHelper.getEquipmentLogger().debug("runCommand - attempting to execute ssh command with following settings:");
      this.sshHelper.getEquipmentLogger().debug("\tserver: " + server);
      this.sshHelper.getEquipmentLogger().debug("\tkey: " + key);
      this.sshHelper.getEquipmentLogger().debug("\tkey_pass:" + keyPassword);
      this.sshHelper.getEquipmentLogger().debug("\tuser:" + user);
      this.sshHelper.getEquipmentLogger().debug("\tpassws:" + passwd);
      this.sshHelper.getEquipmentLogger().debug("\tport: " + port);
      this.sshHelper.getEquipmentLogger().debug("system-call:" + cmdLine);
    }

    SSH2SimpleClient client = null;

    // if there's no ssh_key defined, use the user-name & password
    if (key == null) {
      try {
        client = this.sshHelper.authenticate(server, port, user, passwd);
      } catch (Exception ex) {
        this.sshHelper.getEquipmentLogger().error("runCommand - command execution failed: cound not establish ssh connection. " 
            + "Exception error message : " + ex.getMessage());
        throw new EqCommandTagException("command execution failure: cound not establish ssh connection. error message : " + ex.getMessage());
      }
    } else {
      try {
        client = this.sshHelper.authenticateUsingKey(server, port, user, key, keyPassword);
      } catch (Exception ex) {
        this.sshHelper.getEquipmentLogger().error("runCommand - command execution failed: cound not establish ssh connection. "
            + "Exception error message : " + ex.getMessage());
        throw new EqCommandTagException("command execution failure: cound not establish ssh connection. error message : " + ex.getMessage());
      }
    }

    /*
     * Create the remote console to use for command execution. Here we
     * redirect stderr of all sessions started with this console to our own
     * stderr (NOTE: stdout is NOT redirected here but is instead fetched
     * below).
     */
    try {

      SSH2ConsoleRemote console = new SSH2ConsoleRemote(client.getConnection(), null, System.err);

      /*
       * Run the command (returns a boolean indicating success, we ignore
       * it here). Here we don't redirect stdout and stderr but use the
       * internal streams of the session channel instead.
       */

      console.command(cmdLine);

      /*
       * Fetch the internal stdout stream and wrap it in a BufferedReader
       * for convenience.
       */
      BufferedReader stdout = new BufferedReader(new InputStreamReader(console.getStdOut()));

      /*
       * Read all output sent to stdout (line by line) and print it to our
       * own stdout.
       */

      StringBuffer feedbackBuffer = new StringBuffer("");
      String line;
      while ((line = stdout.readLine()) != null) {
        feedbackBuffer.append(line);
      }

      String feedback = feedbackBuffer.toString();

      this.sshHelper.getEquipmentLogger().debug("runCommand - received execution feedback: " + feedback);

      /*
       * Retrieve the exit status of the command (from the remote end).
       */
      int exitStatus = console.waitForExitStatus();

      // if the execution code indicates possible error ..
      if (exitStatus != SSHHelper.EXIT_CODE_SUCCESS) {
        throw new EqCommandTagException(exitStatus, "The execution ended with exit code : " + exitStatus);
      } else { 
        // if the exit status is fine
        if (address.isXMLProtocolConfigured()) {
          SSHXMLExecutionFeedback execFeedback = this.sshHelper.parseSSHExecutionFeedback(feedback);
          if (execFeedback.statusCode != SSHHelper.STATUS_EXECUTION_OK) {
            throw new EqCommandTagException(execFeedback.statusCode, execFeedback.statusDescription);
          }
        }
      }

      /*
       * NOTE: at this point System.out will be closed together with the
       * session channel of the console
       */

      /*
       * Disconnect the transport layer gracefully
       */
      client.getTransport().normalDisconnect("User disconnects");

    } catch (java.io.IOException ex) {
      this.sshHelper.getEquipmentLogger().error("runCommand - command execution failed: could not open socket. exception error message : " + ex.getMessage());
      throw new EqCommandTagException("command execution failure: could not open socket. error message : " + ex.getMessage());
    } catch (EqCommandTagException ex) {
      this.sshHelper.getEquipmentLogger().warn("runCommand - execution failed. feedback from the source : " + ex.getErrorDescription());
      throw ex;
    } catch (Exception ex) {
      this.sshHelper.getEquipmentLogger().error("runCommand - command execution failed. exception error message : " + ex.getMessage());
      throw new EqCommandTagException("command execution failure: could not open socket. error message : " + ex.getMessage());
    }

    return null;
  }
}
