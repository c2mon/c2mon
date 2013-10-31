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
package cern.c2mon.daq.ssh.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import cern.c2mon.daq.common.EquipmentLogger;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.daq.tools.TIMDriverSimpleTypeConverter;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.daq.tools.equipmentexceptions.EqDataTagException;
import cern.c2mon.daq.tools.equipmentexceptions.EqException;
import cern.tim.shared.common.datatag.address.SSHHardwareAddress;
import cern.tim.shared.daq.datatag.ISourceDataTag;

import com.mindbright.jca.security.SecureRandom;
import com.mindbright.ssh2.SSH2ConsoleRemote;
import com.mindbright.ssh2.SSH2Exception;
import com.mindbright.ssh2.SSH2SimpleClient;
import com.mindbright.ssh2.SSH2Transport;
import com.mindbright.util.RandomSeed;
import com.mindbright.util.SecureRandomAndPad;

/**
 * Helper class implementing some common methods used all over the SSH daq
 * 
 * @author vilches
 *
 */
public class SSHHelper {

  public static final int STATUS_EXECUTION_OK = 0;
  public static final int EXIT_CODE_SUCCESS = 0;

  private String default_server_alias = null;
  private String default_ssh_key = null;
  private String default_key_passphrase = null;

  private String default_user_name = null;
  private String default_user_password = null;

  public static final int STATUS_EXECUTION_FAILED = -1;

  /**
   * Constants definining the xml elements name that appear in the xml
   * feedback message
   */
  public static final String EXECUTION_STATUS_ROOT = "execution-status";
  public static final String EXECUTION_STATUS_STATUS_CODE = "status-code";
  public static final String EXECUTION_STATUS_STATUS_DESCR = "status-description";
  public static final String EXECUTION_STATUS_VALUE = "value";
  public static final String EXECUTION_STATUS_VALUE_TYPE = "type";

  /**
   * Constants definining all possible fields of the equipment address
   */
  public static final String DEFAULT_SERVER_ALIAS = "default_server_alias";
  public static final String DEFAULT_USER_NAME = "default_user_name";
  public static final String DEFAULT_USER_PASSWORD = "default_user_password";
  public static final String SSH_KEY = "default_ssh_key";
  public static final String KEY_PASSPHRASE = "default_key_passphrase";

  /**
   * defines the max. number of attempts, why executing remote commands to
   * recalculate value for a tag in case of MAX_ERRORS one by one failures, the
   * handler stops stops does not try any more, assuming that there's a
   * problem with the configuration
   */
  public static final int MAX_ERRORS = 5;

  public static final String PROTOCOL_XML = "xml";
  public static final String PROTOCOL_SIMPLE_IO = "simple-io";

  /**
   * The equipment logger of this class.
   */
  private EquipmentLogger equipmentLogger;

  /**
   * The equipment configuration of this handler.
   */
  private IEquipmentConfiguration equipmentConfiguration;

  /**
   * The equipment message sender to send to the server.
   */
  private IEquipmentMessageSender equipmentMessageSender;

  /**
   * Constructor
   * @param address
   * @throws EqException
   */
  public SSHHelper(EquipmentLogger equipmentLogger, IEquipmentConfiguration equipmentConfiguration, 
      IEquipmentMessageSender equipmentMessageSender) {
    this.equipmentLogger = equipmentLogger;
    this.equipmentConfiguration = equipmentConfiguration;
    this.equipmentMessageSender = equipmentMessageSender;
  }


  public void parseExuipmentAddress(String address) throws EqException {
    this.equipmentLogger.debug("parseExuipmentAddress - entering parseEquipmentAddress()..");
    // example address :
    // default_server_alias=TIMDAQTEST01:22;default_user_name=timtest;default_user_password=passwd
    // or :
    // default_server_alias=TIMDAQTEST01:22;default_user_name=timtest;default_user_name=timtest;
    // default_user_password=password;default_ssh_key=$HOME/.ssh/key_priv;

    // get the relevent information from the address string
    StringTokenizer tokens = new StringTokenizer(this.equipmentConfiguration.getAddress(), ";");
    String token = "";
    String token2 = "";
    while (tokens.hasMoreTokens()) {
      token = tokens.nextToken();
      StringTokenizer tokens2 = new StringTokenizer(token, "=");
      while (tokens2.hasMoreTokens()) {
        token2 = tokens2.nextToken();
        if (token2.trim().equalsIgnoreCase(DEFAULT_SERVER_ALIAS)) {
          this.default_server_alias = tokens2.nextToken().trim();
          this.equipmentLogger.debug("\tdefault_server_alias=" + this.default_server_alias);
        } else if (token2.trim().equalsIgnoreCase(DEFAULT_USER_NAME)) {
          this.default_user_name = tokens2.nextToken().trim();
          this.equipmentLogger.debug("\tdefault_user_name=" + this.default_user_name);
        } else if (token2.trim().equalsIgnoreCase(DEFAULT_USER_PASSWORD)) {
          this.default_user_password = tokens2.nextToken().trim();
          this.equipmentLogger.debug("\tdefault_user_password=" + this.default_user_password);
        } else if (token2.trim().equalsIgnoreCase(SSH_KEY)) {
          // String s1 = tokens2.nextToken();
          // String s2 = this.parseEnvironmentVariables(s1);
          this.default_ssh_key = this.parseEnvironmentVariables(tokens2.nextToken().trim());
          this.equipmentLogger.debug("\tdefault_ssh_key=" + this.default_ssh_key);
          // this.default_ssh_key =
          // this.parseEnvironmentVariables(s2);

          // this.equipmentLogger.debug("\tparsing eq.address s1 : "+s1);
          // this.equipmentLogger.debug("\tparsing eq.address s2 : "+s2);
        } else if (token2.trim().equalsIgnoreCase(KEY_PASSPHRASE)) {
          this.default_key_passphrase = tokens2.nextToken().trim();
          this.equipmentLogger.debug("\tdefault_key_passphrase=" + this.default_key_passphrase);
        }
      }// while tokens2
    }// while tokens

    this.equipmentLogger.debug("parseExuipmentAddress - leaving parseExuipmentAddress()");
  }

  /**
   * Called periodcally by background timer threads to recalculate a value of
   * the specified tag
   * 
   * @param dtagId
   *            - the identifier of the tag to recalculate
   */
  public void recalculateDataTagValue(Long dtagId) throws EqDataTagException {
    this.equipmentLogger.debug("recalculateDataTagValue - entering recalculateDataTagValue()..");

    this.equipmentLogger.debug("\trecalculateDataTagValue - trying to recalculate value of a tag id : " + dtagId);

    ISourceDataTag sdt = this.equipmentConfiguration.getSourceDataTags().get(dtagId);

    SSHHardwareAddress address = null;

    // try to get the command-tag's address
    try {
      address = (SSHHardwareAddress) sdt.getHardwareAddress();
    } catch (ClassCastException ex) {
      this.equipmentLogger.error("recalculateDataTagValue - Invalid source datatag definition. Could not cast the address to class SSHHardwareAddress");
      throw new EqDataTagException("Invalid source datatag definition. Could not cast the address to class SSHHardwareAddress");
    }

    String server = establishHost(address);
    // this.equipmentLogger.debug("\tserver : "+server);
    String key = establishSshKey(address);
    // this.equipmentLogger.debug("\tkey : "+key);
    String key_pass = establishSshPasshprase(address);
    // this.equipmentLogger.debug("\tkey_pass : "+key_pass);
    String user = establishUser(address);
    // this.equipmentLogger.debug("\tuser : "+user);
    String passwd = establishUserPassword(address);
    // this.equipmentLogger.debug("\tpasswd : "+passwd);

    int port = getServerPort(server);
    // this.equipmentLogger.debug("\tport : "+port);

    String cmdLine = address.getSystemCall();
    // this.equipmentLogger.debug("\tsystem-call : "+cmdLine);

    SSH2SimpleClient client = null;

    // if there's no ssh_key defined, use the user-name & password
    if (key == null) {
      try {
        this.equipmentLogger.debug("recalculateDataTagValue - trying to authenticate using the following credentials :");
        this.equipmentLogger.debug("\t server : " + server);
        this.equipmentLogger.debug("\t port : " + port);
        this.equipmentLogger.debug("\t user : " + user);
        this.equipmentLogger.debug("\t passwd : " + passwd);

        client = authenticate(server, port, user, passwd);
      } catch (Exception ex) {
        this.equipmentLogger.error("recalculateDataTagValue - datatag execution failed: cound not establish ssh connection. exception error message : " + ex.getMessage());
        throw new EqDataTagException("datatag execution failure: cound not establish ssh connection. error message : " + ex.getMessage());
      }
    } else {
      try {
        this.equipmentLogger.debug("recalculateDataTagValue - trying to authenticate using the following credentials :");
        this.equipmentLogger.debug("\t server : " + server);
        this.equipmentLogger.debug("\t port : " + port);
        this.equipmentLogger.debug("\t user : " + user);
        this.equipmentLogger.debug("\t key : " + key);
        this.equipmentLogger.debug("\t key_pass : " + key_pass);

        client = authenticateUsingKey(server, port, user, key, key_pass);
      } catch (Exception ex) {
        this.equipmentLogger.error("recalculateDataTagValue - datatag execution failed: cound not establish ssh connection. exception error message : " + ex.getMessage());
        throw new EqDataTagException("datatag execution failure: cound not establish ssh connection. error message : " + ex.getMessage());
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

      String feedback = "";
      String line;
      while ((line = stdout.readLine()) != null) {
        feedback += line;
      }

      this.equipmentLogger.debug("\t recalculateDataTagValue - received execution feedback : " + feedback);

      /*
       * Retrieve the exit status of the command (from the remote end).
       */
      int exitStatus = console.waitForExitStatus();

      // if the execution code indicates possible error ..
      if (exitStatus != EXIT_CODE_SUCCESS) {
        throw new EqDataTagException(exitStatus, "The execution ended with exit code : " + exitStatus);
      } else // if the exit status is fine
      {
        if (address.isXMLProtocolConfigured()) {
          SSHXMLExecutionFeedback execFeedback = parseSSHExecutionFeedback(feedback);
          if (execFeedback.statusCode != STATUS_EXECUTION_OK)
            throw new EqDataTagException(execFeedback.statusCode, execFeedback.statusDescription);
        }
      }

      this.equipmentMessageSender.sendTagFiltered(sdt, TIMDriverSimpleTypeConverter.convert(sdt, feedback), System.currentTimeMillis());

      /*
       * Disconnect the transport layer gracefully
       */
      client.getTransport().normalDisconnect("recalculateDataTagValue - User disconnects");

    } catch (java.io.IOException ex) {
      this.equipmentLogger.error("recalculateDataTagValue - command execution failed: could not open socket. exception error message : " + ex.getMessage());
      throw new EqDataTagException("command execution failure: could not open socket. error message : " + ex.getMessage());
    } catch (EqDataTagException ex) {
      this.equipmentLogger.error("recalculateDataTagValue - command execution failed. execution error message : " + ex.getMessage());
      throw ex;
    } catch (Exception ex) {
      this.equipmentLogger.error("recalculateDataTagValue - command execution failed. exception error message : " + ex.getMessage());
      throw new EqDataTagException("command execution failure" + ex.getMessage());
    }

    this.equipmentLogger.debug("recalculateDataTagValue - leaving recalculateDataTagValue()");
  }

  /**
   * Parses the execution feedback XML
   * 
   * @return SSHExecutionFeedback - with the status exectution code and
   *         description
   * @param xmlFeedback
   */
  public SSHXMLExecutionFeedback parseSSHExecutionFeedback(String xmlFeedback) throws EqCommandTagException {
    this.equipmentLogger.debug("parseSSHExecutionFeedback - entering parseSSHExecutionFeedback()..");
    /**
     * The started process should throw the following XML output on stdout:
     * 
     * <?xml version="1.0"?> <execution-status> <status-code>0</status-code>
     * <status-description> blah blah blah..</status-description> <value
     * type="java.l  ang.Integer">0</value> </execution-status>"
     */
    DOMParser parser = new DOMParser();
    Document feedbackDoc = null;

    int statusCode = -1;
    String statusDescr = null;
    Object value = null;
    String type = null;

    try {
      Reader in = new StringReader(xmlFeedback);
      InputSource source = new InputSource(in);
      parser.parse(source);
      in.close();
      feedbackDoc = parser.getDocument();

      // get the root element of the document
      Element rootElem = feedbackDoc.getDocumentElement();

      NodeList rootChildren = rootElem.getChildNodes();
      // go through all children
      for (int i = 0; i < rootChildren.getLength(); i++) {
        Node childNode = rootChildren.item(i);
        if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals(EXECUTION_STATUS_STATUS_CODE)) {
          statusCode = Integer.parseInt(childNode.getFirstChild().getNodeValue());
        } else if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals(EXECUTION_STATUS_STATUS_DESCR)) {
          statusDescr = childNode.getFirstChild().getNodeValue();
        } else if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals(EXECUTION_STATUS_VALUE)) {
          type = ((Element) childNode.getFirstChild()).getAttribute(EXECUTION_STATUS_VALUE_TYPE);
          String errMessage = "parseSSHExecutionFeedback - error detected while trying to parse the xml feedback";
          if (type.equals("java.lang.Integer")) {
            try {
              value = new Integer(childNode.getFirstChild().getNodeValue());
            } catch (Exception ex) {
              this.equipmentLogger.error(errMessage, ex);
              throw new EqCommandTagException(errMessage);                            
            }
          } else if (type.equals("java.lang.Boolean")) {
            try {
              value = new Boolean(childNode.getFirstChild().getNodeValue());
            } catch (Exception ex) {
              this.equipmentLogger.error(errMessage, ex);
              throw new EqCommandTagException(errMessage);                            
            }
          } else if (type.equals("java.lang.Long")) {
            try {
              value = new Long(childNode.getFirstChild().getNodeValue());
            } catch (Exception ex) {
              this.equipmentLogger.error(errMessage, ex);
              throw new EqCommandTagException(errMessage);                            
            }
          } else if (type.equals("java.lang.Float")) {
            try {
              value = new Float(childNode.getFirstChild().getNodeValue());
            } catch (Exception ex) {
              this.equipmentLogger.error(errMessage, ex);
              throw new EqCommandTagException(errMessage);
            }
          } else if (type.equals("java.lang.Double")) {
            try {
              value = new Double(childNode.getFirstChild().getNodeValue());
            } catch (Exception ex) {   
              this.equipmentLogger.error(errMessage, ex);
              throw new EqCommandTagException(errMessage);                            
            }
          } else if (type.equals("java.lang.String")) {
            value = new Integer(childNode.getFirstChild().getNodeValue());
          } else {
            throw new EqCommandTagException("parseSSHExecutionFeedback - error detected while trying to parse the xml feedback: unexpected type.");
          }
        }
      }// for

    } catch (java.io.IOException ex) {
      this.equipmentLogger.error("\tparseSSHExecutionFeedback - could not parse the xml-feedback stream. ex.error = " + ex.getMessage(), ex);
      // return null;
      throw new EqCommandTagException(ex.getMessage());
    } catch (org.xml.sax.SAXException ex) {
      this.equipmentLogger.error("\tparseSSHExecutionFeedback - could not parse the xml-feedback stream. ex.error = " + ex.getMessage(), ex);
      throw new EqCommandTagException(ex.getMessage());
      // return null;
    } catch (NumberFormatException ex) {
      this.equipmentLogger.error("\tparseSSHExecutionFeedback - could not parse the xml-feedbck stream. ex. error = " + ex.getMessage(), ex);
      throw new EqCommandTagException(ex.getMessage());
      // return null;
    }

    this.equipmentLogger.debug("parseSSHExecutionFeedback - leaving parseSSHExecutionFeedback()");
    return new SSHXMLExecutionFeedback(statusCode, statusDescr, type, value);
  }

  /**
   * This private method (used mainly for ssh_key path in eq. address) is used
   * for obtaining environment variables $HOME & $USER from the string
   * 
   * @return String with the variables replaced by their values obtained from
   *         the system
   * @param pPath
   */
  public String parseEnvironmentVariables(String pPath) {
    // e.g : ${HOME}/aaaa/bbbb/ccccc/$USER/.ssh/key
    // $USER, $HOME, ~

    if (pPath == null)
      return null;

    String USER = "USER";
    String HOME = "HOME";

    StringTokenizer tokens = new StringTokenizer(pPath, "/\\");

    StringBuffer output = new StringBuffer("");

    if (pPath.startsWith("/"))
      output.append("/");
    else if (pPath.startsWith("\\"))
      output.append("\\");

    String token = "";
    String token2 = "";
    while (tokens.hasMoreTokens()) {
      token = tokens.nextToken();
      if (token.startsWith("$")) {
        // get rid of '$'
        String subToken = token.substring(1);
        if (subToken.equals(USER) || subToken.equals("{" + USER + "}")) {
          output.append(System.getProperty("user.name"));
        } else if (subToken.equals(HOME) || subToken.equals("{" + HOME + "}")) {
          output.append(System.getProperty("user.home"));
        }
      } else {
        output.append(token);
      }

      if (tokens.hasMoreTokens()) {
        output.append("/");
      }
    }// while

    return output.toString();
  }

  /**
   * This private method is used by the handler for ssh client authentication
   * using user-name & password
   * 
   * @return
   * @param userPassword
   * @param userName
   * @param serverAlias
   */
  public SSH2SimpleClient authenticate(String serverAlias, int port, String userName, String userPassword) throws Exception {
    this.equipmentLogger.debug("authenticate - entering authenticate()..");

    /*
     * this.equipmentLogger.debug("\ttrying to authenticate client using the following credentials :"
     * ); this.equipmentLogger.debug("\t\t server-alias : "+serverAlias);
     * this.equipmentLogger.debug("\t\t port : "+port);
     * this.equipmentLogger.debug("\t\t user-name : "+userName);
     * this.equipmentLogger.debug("\t\t user-password : "+userPassword);
     */

    SSH2SimpleClient client = null;
    try {
      Socket serverSocket = new Socket(serverAlias, port);
      SSH2Transport transport = new SSH2Transport(serverSocket, createSecureRandom());
      client = new SSH2SimpleClient(transport, userName, userPassword);
    } catch (java.io.IOException ex) {
      throw ex;
    } catch (SSH2Exception ex) {
      throw ex;
    }

    this.equipmentLogger.debug("\tauthenticate - authentication SUCCESSFUL");
    this.equipmentLogger.debug("authenticate - leaving authenticate()");
    return client;
  }

  /**
   * This private method is used by the handler for ssh client authentication
   * using ssh rsa key
   * 
   * @return
   * @param keyPass
   * @param keyPath
   * @param serverAlias
   */
  public SSH2SimpleClient authenticateUsingKey(String serverAlias, int port, String userName, String keyPath, String keyPass) throws Exception {
    this.equipmentLogger.debug("authenticateUsingKey - entering authenticateUsingKey()..");
    /*
     * this.equipmentLogger.debug("\ttrying to authenticate client using the following credentials :"
     * ); this.equipmentLogger.debug("\t\t server-alias : "+serverAlias);
     * this.equipmentLogger.debug("\t\t port : "+port);
     * this.equipmentLogger.debug("\t\t user-name : "+userName);
     * this.equipmentLogger.debug("\t\t key-path : "+keyPath);
     * this.equipmentLogger.debug("\t\t key-passphrase : "+keyPass);
     */

    SSH2SimpleClient client = null;

    try {
      Socket serverSocket = new Socket(serverAlias, port);
      SSH2Transport transport = new SSH2Transport(serverSocket, createSecureRandom());
      client = new SSH2SimpleClient(transport, userName, keyPath, keyPass);
    } catch (Exception ex) {
      throw ex;
    }

    this.equipmentLogger.debug("\tauthenticateUsingKey - authentication SUCCESSFUL");
    this.equipmentLogger.debug("authenticateUsingKey - leaving authenticateUsingKey()");
    return client;
  }

  /**
   * @return com.mindbright.util.SecureRandomAndPad
   * @roseuid 43302FDC02A1
   */
  public static SecureRandomAndPad createSecureRandom() {
    /*
     * NOTE, this is how it should be done if you want good randomness,
     * however good randomness takes time so we settle with just some
     * low-entropy garbage here.
     * 
     * RandomSeed seed = new RandomSeed("/dev/random", "/dev/urandom");
     * byte[] s = seed.getBytesBlocking(20); return new
     * SecureRandomAndPad(new SecureRandom(s));
     */
    byte[] seed = RandomSeed.getSystemStateHash();
    return new SecureRandomAndPad(new SecureRandom(seed));
  }

  /**
   * 
   * @param serverAlias
   * @return
   */
  public int getServerPort(String serverAlias) {
    String srv = serverAlias;
    // the default port
    int port = 22;

    if (srv == null)
      return port;

    // check if there was different port specified (e.g. TIM_DAQSRV1:22)
    int i = srv.indexOf(':');
    if (i != -1) {
      String p = srv.substring(i + 1);
      srv = srv.substring(0, i);
      port = Integer.parseInt(p);
    }

    return port;
  }

  public String establishHost(final SSHHardwareAddress adddr) {
    String server = null;
    if ((server = adddr.getServerAlias()) == null) {
      // unless the server alias is specified inside the command tag, take
      // the default value
      // from the equipment's address
      if ((server = this.default_server_alias) == null) {
        try {
          InetAddress addr = InetAddress.getLocalHost();
          server = addr.getHostName();
        } catch (Exception ex) {
          this.equipmentLogger.error("establishHost - could not establish the host's name");
        }
      }// if
    }// if

    return server;
  }

  /**
   * 
   * @param adddr
   * @return
   */
  public String establishSshKey(final SSHHardwareAddress adddr) {
    String key = null;

    if ((key = parseEnvironmentVariables(adddr.getSshKey())) == null) {
      key = this.default_ssh_key;
    }

    return key;
  }

  /**
   * 
   * @param adddr
   * @return
   */
  public String establishSshPasshprase(final SSHHardwareAddress adddr) {
    String key_pass = null;
    if ((key_pass = adddr.getKeyPassphrase()) == null) {
      key_pass = this.default_key_passphrase;
    }

    return key_pass;
  }

  /**
   * 
   * @param adddr
   * @return
   */
  public String establishUser(final SSHHardwareAddress adddr) {
    String user = null;
    // if the user is not specified for the command, take the default one
    if ((user = adddr.getUserName()) == null) {
      // if the user is not specified for the command, take the default
      // one
      if ((user = this.default_user_name) == null)
        user = System.getProperty("user.name");
    }

    return user;
  }

  /**
   * 
   * @param adddr
   * @return
   */
  public String establishUserPassword(final SSHHardwareAddress adddr) {
    String passwd = null;
    if ((passwd = adddr.getUserPassword()) == null) {
      // if the password is not specified for the command, take the
      // default one
      passwd = this.default_user_password;
    }

    return passwd;
  }

  public String getServer(String serverAlias) {
    String srv = serverAlias;
    if (srv == null)
      return srv;

    // check if there was different port specified (e.g. TIM_DAQSRV1:22)
    int i = srv.indexOf(':');
    if (i != -1) {
      String p = srv.substring(i + 1);
      srv = srv.substring(0, i);
    }
    return srv;
  }

  /**
   * @return the equipmentConfiguration
   */
  public IEquipmentConfiguration getEquipmentConfiguration() {
    return this.equipmentConfiguration;
  }

  /**
   * @return the equipmentLogger
   */
  public EquipmentLogger getEquipmentLogger() {
    return this.equipmentLogger;
  }

  /**
   * @return the equipmentMessageSender
   */
  public IEquipmentMessageSender getEquipmentMessageSender() {
    return this.equipmentMessageSender;
  }

}
