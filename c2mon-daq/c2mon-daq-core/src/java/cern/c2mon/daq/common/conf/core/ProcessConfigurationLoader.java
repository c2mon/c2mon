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
   * JMS DAq queue trunk
   */
  private String jmsDaqQueueTrunk;

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
  public void setEquipmentConfigurationFactory(EquipmentConfigurationFactory eqConfFactory) {
    this.equipmentConfigurationFactory = eqConfFactory;
  }
  
  @Autowired
  public void setJmsDaqQueueTrunk(String jmsDaqQueueTrunk) {
    this.jmsDaqQueueTrunk = jmsDaqQueueTrunk;
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
      LOGGER.warn("getProcessConfiguration - Configuration request to server: timeout waiting for server response.");
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
   * @param jmsDaqQueueTrunk
   * @return The ProcessConfiguration object.
   * @throws ConfUnknownTypeException Thrown if the configuration has the type 'unknown'.
   * @throws ConfRejectedTypeException Thrown if the configuration has the type 'rejected'.
   */
  public ProcessConfiguration createProcessConfiguration(final String processName, final Long processPIK, 
      final Document confXMLDoc, final boolean localConfiguration, String jmsDaqQueueTrunk) throws ConfUnknownTypeException, ConfRejectedTypeException {
    this.jmsDaqQueueTrunk = jmsDaqQueueTrunk;
    return createProcessConfiguration(processName, processPIK, confXMLDoc, localConfiguration);
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
      
      String pik;
      if (processConfiguration.getprocessPIK() == ProcessConfigurationRequest.NO_PIK) {
        pik = "NOPIK";
      }
      else {
        pik = processConfiguration.getprocessPIK().toString();
      }
      
      String jmsDaqQueue = this.jmsDaqQueueTrunk + ".command." + processConfiguration.getHostName() + "." 
          + processConfiguration.getProcessName() + "." + pik;
      processConfiguration.setJmsDaqCommandQueue(jmsDaqQueue);
      LOGGER.trace("createProcessConfiguration - jms Daq Queue: " + jmsDaqQueue);

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
}
