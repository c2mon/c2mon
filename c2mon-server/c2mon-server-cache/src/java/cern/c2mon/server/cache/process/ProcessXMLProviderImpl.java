package cern.c2mon.server.cache.process;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.CommandTagFacade;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessXMLProvider;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.exception.SubEquipmentException;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;

/**
 * TODO Add to TC config.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class ProcessXMLProviderImpl implements ProcessXMLProvider {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ProcessXMLProviderImpl.class);

  /**
   * Required facade, cache and DAO beans.
   */
  private EquipmentCache equipmentCache;  
  private SubEquipmentCache subEquipmentCache;
  private SubEquipmentDAO subEquipmentDAO;  
  private SubEquipmentFacade subEquipmentFacade;  
  private DataTagFacade dataTagFacade;
  private ControlTagCache controlTagCache;
  private ProcessCache processCache;
  private CommandTagFacade commandTagFacade;

  @Autowired
  public ProcessXMLProviderImpl(EquipmentCache equipmentCache, SubEquipmentDAO subEquipmentDAO,
      SubEquipmentFacade subEquipmentFacade, DataTagFacade dataTagFacade, ControlTagCache controlTagCache,
      ProcessCache processCache, CommandTagFacade commandTagFacade, SubEquipmentCache subEquipmentCache) {
    super();
    this.equipmentCache = equipmentCache;
    this.subEquipmentDAO = subEquipmentDAO;
    this.subEquipmentFacade = subEquipmentFacade;
    this.subEquipmentCache = subEquipmentCache;
    this.dataTagFacade = dataTagFacade;
    this.controlTagCache = controlTagCache;
    this.processCache= processCache;
    this.commandTagFacade = commandTagFacade;
  }

  @Override
  public String getProcessConfigXML(Process process) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("getConfigXML() called.");
    }
    if (process != null) {

      //cast to the internal cache object (change this if moved to other but cache module!)
      ProcessCacheObject processCacheObject = (ProcessCacheObject) process;    
    
      //EquipmentFacadeLocal equipmentFacadeLocal = null;
      String schemaInfo = "xmlns=\"http://timweb.cern.ch/schemas/c2mon-daq/Configuration\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
          "xsi:schemaLocation=\"http://timweb.cern.ch/schemas/c2mon-daq/Configuration http://timweb/schemas/c2mon-daq/ProcessConfiguration.xsd\" ";
      
      StringBuffer str = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

      str.append("<ProcessConfiguration ").append(schemaInfo).append(" process-id=\"");
      
      str.append(processCacheObject.getId());
      str.append("\" type=\"initialise\">\n");

      str.append("  <alive-tag-id>");
      str.append(processCacheObject.getAliveTagId());
      str.append("</alive-tag-id>\n");

      str.append("  <alive-interval>");
      str.append(processCacheObject.getAliveInterval());
      str.append("</alive-interval>\n");

      str.append("  <max-message-size>");
      str.append(processCacheObject.getMaxMessageSize());
      str.append("</max-message-size>\n");

      str.append("  <max-message-delay>");
      str.append(processCacheObject.getMaxMessageDelay());
      str.append("</max-message-delay>\n");
      
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("getting equipment ids");
      }
      
      str.append("  <EquipmentUnits>\n");
      Collection<Long> equipmentIds = processCacheObject.getEquipmentIds();

      if (equipmentIds != null && equipmentIds.size() > 0) {
        for (Long equipmentId : equipmentIds) {
          str.append(getEquipmentConfigXML((Long) equipmentId));
        }
      }
      str.append("  </EquipmentUnits>\n");
      str.append("</ProcessConfiguration>\n");
      return str.toString(); 

    } else {
      LOGGER.error("Called getConfigXML() for a NULL process object - this should be avoided!"
          + "  ... throwing a runtime exception :)");
      throw new RuntimeException("Calling getConfigXML() on NULL process object.");
    }      
  }
  
  @Override
  public String getEquipmentConfigXML(Long id) {             
    try {
      StringBuilder str = new StringBuilder(); 
      EquipmentCacheObject equipment = (EquipmentCacheObject) equipmentCache.getCopy(id);
         
      // If found, generate config XML in String buffer
      str.append("<EquipmentUnit id=\"");

      str.append(equipment.getId());
      str.append("\" name=\"");
      str.append(equipment.getName());
      str.append("\">\n");

      str.append("  <handler-class-name>");
      str.append(equipment.getHandlerClassName());
      str.append("</handler-class-name>\n");

      str.append("  <commfault-tag-id>");
      str.append(equipment.getCommFaultTagId());
      str.append("</commfault-tag-id>\n");

      str.append("  <commfault-tag-value>");
      str.append(equipment.getCommFaultTagValue());
      str.append("</commfault-tag-value>\n");

      if (equipment.getAliveTagId() != null) {
        str.append("  <alive-tag-id>");
        str.append(equipment.getAliveTagId());
        str.append("</alive-tag-id>\n");

        str.append("  <alive-interval>");
        str.append(equipment.getAliveInterval());
        str.append("</alive-interval>\n");
      }

      str.append("  <address>");
      if (equipment.getAddress() != null) {
        str.append(equipment.getAddress());
      }
      str.append("</address>\n");
      
      // Generate the subEquipments section
      str.append("  <SubEquipmentUnits>\n");
      str.append(getSubEquipmentUnitsConfigXML(equipment));
      str.append("  </SubEquipmentUnits>\n");
      
      str.append("  <DataTags>\n");
      str.append(getDataTagsConfigXML(equipment));
      str.append("  </DataTags>\n");

      str.append("  <CommandTags>\n");
      str.append(getCommandTagsConfigXML(equipment));
      str.append("  </CommandTags>\n");

      str.append("</EquipmentUnit>");            
      return str.toString();  
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.error("Cannot locate Equipment in cache (Id=" + id + ") - throwing the exception.");
      throw cacheEx;
    }        
  }
  
  /**
   * Creates the configuration for the subequipments attached to the equipment provided as a parameter
   * 
   * <p>Called within block synchronized on equipment.
   * 
   * @param equipment Reference to the equipment for which we want to generate its subequipments configuration
   * @return A string containing the XML describing the subequipments configuration
   */
  private String getSubEquipmentUnitsConfigXML(final EquipmentCacheObject equipment) {
    if (LOGGER.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("getSubEquipmentUnitsConfigXML([MonitoringEquipment: ");
      str.append(equipment.getId());
      str.append("]) called.");
      LOGGER.debug(str);
    }

    // Initialise buffer for the XML structure to be generated
    StringBuilder str = new StringBuilder();

    // Generate XML for this equipment unit and append it to the buffer        
    str.append(getSubEquipmentConfigXML(equipment.getId()));          
    
    // Return the result
    return str.toString();
  }
  
  /**
   * Generates a XML file describing the configuration for the SubEquipment's that are attached to the
   * indicated equipment
   * 
   * @param equipmentId
   *          The id of the equipment for which we want to generate its
   *          subEquipments configuration
   * @return A string representing an XML file containing the equipment's
   *         information for its subEquipments
   */
  public String getSubEquipmentConfigXML(final Long equipmentId) {
    if (LOGGER.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("getConfigXML([EquipmentId: ");
      str.append(equipmentId.toString());
      str.append("]) called.");
      LOGGER.debug(str);
    }

    // Initialise buffer for the XML structure to be generated
    StringBuffer str = new StringBuffer();

    // Generate XML for this equipment unit and append it to the buffer
    try {
      List<SubEquipment> subEquipmentsIds = subEquipmentDAO.getSubEquipmentsByEquipment(equipmentId);

      if (subEquipmentsIds != null) {
        Iterator<SubEquipment> it = subEquipmentsIds.iterator();
        while (it.hasNext()) {
          str.append(getSubEquipmentConfigXML((SubEquipmentCacheObject) it.next()));
        }
      }
    } catch (SubEquipmentException e) {
      LOGGER.error("getConfigXML() : Unable to get the subequipments for the equipment ", e);
    }

    // Return the resulting XML structure
    return str.toString();
  }
  
  /**
   * Returns the configuration XML for the subequipment as a String.
   * 
   * @param subEquipmentCacheObject the SubEquipment we want the config XML for
   * @return XML as a String
   */
  private String getSubEquipmentConfigXML(final SubEquipmentCacheObject subEquipmentCacheObject) {    
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("getConfigXML() - Creating the configuration for the subequipment with id: " + subEquipmentCacheObject.getId());
    }
    StringBuffer str = new StringBuffer("    <SubEquipmentUnit  id=\"");
    str.append(subEquipmentCacheObject.getId());
    str.append("\" name=\"");
    str.append(subEquipmentCacheObject.getName());
    str.append("\">\n");

    str.append("      <commfault-tag-id>");
    str.append(subEquipmentCacheObject.getCommFaultTagId());
    str.append("</commfault-tag-id>\n");

    str.append("      <commfault-tag-value>");
    str.append(subEquipmentCacheObject.getCommFaultTagValue());
    str.append("</commfault-tag-value>\n");

    str.append("      <alive-tag-id>");
    str.append(subEquipmentCacheObject.getAliveTagId());
    str.append("</alive-tag-id>\n");

    str.append("      <alive-interval>");
    str.append(subEquipmentCacheObject.getAliveInterval());
    str.append("</alive-interval>\n");

    str.append("    </SubEquipmentUnit>\n");
    return str.toString();   
  }
  
  /**
   * Generate the content of the <DataTags> section of the DAQ config XML.
   * This method generates a <DataTag ...> XML entry for each DataTag
   * attached to the specified equipment unit or one of its subequipments.
   * 
   * <p>Called within block synchronized on equipment.
   */
  private String getDataTagsConfigXML(final EquipmentCacheObject pEquipment) {
    if (LOGGER.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("getDataTagsConfigXML([MonitoringEquipment: ");
      str.append(pEquipment.getId());
      str.append("]) called.");
      LOGGER.debug(str);
    }

    // Initialise buffer for the XML structure to be generated
    StringBuilder str = new StringBuilder();

    // Generate XML for this equipment unit and append it to the buffer.
    // generate datatag XML:
    ThreadPoolExecutor tagXmlExecutor = 
      new ThreadPoolExecutor(8, 10, 5, TimeUnit.SECONDS, 
                      new LinkedBlockingQueue<Runnable>(10000), new ThreadPoolExecutor.CallerRunsPolicy());    
    Collection<Long> dataTags = pEquipment.getDataTagIds();
    if (dataTags != null) {
      LinkedList<Future<String>> futureXmlStrings = new LinkedList<Future<String>>();
      LinkedList<Long> partialList = new LinkedList<Long>();      
      Iterator<Long> it = dataTags.iterator();
      while (it.hasNext()) {
        while (partialList.size() < 100 && it.hasNext()) {
          partialList.addLast(it.next());
        }
        Callable<String> tagTask = new GetTagXmlTask((LinkedList<Long>) partialList.clone());
        partialList.clear();        
        futureXmlStrings.addFirst(tagXmlExecutor.submit(tagTask));
      }
      tagXmlExecutor.shutdown();
      try {
        tagXmlExecutor.awaitTermination(120, TimeUnit.SECONDS);
        while (!futureXmlStrings.isEmpty()) {
          str.append(futureXmlStrings.pollFirst().get());
        }
      } catch (InterruptedException e) {
        LOGGER.error("Interrupted while waiting for XML tag threads to terminate - no datatags were added!");       
      } catch (ExecutionException e) {
        LOGGER.error("Interrupted while waiting for XML tag threads to terminate - no datatags were added!");        
      }      
    }
    
    // Exceptional treatment for the alive tag -> ALIVE tags may have a 
    // hardware address as they are usually sent by the monitored equipment.
    // However, they never appear in the list of DataTags for an equipment
    // and therefore need to be added to the list explicitly.
    Long aliveTagId = pEquipment.getAliveTagId();
    if (aliveTagId != null) {
      LOGGER.debug("For equipment " + pEquipment.getId() + ", alive Tag defined : " + aliveTagId);      
      try {
        ControlTagCacheObject aliveTag = (ControlTagCacheObject) controlTagCache.getCopy(aliveTagId);    
        LOGGER.debug("alive tag obtained from cache): " + aliveTagId);
        if (aliveTag.getAddress() != null && aliveTag.getAddress().getHardwareAddress() != null) { //added check on address not null in C2MON, as we allow null address fields if the DB field is
          LOGGER.debug("alive Tag has hardware address " + aliveTagId);
          str.append(dataTagFacade.generateSourceXML(aliveTag));
        } else {
          LOGGER.debug("Alive tag has no hardware address, so not including in DAQ XML.");            
        }
      } catch (CacheElementNotFoundException cacheEx) {
        LOGGER.error("Unable to locate alive tag with id " + aliveTagId + ") in the cache "
            + "when checking if it needs adding as a data tag to the XML configuration document.", cacheEx);
      }                
    }
    
    //Add sub-equipment alive tags to XML if they have a hardware address      
    for (Long subEquipmentId : pEquipment.getSubEquipmentIds()) {       
      Long aliveId = subEquipmentCache.get(subEquipmentId).getAliveTagId();
      if (aliveId != null) {
        LOGGER.debug("For sub-equipment " + subEquipmentId + ", alive Tag defined : " + aliveId);          
        controlTagCache.acquireReadLockOnKey(aliveId);
        try {
          ControlTagCacheObject aliveTag = (ControlTagCacheObject) controlTagCache.get(aliveId);          
          if (aliveTag.getAddress() != null && aliveTag.getAddress().getHardwareAddress() != null) {
            LOGGER.debug("Alive tag has hardware address: " + aliveTag.getAddress().getHardwareAddress().toConfigXML());
            str.append(dataTagFacade.generateSourceXML(aliveTag));
          } else {
            LOGGER.debug("Alive tag has no hardware address, so not including in DAQ XML.");
          }         
        } catch (CacheElementNotFoundException cacheEx) {
          LOGGER.error("Unable to locate subequipment alive tag with id " + aliveId + ") in the cache "
              + "when checking if it needs adding as a data tag to the XML configuration document.", cacheEx);
        } finally {
          controlTagCache.releaseReadLockOnKey(aliveId);
        }
      }
    }
    
    //Return the resulting XML structure
    return str.toString();
  }
  
  /**
   * Generate the content of the <CommandTags> section of the DAQ config XML.
   * This method generates a <CommandTag ...> XML entry for each CommandTag
   * attached to the specified equipment unit or one of its subequipments.
   * 
   * <p>Call within equipment lock.
   */
  private String getCommandTagsConfigXML(final EquipmentCacheObject pEquipment) {
    if (LOGGER.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("getCommandTagsConfigXML([Equipment: ");
      str.append(pEquipment.getId());
      str.append("]) called.");
      LOGGER.debug(str);
    }
    StringBuffer str = new StringBuffer();      
    Collection<Long> commandTagIds = pEquipment.getCommandTagIds();
    for (Long id : commandTagIds) {
      str.append(commandTagFacade.getConfigXML(id));
    }     
    return str.toString();
  }
  
  /**
   * Task used when loading the config XML from the DataTag cache.
   * @author Mark Brightwell
   *
   */
  private class GetTagXmlTask implements Callable<String> {

    private Collection<Long> keyList;
    
    public GetTagXmlTask(Collection<Long> keyList) {
      this.keyList = keyList;
    }
    
    /**
     * Get the XML for the list of tags and return as String.
     */
    @Override
    public String call() {
      StringBuilder str = new StringBuilder();      
      Iterator<Long> it = keyList.iterator();
      while (it.hasNext()) {
        str.append(dataTagFacade.getConfigXML(it.next()));
      }
      return str.toString();
    }
    
  }

  @Override
  public String getProcessConfigXML(String processName) {
    return getProcessConfigXML(processCache.getCopy(processName));
  }

}
