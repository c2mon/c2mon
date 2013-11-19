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
package cern.c2mon.shared.daq.process.backward;

import org.w3c.dom.Element;
 
/**
 * Class for encoding and decoding ProcessDisconnection messages.
 * A DAQ process is expected to send a ProcessDisconnection message to
 * the server before shutting down. This is necessary for the application
 * server to know that a DAQ process was stopped rather than the victim
 * of a violent crash.
 * 
 * The disconnection message uniquely identifies a DAQ process by its name
 * and also carries the startup time as a "security question". 
 * 
 * @author J. Stowisek
 * @version $Revision: 1.5 $ ($Date: 2007/07/04 12:38:56 $ - $State: Exp $) 
 */
 
public class ProcessDisconnectionBC implements ProcessRequestBC {
 
 public static final String XML_ROOT_ELEMENT = "ProcessDisconnection";
 
 private static final String XML_ATTRIBUTE_PROCESS_ID = "id";
 
 private static final String XML_ATTRIBUTE_PROCESS_NAME = "name";
 
 private static final String XML_ATTRIBUTE_STARTUP_TIME = "startup-time";
 
 /**
   * Identifier of the process to be disconnected.
   */
 private Long id = null;
 
 /** 
   * Name of the process to be disconnected. 
   */
 private String name = null;
 
 /**
   * Exact time when the process was started. 
   * This timestamp is used by the server to verify that the message comes from
   * a process that is actually running.
   */
 private long startupTime;
 
 
 /**
   * Constructor.
   * @param pName process name
   * @param pStartupTime time when the process was started.
   * This startup time MUST MATCH the timestamp that was sent with the
   * ProcessConnectionRequest message.
   */
 public ProcessDisconnectionBC(
     final Long pId, final String pName, final long pStartupTime
 ) {
   this.id = pId;
   this.name = pName;
   this.startupTime = pStartupTime;
 }
 
 /**
   * Constructor.
   * @param pName process name
   * @param pStartupTime time when the process was started.
   * This startup time MUST MATCH the timestamp that was sent with the
   * ProcessConnectionRequest message.
   */
 public ProcessDisconnectionBC(
     final String pName, final long pStartupTime
 ) {
   this(null, pName, pStartupTime);
 }
 
 public Long getProcessId() {
   return this.id;
 }
 
 public String getProcessName() {
   return this.name;
 }
 
 public long getStartupTime() {
   return this.startupTime;
 }
 
 /**
   * Creates a ProcessDisconnection object from an XML Element. Never returns null.
   * @param domElement Root element of the XML document containing the ProcessDisconnection XML.
   */
 public static ProcessDisconnectionBC fromXML(final Element domElement) {
   ProcessDisconnectionBC result = null;
 
   if (domElement.getNodeName().equals(XML_ROOT_ELEMENT)) {
     String idStr = domElement.getAttribute(XML_ATTRIBUTE_PROCESS_ID);
     if (idStr == null || idStr.length() == 0) {
       result = new ProcessDisconnectionBC(
         domElement.getAttribute(XML_ATTRIBUTE_PROCESS_NAME),
         Long.parseLong(domElement.getAttribute(XML_ATTRIBUTE_STARTUP_TIME))
       );
     }
     else {
       result = new ProcessDisconnectionBC(
         new Long(idStr),
         domElement.getAttribute(XML_ATTRIBUTE_PROCESS_NAME),
         Long.parseLong(domElement.getAttribute(XML_ATTRIBUTE_STARTUP_TIME))
       );
     }
     return result;
   } else {
     throw new RuntimeException("Called the fromXML message on the wrong XML doc Element! - check your code as this should be checked first"); //TODO use new type of Exception here? - YES, that should be caught 
   }
 
 }
 
 /**
   * Return an XML representation of the ProcessDisconnection object.
   * Internally, this method calls getXML(); the output of both messages is thus
   * identical.
   * @return an XML representation of the ProcessDisconnection object
   */
 public String toXML() {
   return getXML(this.id, this.name, this.startupTime);
 }
 
 /**
   * Generates the contents of the XML message to be sent by the DAQ process.
   * @param pId process identified
   * @param pName process name
   * @param pStartupTime process startup time. 
   * @return the contents of the XML message to be sent by the DAQ process.
   */
 public static String getXML(final Long pId, final String pName, final long pStartupTime) {
   StringBuffer sbuffer = new StringBuffer();
   sbuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
   sbuffer.append('<');
   sbuffer.append(XML_ROOT_ELEMENT);
   sbuffer.append(' ');
   if (pId != null) {
     sbuffer.append(XML_ATTRIBUTE_PROCESS_ID);
     sbuffer.append("=\"");
     sbuffer.append(pId);
     sbuffer.append("\" ");
   }
   sbuffer.append(XML_ATTRIBUTE_PROCESS_NAME);
   sbuffer.append("=\"");
   sbuffer.append(pName);
   sbuffer.append("\" ");
   sbuffer.append(XML_ATTRIBUTE_STARTUP_TIME);
   sbuffer.append("=\"");
   sbuffer.append(pStartupTime);
   sbuffer.append("\" />\n");
   return sbuffer.toString();
 }
 
}
