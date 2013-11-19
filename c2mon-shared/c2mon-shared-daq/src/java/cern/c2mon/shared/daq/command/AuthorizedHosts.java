package cern.c2mon.shared.daq.command;


import java.util.regex.Pattern;

import java.io.Serializable;
/**
 * @author J. Stowisek
 * @version $Revision: 1.5 $ ($Date: 2007/07/04 12:38:54 $ - $State: Exp $)
 */

public class AuthorizedHosts implements Serializable {

  /**
   * 
   */
  protected static final String HOSTLIST_REGEX = "\\*|([*a-zA-Z0-9_-]+(\\.[*a-zA-Z0-9_]+)*)(\\,([*a-zA-Z0-9_-]+(\\.[*a-zA-Z0-9_]+)*))*";

  /**
   * Transient so not shared in distributed cache.
   * The constructor will always set this from the
   * text field when loaded in the local JVM.
   */
  protected transient Pattern pattern;
  
  /**
   * String version of pattern.
   */
  private String patternStr;
  
  /**
   * Sets the pattern field from the pattern String.
   * Called by TC when instantiating on node.
   */
  public void parsePattern() {
    if (isValidPattern(patternStr)) {
      patternStr = patternStr.replaceAll("\\.", "\\\\.");
      patternStr = patternStr.replaceAll("\\*", "\\.\\*");
      String[] patterns = patternStr.split(",");
      StringBuffer str = new StringBuffer();

      for (int i = 0; i < patterns.length; i++) {
        str.append("(");
        str.append(patterns[i]);
        str.append(")*");
      }
      patternStr = str.toString();
      this.pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
    }
  }
  
  /**
   * 
   */
  public AuthorizedHosts(String patternStr) {
    this.patternStr = patternStr;
    parsePattern();
  }

  /**
   * 
   */
  public boolean isValidHost(String hostName) {
    if (hostName.equals("")) {
      return false;
    } else {
      return pattern.matcher(hostName).matches();
    }
  }

  /**
   * 
   */
  public static final boolean isValidPattern(String pattern) {
    return pattern != null && pattern.matches(HOSTLIST_REGEX);
  }

  /**
   * 
   */
  public static void main(String[] args) {
    String patternStr;
    String host;
    AuthorizedHosts h;

    patternStr = "cs-ccr-tim*.cern.ch";
    h = new AuthorizedHosts(patternStr);
    System.out.println(
        "Pattern: " + patternStr + " (ok= "
        + AuthorizedHosts.isValidPattern(patternStr) + ")");
    host = "tcrpl1.cern.ch";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "cs-ccr-tim4.cern.ch";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "TCPL5.cern.ch";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "TCPL5";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    patternStr = "tcrpl*";
    h = new AuthorizedHosts(patternStr);
    System.out.println(
        "Pattern: " + patternStr + " (ok= "
        + AuthorizedHosts.isValidPattern(patternStr) + ")");
    host = "tcrpl1.cern.ch";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "TCRPL5.cern.ch";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "TCPL5.cern.ch";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "TCPL5";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    patternStr = "tcrpl*,pcst*";
    h = new AuthorizedHosts(patternStr);
    System.out.println(
        "Pattern: " + patternStr + " (ok= "
        + AuthorizedHosts.isValidPattern(patternStr) + ")");
    host = "tcrpl1.cern.ch";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "TCRPL5.cern.ch";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "TCPL5.cern.ch";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "TCPL5";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    patternStr = "*";
    h = new AuthorizedHosts(patternStr);
    System.out.println(
        "Pattern: " + patternStr + " (ok= "
        + AuthorizedHosts.isValidPattern(patternStr) + ")");
    patternStr = "tcrpl1.cern.ch,tcrpl2.cern.ch,tcrpl5";
    h = new AuthorizedHosts(patternStr);
    System.out.println(
        "Pattern: " + patternStr + " (ok= "
        + AuthorizedHosts.isValidPattern(patternStr) + ")");
    host = "tcrpl1.cern.ch";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "TCRPL5.cern.ch";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "TCPL5.cern.ch";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "TCPL5";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
    host = "";
    System.out.println(
        "Host: " + host + " (matches= " + h.isValidHost(host) + ")");
  }
}
