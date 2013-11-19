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

import java.net.InetAddress;
import java.net.UnknownHostException;
 
import java.sql.Timestamp;
 
import javax.jms.Destination;
 
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
 
import cern.c2mon.util.parser.ParserException;
 
/**
 * @author J. Stowisek
 * @version $Revision: 1.8 $ ($Date: 2007/03/07 09:48:19 $ - $State: Exp $) 
 */
public class ProcessConnectionRequestBC implements ProcessRequestBC {
 // ----------------------------------------------------------------------------
 // CONSTANT DEFINITIONS
 // ----------------------------------------------------------------------------
 public static final String XSD_URL = "http://cern.ch/ts-project-tim/xml/ProcessConnectionRequest.xsd";
 
 public static final String XML_ROOT_ELEMENT = "ProcessConnection";
 
 /**
   * Unique name of the Process that wishes to connect.
   */
 protected String name = null;
 
 /**
   * Name of the host on which the process is running.
   */
 protected String hostname = null;
 
 /**
   * Time when the process was started.
   */
 protected Timestamp startupTime = null;
 
 /**
   * Name of the JMS Topic on which the Process will listen for a reply
   * from the server. Changed from String to Destination in TIM2 (not used in TIM1).
   */
 protected Destination replyQueue = null;
 
 private final static Logger LOGGER = Logger.getLogger(ProcessConnectionRequestBC.class);
 
 /**
   * Constructor
   * @param pName name of the Process that wishes to connect
   */
 public ProcessConnectionRequestBC(final String pName) {
   this(pName, new Timestamp(System.currentTimeMillis()));
 }
 
 /**
   * Constructor
   * @param pName name of the Process that wishes to connect
   * @param pStartumTime time when the Process was started
   */
 public ProcessConnectionRequestBC(final String pName, final Timestamp pStartupTime) {
   this.name = pName;
   this.startupTime = pStartupTime;
 
   try {
     this.hostname = InetAddress.getLocalHost().getHostName();
   } catch (UnknownHostException e) {
     this.hostname = null;
   }
 }
 
 /**
   * Internal Constructor
   * @param pName name of the Process that wishes to connect
   * @param pStartumTime time when the Process was started
   * @param pReplyTopic name of the JMS Topic on which the process listens for a reply from the server
   * @param pHostname name of the host on which the process is running
   */
 //TODO never used?
 protected ProcessConnectionRequestBC(String pName, Timestamp pStartupTime, Destination pReplyTopic, String pHostname) {
   this.name = pName;
   this.hostname = pHostname;
   this.startupTime = pStartupTime;
   this.replyQueue = pReplyTopic;
 }
 
 /**
   * Get the name of the process that wants to connect.
   */
 public String getProcessName() {
   return this.name;
 }
 
 /**
   * Get the name of the JMS topic on which the DAQ process listens for a
   * response from the server.
   */
 public Destination getReplyQueue() {
   return this.replyQueue;
 }
 
 /**
   * Get the name of the host on which the DAQ process is running.
   */
 public String getHostName() {
   return this.hostname;
 }
 
 /**
   * Get the start-up time of the DAQ process.
   */
 public Timestamp getStartupTime() {
   return this.startupTime;
 }
 
 /**
   * Create an XML representation of the ProcessConnectionRequest object.
   */
 public String toXML() {
   StringBuffer str = new StringBuffer(200);
   str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
   str.append('<');
   str.append(XML_ROOT_ELEMENT);
   str.append(" process-name=\"");
   str.append(this.name);
   str.append("\">\n");
 
   str.append("<hostname>");
   str.append(this.hostname);
   str.append("</hostname>\n");
   
   str.append("<startup-time>");
   str.append(this.startupTime.getTime());
   str.append("</startup-time>\n");
 
   str.append("<reply-topic>");
   str.append("null-not-used"); //TODO removed once old server no longer use (is set non-null here to be compatible with old tim-shared, which assumes non-null)
   str.append("</reply-topic>\n");
 
   str.append("</");
   str.append(XML_ROOT_ELEMENT);
   str.append(">\n");
   return str.toString();
 }
 
 /**
   * Create a ProcessConnectionRequest object from an XML structure.
   * The XML structure must be the one created by the toXML() method.
   * Throws an exception if passed a Element without the correct node name.
   * @throws ParserException if any problem occurs in the XML conversion
   */
 public static ProcessConnectionRequestBC fromXML(Element domElement) throws ParserException {    
 
   /* Only process if the element name is <ProcessConnection> */
   if (domElement.getNodeName().equals(XML_ROOT_ELEMENT)) {
     try {
       ProcessConnectionRequestBC result = new ProcessConnectionRequestBC(
           domElement.getAttribute("process-name"));
 
       /* Extract all elements */    
       NodeList fields = domElement.getChildNodes();
       int fieldsCount = fields.getLength();
       String fieldName;
       String fieldValueString;
       Node fieldNode;
       
       for (int i = 0; i != fieldsCount; i++) {
         fieldNode = fields.item(i);
    
         if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
           fieldName = fieldNode.getNodeName();
           if (fieldNode.getFirstChild() != null) {
             fieldValueString = fieldNode.getFirstChild().getNodeValue();
           } else {
             fieldValueString = null;
           }
           if (fieldName.equals("startup-time") && fieldValueString != null) {
             try {
               result.startupTime = new Timestamp(
                   Long.parseLong(fieldValueString));
             } catch (NumberFormatException e) {
               result.startupTime = null;
             }
           } else if (fieldName.equals("reply-topic")) {
             //TODO always set to null in DAQ - can be removed?
             result.replyQueue = null; //set to null as overwritten after this call in converter class
           } else if (fieldName.equals("hostname")) {
             result.hostname = fieldValueString;
           }
         } // if Element node
       } // for
       return result;
     } catch (Exception e) {
       throw new ParserException(e);
     }      
   } else {
     throw new RuntimeException("Called the fromXML() method on the wrong XML doc Element! - check your code as this should be checked first"); //TODO use new type of Exception here? - YES, that should be caught 
   }    
 }
 
 /**
   * @param replyTopic the replyTopic to set
   */
 public void setReplyQueue(Destination replyQueue) {
   this.replyQueue = replyQueue;
 }
}
