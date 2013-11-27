/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common.conf.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jms.TextMessage;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.support.converter.MessageConversionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.daq.common.messaging.impl.ProcessMessageSender;
import cern.c2mon.daq.tools.processexceptions.ConfRejectedTypeException;
import cern.c2mon.daq.tools.processexceptions.ConfUnknownTypeException;
import cern.c2mon.shared.daq.config.ConfigurationXMLConstants;
import cern.c2mon.shared.daq.process.ProcessConfigurationRequest;
import cern.c2mon.shared.daq.process.ProcessConfigurationResponse;
import cern.c2mon.shared.daq.process.ProcessConnectionResponse;
import cern.c2mon.shared.util.parser.ParserException;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

/**
 * Loads the configuration from the server or from a file.
 * 
 * @author Andreas Lang
 */
public class ProcessConfigurationLoader extends XMLTagValueExtractor implements ConfigurationXMLConstants {
  /**
   * The logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ProcessConfigurationLoader.class);

  /**
   * Position of the tim token in the JMS listener topic
   */
  private static final int TIM_TOKEN = 0;
  /**
   * Position of the process token in the JMS listener topic
   */
  private static final int PROCESS_TOKEN = 1;
  /**
   * Position of the command name token in the JMS listener topic
   */
  private static final int COMMAND_TOKEN = 2;
  /**
   * Position of the process name token in the JMS listener topic
   */
  private static final int PROCESS_NAME_TOKEN = 4;

  /**
   * Reference to the ProcessRequestSender (for requesting the XML config document). This reference is injected in the
   * Spring xml file, for ease of configuration.
   */
  @Autowired
  @Qualifier("primaryRequestSender")
  private ProcessRequestSender processRequestSender;

  private EquipmentConfigurationFactory equipmentConfigurationFactory;

  /**
   * The process message sender used during the creation of the configuration to send commFaults.
   */
  private ProcessMessageSender processMessageSender;


  @Autowired
  public void setEquipmentCononfigurationFactory(EquipmentConfigurationFactory eqConfFactory) {
    this.equipmentConfigurationFactory = eqConfFactory;
  }

  /**
   * Gets the Process configuration from the server and saves it to the provided location.
   * If ProcessConfigurationResponse returned null the DAQ start up process is stopped.
   * 
   * @param saveLocation The location to save the object.

   * 
   * @return The Process Configuration Response. It will never return null.
   */
  public ProcessConfigurationResponse getProcessConfiguration() {
    ProcessConfigurationResponse processConfigurationResponse;
    LOGGER.trace("getProcessConfiguration - getting Process Configuration");

    processConfigurationResponse = processRequestSender.sendProcessConfigurationRequest();

    if (processConfigurationResponse == null) {
      LOGGER.warn("getProcessConfiguration - Connection request to server: timeout waiting for server response.");
      LOGGER.info("getProcessConfiguration - Could not receive any configuration - stopping the DAQ process...");
      System.exit(0);
    }
    LOGGER.info("getProcessConfiguration - Configuration XML received from server and parsed");
    return processConfigurationResponse;
  }

  /**
   * Gets the Process Connection from the server and return it. If there is a TimeOut and 
   * there is no processConnectionResponse returned (null process PIK)the DAQ start up process is stopped.
   * 
   * @return The PIK as ProcessPIK class. It will never return null.
   */
  public final ProcessConnectionResponse getProcessConnection() {
    ProcessConnectionResponse processConnectionResponse;
    LOGGER.trace("getProcessConnection - getting Process Connection");

    // Ask for XML file to the server
    processConnectionResponse = processRequestSender.sendProcessConnectionRequest();

    if (processConnectionResponse == null) {
      LOGGER.warn("getProcessConnection - Connection request to server: timeout waiting for server response.");
      LOGGER.trace("getProcessConnection - Could not receive any Process Identifier Key (PIK) - stopping the DAQ start up process...");
      System.exit(0);
    }
    LOGGER.info("getProcessConnection - Process Identifier Key (PIK) received from server: " + processConnectionResponse.getProcessPIK());
    return processConnectionResponse;
  }

  /**
   * Loads the configuration from the file system.
   * 
   * @param file The location of the configuration xml on the file system.
   * @return The configuration as DOM Document.
   */
  public Document fromFiletoDOC(final String file) {
    Document confXMLDoc;
    LOGGER.trace("fromFiletoDOC - trying to configure process using configuration xml from the file " + file);

    DOMParser parser = new DOMParser();
    try {
      parser.parse(file);
      confXMLDoc = parser.getDocument();
    } catch (java.io.IOException ex) {
      LOGGER.error("fromFiletoDOC - Could not open processConfiguration XML file : " + file);
      return null;
    } catch (org.xml.sax.SAXException ex) {
      LOGGER.error("fromFiletoDOC - Could not parse processConfiguration XML file : " + file);
      return null;
    }

    LOGGER.trace("fromFiletoDOC - Configuration XML loaded from filesystem and parsed");
    return confXMLDoc;
  }

  /**
   * Loads the configuration from the XML file given by the server and stored in the 
   * 
   * @param xml The xml configuration.
   * @return The configuration as DOM Document.
   */
  public Document fromXMLtoDOC(final String xml) {
    Document confXMLDoc;
    LOGGER.trace("fromXMLtoDOC - trying to configure process using configuration XML");

    SimpleXMLParser parser = null;
    // Simple DOM parser for parsing XML message content
    try {
      parser = new SimpleXMLParser();
      confXMLDoc = parser.parse(xml);
    } catch (ParserConfigurationException e) {
      LOGGER.error("fromXMLtoDOC - Error creating instance of SimpleXMLParser");
      return null;
    } catch (ParserException ex) {        
      LOGGER.error("fromXMLtoDOC - Exception caught in DOM parsing processConfiguration XML");
      LOGGER.trace("fromXMLtoDOC - processConfiguration XML was: " + xml);          
      return null;
    }   

    LOGGER.trace("fromXMLtoDOC - Configuration XML loaded and parsed");
    return confXMLDoc;
  }

  /**
   * Takes the configuration DOM document and returns an ProcessConfiguration.
   * 
   * @param processName The name of the process.
   * @param processPIK The process PIK.
   * @param confXMLDoc the configuration XML document
   * @param localConfiguration flag indicating if the configuration is locally or from the server
   * @return The ProcessConfiguration object.
   * @throws ConfUnknownTypeException Thrown if the configuration has the type 'unknown'.
   * @throws ConfRejectedTypeException Thrown if the configuration has the type 'rejected'.
   */
  public ProcessConfiguration createProcessConfiguration(final String processName, final Long processPIK, 
      final Document confXMLDoc, final boolean localConfiguration) throws ConfUnknownTypeException, ConfRejectedTypeException {
    ProcessConfiguration processConfiguration = new ProcessConfiguration();
    // get the root element of the document
    Element rootElem = confXMLDoc.getDocumentElement();

    String confType = rootElem.getAttribute(TYPE_ATTRIBUTE);

    if (confType.equalsIgnoreCase(TYPE_ATTRIBUTE_VALUE_UNKNOWN)) {
      throw new ConfUnknownTypeException();
    }

    if (confType.equalsIgnoreCase(TYPE_ATTRIBUTE_VALUE_REJECTED)) {
      throw new ConfRejectedTypeException();
    }

    try {
      // updating ProcessConfiguration object with information obtained
      // from the
      // Process Configuration XML
      processConfiguration.setProcessID(Long.parseLong((rootElem.getAttribute(PROCESS_ID_ATTRIBUTE))));
      processConfiguration.setProcessName(processName);
      processConfiguration.setprocessPIK(processPIK);
      
      try {
        processConfiguration.setHostName(InetAddress.getLocalHost().getHostName());
      } catch (UnknownHostException e) {
        processConfiguration.setHostName("NOHOST");
      }

      processConfiguration.setJMSUser(getTagValue(rootElem, JMS_USER_ELEMENT));

      processConfiguration.setJMSPassword(getTagValue(rootElem, JMS_PASSWORD_ELEMENT));

      processConfiguration.setJMSQueueConFactJNDIName(getTagValue(rootElem, JMS_QCF_JNDI_NAME_ELEMENT));

      String jmsListenerTopic = getTagValue(rootElem, JMS_LISTENER_TOPIC);
      if (localConfiguration) {
        if (jmsListenerTopic != null && jmsListenerTopic.length() > 0) {  
          String pik;
          if (processConfiguration.getprocessPIK() == ProcessConfigurationRequest.NO_PIK) {
            pik = "NOPIK";
          }
          else {
            pik = processConfiguration.getprocessPIK().toString();
          }
          // Old format: tim.process.HOSTNAME.PROCESS_NAME.START_TIMESTAMP
          //  - replace the HOSTNAME and START_TIMESTAMP parts of the topic with *
          // New format: tim.process.command.HOSTNAME.PROCESS_NAME.PIK
          //  - replace HOSTNAME with current one and PIK with the one taken from server while Connection phase
          String[] tokens = jmsListenerTopic.split("\\.");
          LOGGER.debug("tokens.lenght : " + tokens.length);
          StringBuilder strBuf = new StringBuilder();
          try {
            strBuf.append(tokens[TIM_TOKEN]).append(".").append(tokens[PROCESS_TOKEN]).append(".").append(tokens[COMMAND_TOKEN]).append(".")
              .append(processConfiguration.getHostName()).append(".").append(tokens[PROCESS_NAME_TOKEN]).append(".").append(pik);
          } catch (Exception ex) {
            LOGGER.debug("error while splitting the topic. Format: tim.process.command.HOSTNAME.PROCESS_NAME.PIK", ex);
          }
          String topic = strBuf.toString();
          LOGGER.debug("setting the listener topic to: " + topic);
          processConfiguration.setListenerTopic(topic);
        }
      } else {
        processConfiguration.setListenerTopic(jmsListenerTopic);
      }

      processConfiguration.setJMSQueueJNDIName(getTagValue(rootElem, JMS_QUEUE_JNDI_NAME_ELEMENT));

      processConfiguration.setAliveTagID(Long.parseLong(getTagValue(rootElem, ALIVE_TAG_ID_ELEMENT)));

      processConfiguration.setAliveInterval(Integer.parseInt(getTagValue(rootElem, ALIVE_INTERVAL_ELEMENT)));

      processConfiguration.setMaxMessageSize(Long.parseLong(getTagValue(rootElem, MAX_MESSAGE_SIZE_ELEMENT)));

      processConfiguration.setMaxMessageDelay(Long.parseLong(getTagValue(rootElem, MAX_MESSAGE_DELAY_ELEMENT)));

      Node equipmentUnitsNode = rootElem.getElementsByTagName(EQUIPMENT_UNITS_ELEMENT).item(0);
      NodeList equipmentUnits = equipmentUnitsNode.getChildNodes();
      for (int i = 0; i < equipmentUnits.getLength(); i++) {
        Node currentNode = equipmentUnits.item(i);
        if (currentNode.getNodeType() == Node.ELEMENT_NODE
            && currentNode.getNodeName().equals(EQUIPMENT_UNIT_ELEMENT)) {
          try {
            EquipmentConfiguration equipmentConfiguration = equipmentConfigurationFactory
                .createEquipmentConfiguration((Element) currentNode);
            processConfiguration.addEquipmentConfiguration(equipmentConfiguration);
          } catch (Exception ex) {
            LOGGER.error("Exception caught while trying to create an instance of EquipmentUnit.", ex);
          }
        }
      }
    } // try
    catch (NullPointerException ex) {
      LOGGER.fatal("NullPointerException caught while trying to configure the process. Ex. message = "
          + ex.getMessage());
      LOGGER.fatal("The structure of ProcessConfiguration XML might contain some mistakes !");
      throw ex;
    } catch (NumberFormatException ex) {
      LOGGER.fatal("NumberFormatException caught while trying to configure the process. Ex. message = "
          + ex.getMessage());
      LOGGER.fatal("The structure of ProcessConfiguration XML might contain some mistakes !");
      throw ex;
    }
    return processConfiguration;
  }

  /**
   * Gets the tag value of a child element.
   * 
   * @param element The element to use.
   * @param tagName The name of the element.
   * @return The String value of the child element with the provided tag name.
   */
  // private String getTagValue(final Element element, final String tagName) {
  // return element.getElementsByTagName(tagName).item(0).getFirstChild().getNodeValue();
  // }

  /**
   * Creates the equipment configuration from the matching subelement in the DOM tree.
   * 
   * @param equipmentUnit A EquipmentUnit element from the DOM tree.
   * @return An equipment configuration object.
   */
  // public EquipmentConfiguration createEquipmentConfiguration(final Element equipmentUnit) {
  // String eqID = equipmentUnit.getAttribute(ID_ATTRIBUTE);
  // LOGGER.debug("EQ ID : " + eqID);
  // String eqName = equipmentUnit.getAttribute(NAME_ATTRIBUTE);
  //
  // EquipmentConfiguration equipmentConfiguration = new EquipmentConfiguration();
  //
  // try {
  // equipmentConfiguration.setId(Long.parseLong(eqID));
  // equipmentConfiguration.setName(eqName);
  //
  // equipmentConfiguration.setHandlerClassName(getTagValue(equipmentUnit, HANDLER_CLASS_NAME_ELEMENT).trim());
  //
  // equipmentConfiguration.setCommFaultTagId(Long
  // .parseLong(getTagValue(equipmentUnit, COMMFAULT_TAG_ID_ELEMENT)));
  //
  // equipmentConfiguration.setCommFaultTagValue(Boolean.parseBoolean(getTagValue(equipmentUnit,
  // COMMFAULT_TAG_VALUE_ELEMENT)));
  //
  // // try and be prepared to catch the exception, because the field is
  // // not obligatory and may not exist
  // try {
  // equipmentConfiguration.setAliveTagId(Long.parseLong(getTagValue(equipmentUnit, ALIVE_TAG_ID_ELEMENT)));
  // } catch (NullPointerException ex) {
  // LOGGER.debug("Process has no alive Tag id.");
  // }
  // // try and be prepared to catch the exception, because the field is
  // // not obligatory and may not exist
  // try {
  // equipmentConfiguration.setAliveTagInterval(Long.parseLong(getTagValue(equipmentUnit,
  // ALIVE_INTERVAL_ELEMENT)));
  // } catch (NullPointerException ex) {
  // LOGGER.debug("Process has no alive Tag interval.");
  // }
  //
  // // try and be prepared to catch the exception, because the field is
  // // not obligatory and may not exist
  // try {
  // equipmentConfiguration.setAddress(getTagValue(equipmentUnit, ADDRESS_ELEMENT));
  // } catch (NullPointerException ex) {
  // equipmentConfiguration.setAddress(null);
  // }
  // } catch (NullPointerException ex) {
  // LOGGER.error("NullPointerException caught. Returning null !", ex);
  // LOGGER.error("\t Exception LocalizedMessage = " + ex.getLocalizedMessage());
  // LOGGER.info("sending CommFaultTag. Tag id: " + equipmentConfiguration.getCommFaultTagId());
  // processMessageSender.sendCommfaultTag(equipmentConfiguration.getCommFaultTagId(), equipmentConfiguration
  // .getCommFaultTagValue());
  // return null;
  // } catch (NumberFormatException ex) {
  // LOGGER.error("NumberFormatException caught. Returning null !");
  // LOGGER.error("\t Exception LocalizedMessage = " + ex.getLocalizedMessage());
  // LOGGER.info("sending CommFaultTag. Tag id: " + equipmentConfiguration.getCommFaultTagId());
  // processMessageSender.sendCommfaultTag(equipmentConfiguration.getCommFaultTagId(), equipmentConfiguration
  // .getCommFaultTagValue());
  // return null;
  // } catch (IllegalAccessError ex) {
  // LOGGER.error("IllegalAccessError caught. Returning null !");
  // LOGGER.error("\t Exception LocalizedMessage = " + ex.getLocalizedMessage());
  // LOGGER.info("sending CommFaultTag. Tag id: " + equipmentConfiguration.getCommFaultTagId());
  // processMessageSender.sendCommfaultTag(equipmentConfiguration.getCommFaultTagId(), equipmentConfiguration
  // .getCommFaultTagValue());
  // return null;
  // } catch (Exception ex) {
  // // just in case - let's catch most generic exception..
  // LOGGER.error("Exception caught. Returning null !");
  // LOGGER.error("\t Exception LocalizedMessage = " + ex.getLocalizedMessage());
  // LOGGER.error("\t" + ex);
  // LOGGER.info("sending CommFaultTag. Tag id: " + equipmentConfiguration.getCommFaultTagId());
  // processMessageSender.sendCommfaultTag(equipmentConfiguration.getCommFaultTagId(), equipmentConfiguration
  // .getCommFaultTagValue());
  // return null;
  // }
  // // should be one
  // processSubEquipmentUnits(equipmentUnit, equipmentConfiguration);
  // processDataTags(equipmentUnit, equipmentConfiguration);
  // processCommandTags(equipmentUnit, equipmentConfiguration);
  // return equipmentConfiguration;
  // }

  /**
   * TODO: Backward compatibility. remove after updating server
   */
  
  /**
   * Loads the configuration from the server.
   * 
   * @return The configuration as DOM Document.
   */
  public Document loadConfigRemote() {
    return loadConfigRemote(null);
  }

  /**
   * Loads the configuration from the server and saves it to the provided location.
   * 
   * @param saveLocation The location to save the object.
   * @return The configuration as DOM Document.
   */
  public Document loadConfigRemote(final String saveLocation) {
    Document confXMLDoc;
    LOGGER.debug("invoking ProcessRequestSender sendProcessConfigurationRequest");
    // check if the user wanted to save the configuration XML
    if (saveLocation != null) {
      confXMLDoc = processRequestSender.old_sendProcessConfigurationRequest(saveLocation);
    } else {
      confXMLDoc = processRequestSender.old_sendProcessConfigurationRequest();
    }
    if (confXMLDoc == null) {
      LOGGER.warn("Connection request to server: timeout waiting for server response.");
      LOGGER.info("Could not receive any configuration - stopping the DAQ process...");
      System.exit(0);
    }
    LOGGER.info("Configuration XML received from server and parsed");
    return confXMLDoc;
  }

  /**
   * Loads the configuration from the file system.
   * 
   * @param fileSystemLocation The location of the configuration xml on the file system.
   * @return The configuration as DOM Document.
   */
  public Document loadConfigLocal(final String fileSystemLocation) {
    Document confXMLDoc;
    LOGGER.debug("trying to configure process using configuration xml from the file " + fileSystemLocation);

    DOMParser parser = new DOMParser();
    try {
      parser.parse(fileSystemLocation);
      confXMLDoc = parser.getDocument();
    } catch (java.io.IOException ex) {
      LOGGER.error("Could not open processConfiguration XML file : " + fileSystemLocation);
      confXMLDoc = null;
    } catch (org.xml.sax.SAXException ex) {
      LOGGER.error("Could not parse processConfiguration XML file : " + fileSystemLocation);
      confXMLDoc = null;
    }
    if (confXMLDoc == null) {
      LOGGER.info("Could not receive any configuration - stopping the DAQ process...");
      System.exit(0);
    }
    LOGGER.info("Configuration XML loaded from filesystem and parsed");
    return confXMLDoc;
  }

}
