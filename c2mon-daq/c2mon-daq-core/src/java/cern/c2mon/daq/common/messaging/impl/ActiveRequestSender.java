package cern.c2mon.daq.common.messaging.impl;

import java.io.FileWriter;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.w3c.dom.Document;

import cern.c2mon.daq.common.conf.core.CommonConfiguration;
import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.conf.core.RunOptions;
import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.daq.common.messaging.impl.backward.ProcessRequestResponseConverterBC;
import cern.c2mon.shared.daq.process.ProcessConfigurationRequest;
import cern.c2mon.shared.daq.process.ProcessConfigurationResponse;
import cern.c2mon.shared.daq.process.ProcessConnectionRequest;
import cern.c2mon.shared.daq.process.ProcessConnectionResponse;
import cern.c2mon.shared.daq.process.ProcessDisconnectionRequest;
import cern.c2mon.shared.daq.process.ProcessMessageConverter;
import cern.c2mon.shared.daq.process.backward.ProcessConnectionRequestBC;
import cern.c2mon.shared.daq.process.backward.ProcessDisconnectionBC;
import cern.c2mon.shared.daq.process.backward.ProcessRequestConverterBC;

/**
 * Implementation of ProcessRequestSender interface for ActiveMQ JMS middleware.
 * 
 * @author mbrightw
 * @author vilches (refactoring updates)
 * 
 */
public class ActiveRequestSender implements ProcessRequestSender {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ActiveRequestSender.class);
    
    /**
     * Constant of the PIK request time out
     */
    private static final long PIK_REQUEST_TIMEOUT = 5000;

    /**
     * Reference to the common DAQ configuration.
     */
    private final CommonConfiguration commonConfiguration;

    /**
     * Reference to the JmsTemplate used to send messages to the server
     * (instantiated in Spring XML).
     */
    private JmsTemplate jmsTemplate;

    /**
     * Configuration controller as access point to the configuration.
     */
    private final ConfigurationController configurationController;
    
    /**
     * ProcessMessageConverter helper class (fromMessage/ToMessage)
     */
    private ProcessMessageConverter processMessageConverter;
    
    /**
     * TODO: Backward compatibility. remove after updating server
     */
    
    /**
     * Reference to converter called explicitly when sending the connection
     * request.
     */
    private final ProcessRequestConverterBC processRequestConverterBC;
    
    /**
     * Reference to the converter for processing the reply (converts Message ->
     * DOM document).
     */
   private final ProcessRequestResponseConverterBC processRequestResponseConverterBC;

    /**
     * Unique constructor used by Spring to instantiate the bean.
     * 
     * @param commonConfiguration
     *            the common DAQ configuration object (from properties file)
     * @param jmsTemplate
     *            the JmsTemplate is wired using a Qualifier annotation to
     *            distinguish it from the others: can be overwritten using the setter
     * @param processRequestConverterBC (Backward compatibility)
     *            the request converter object
     * @param processRequestResponseConverterBC (Backward compatibility)
     *            the response converter object
     * @param configurationController
     *            Configuration controller holding the configuration.
     */
    @Autowired
    public ActiveRequestSender(final CommonConfiguration commonConfiguration, 
                               @Qualifier("processRequestJmsTemplate") final JmsTemplate jmsTemplate,
                               final ProcessRequestConverterBC processRequestConverterBC,
                               final ProcessRequestResponseConverterBC processRequestResponseConverterBC,
                               final ConfigurationController configurationController) {
        this.commonConfiguration = commonConfiguration;
        this.jmsTemplate = jmsTemplate;
        this.configurationController = configurationController;
        this.processMessageConverter = new ProcessMessageConverter();
        
        // TODO: Backward compatibility. To remove later
        this.processRequestConverterBC = processRequestConverterBC;
        this.processRequestResponseConverterBC = processRequestResponseConverterBC;
    }

    
    @Override
    public ProcessConfigurationResponse sendProcessConfigurationRequest() {
        LOGGER.debug("sendProcessConfigurationRequest - Sending Process Configuration request to server.");
        // use of JmsTemplate here means exceptions are caught by Spring and
        // converted!
        // JMS template NOT used for reply as need to use same connection &
        // session
        final Destination requestDestination = jmsTemplate.getDefaultDestination();
        ProcessConfigurationResponse processConfigurationResponse = (ProcessConfigurationResponse) jmsTemplate.execute(new SessionCallback<Object>() {
            public Object doInJms(final Session session) throws JMSException {
                TemporaryQueue replyQueue = session.createTemporaryQueue();
                // TODO the '-processName' should be a constant
                String processName = configurationController.getCommandParamsHandler().getParamValue("-processName");
                ProcessConfigurationRequest processConfigurationRequest = new ProcessConfigurationRequest(processName);
                processConfigurationRequest.setprocessPIK(configurationController.getProcessConfiguration().getprocessPIK());
                
                Message message = processMessageConverter.toMessage(processConfigurationRequest, session);
                message.setJMSReplyTo(replyQueue);                
                MessageProducer messageProducer = session.createProducer(requestDestination);
                try {
                  messageProducer.setTimeToLive(commonConfiguration.getRequestTimeout());
                  messageProducer.send(message);

                  // wait for reply (receive timeout is set in XML)
                  MessageConsumer consumer = session.createConsumer(replyQueue);
                  try {
                    Message replyMessage = consumer.receive(commonConfiguration.getRequestTimeout());
                    if (replyMessage == null) {
                        return null;
                    } else {
                        return processMessageConverter.fromMessage(replyMessage);
                    }
                  } finally {
                    consumer.close();
                  }                  
                } finally {
                  messageProducer.close();
                }                
            }
        }, true); // start the connection for receiving messages

        // Can be null if there is a TimeOut
        return processConfigurationResponse; 
    }
    
    @Override
    public ProcessConnectionResponse sendProcessConnectionRequest() {
        LOGGER.debug("sendProcessConnectionRequest - Sending Process Connection Request to server.");
        // use of JmsTemplate here means exceptions are caught by Spring and
        // converted!
        // JMS template NOT used for reply as need to use same connection &
        // session
        final Destination requestDestination = jmsTemplate.getDefaultDestination();
        
        ProcessConnectionResponse processConnectionResponse = (ProcessConnectionResponse) jmsTemplate.execute(new SessionCallback<Object>() {
            public Object doInJms(final Session session) throws JMSException {
                TemporaryQueue replyQueue = session.createTemporaryQueue();
                String processName = configurationController.getCommandParamsHandler().getParamValue("-processName");
                // Process PIK Request
                ProcessConnectionRequest processConnectionRequest = new ProcessConnectionRequest(processName);
                configurationController.getRunOptions().setStartUp(processConnectionRequest.getProcessStartupTime().getTime());
                
                Message message = processMessageConverter.toMessage(processConnectionRequest, session);
                message.setJMSReplyTo(replyQueue);                
                MessageProducer messageProducer = session.createProducer(requestDestination);
                try {
                  // TimeOut (too long for the PIK) 12000
                  //messageProducer.setTimeToLive(commonConfiguration.getRequestTimeout());
                  messageProducer.setTimeToLive(PIK_REQUEST_TIMEOUT);
                  messageProducer.send(message);
                  
                  // If there is a timeout SystemExit

                  // wait for reply (receive timeout is set in XML) -> 12000
                  MessageConsumer consumer = session.createConsumer(replyQueue);
                  try {
                    //Message replyMessage = consumer.receive(commonConfiguration.getRequestTimeout());
                    Message replyMessage = consumer.receive(PIK_REQUEST_TIMEOUT);
                    if (replyMessage == null) {
                      return null;
                    } else {
                      // Convert the XML and return it as a ProcessConnectionRespond object
                      return processMessageConverter.fromMessage(replyMessage);
                    }
                  } finally {
                    consumer.close();
                  }                  
                } finally {
                  messageProducer.close();
                }                
            }
        }, true); // start the connection for receiving PIK messages

        // Can be null if there is a TimeOut
        return processConnectionResponse;
    }
    
    @Override
    public void sendProcessDisconnectionRequest() {
      RunOptions runOptions = this.configurationController.getRunOptions();

      LOGGER.debug("sendProcessDisconnectionRequest - Sending Process Disconnection notification to server.");
      ProcessConfiguration processConfiguration = this.configurationController.getProcessConfiguration();
      ProcessDisconnectionRequest processDisconnectionRequest;

      // processConfiguration set up for ProcessDisconnectionRequest compatibility

      // ID
      if (processConfiguration.getProcessID() == null) {
        processConfiguration.setProcessID(ProcessDisconnectionRequest.NO_ID);
      }
      // Name
      if (processConfiguration.getProcessName() == null) {
        processConfiguration.setProcessName(ProcessDisconnectionRequest.NO_PROCESS);
      }
      // PIK
      if (processConfiguration.getprocessPIK() == null) {
        processConfiguration.setprocessPIK(ProcessDisconnectionRequest.NO_PIK);
      }

      // We don't care if there is NO_PIK or NO_PROCESS. The server will take care
      if (processConfiguration.getProcessID() != ProcessDisconnectionRequest.NO_ID) {
        processDisconnectionRequest = new ProcessDisconnectionRequest(processConfiguration.getProcessID(), 
            processConfiguration.getProcessName(), processConfiguration.getprocessPIK(), runOptions.getStartUp());
      }
      else  {
        processDisconnectionRequest = new ProcessDisconnectionRequest(
            processConfiguration.getProcessName(), processConfiguration.getprocessPIK(), runOptions.getStartUp());
      }

      LOGGER.trace("sendProcessDisconnectionRequest - Converting and sending disconnection message");
      
      jmsTemplate.setMessageConverter(this.processMessageConverter);
      jmsTemplate.convertAndSend(processDisconnectionRequest);
      
      LOGGER.trace("sendProcessDisconnectionRequest - Process Disconnection for " + processConfiguration.getProcessName() + " sent");
    }

    /**
     * Useful for overwriting the default JmsTemplate that is wired in a start-up
     * (for using a different connection for instance).
     * @param jmsTemplate the jmsTemplate to set
     */
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
      this.jmsTemplate = jmsTemplate;
    }
    
    /**
     * TODO: Backward compatibility. remove after updating server
     */
    
    /**
     * This method publishes the ProcessConnection message on the configured
     * topic. As a result it expects to receive the ProcessConfiguration
     * message. The XML content of the message is then automatically parsed, and
     * the DOM Document object is returned for further processing. If the server
     * does not reply within the specified timeout, null is returned.
     * 
     * @return the parsed XML document
     */
   @Override
   public Document old_sendProcessConfigurationRequest() {
       return old_sendProcessConfigurationRequest("");
   }
    
    /**
     * This method publishes the ProcessConnection message on the configured
     * topic. As a result it suspects to receive the ProcessConfiguration
     * message. The XML content of the message is then automatically parsed, and
     * the DOM Document object is returned for further processing. If the
     * argument contains a non-empty string (file name), the method will try to
     * save received XML in a specified file.
     * 
     * @param fileToSaveConf
     *            the name of the file to save the config to
     * @return the parsed XML document
     */
   @Override
   public Document old_sendProcessConfigurationRequest(final String fileToSaveConf) {
       LOGGER.debug("old_sendProcessConfigurationRequest - Sending Process Configuration request to server.");
       // use of JmsTemplate here means exceptions are caught by Spring and
       // converted!
       // JMS template NOT used for reply as need to use same connection &
       // session
       final Destination requestDestination = jmsTemplate.getDefaultDestination();
       Document xmlReplyDocument = (Document) jmsTemplate.execute(new SessionCallback<Object>() {
           public Object doInJms(final Session session) throws JMSException {
               TemporaryQueue replyQueue = session.createTemporaryQueue();
               // TODO the '-processName' should be a constant
               String processName = configurationController.getCommandParamsHandler().getParamValue("-processName");
               ProcessConnectionRequestBC connectionRequest = new ProcessConnectionRequestBC(processName);
               configurationController.getRunOptions().setStartUp(connectionRequest.getStartupTime().getTime());
               Message message = processRequestConverterBC.toMessage(connectionRequest, session);
               message.setJMSReplyTo(replyQueue);                
               MessageProducer messageProducer = session.createProducer(requestDestination);
               try {
                 messageProducer.setTimeToLive(commonConfiguration.getRequestTimeout());
                 messageProducer.send(message);
 
                 // set converter for reply
                 // processRequestJmsTemplate.setMessageConverter(processRequestResponseConverter);
 
                 // wait for reply (receive timeout is set in XML)
                 MessageConsumer consumer = session.createConsumer(replyQueue);
                 try {
                   Message replyMessage = consumer.receive(commonConfiguration.getRequestTimeout());
                   if (replyMessage == null) {
                       return null;
                   } else {
                       return processRequestResponseConverterBC.fromMessage(replyMessage);
                   }
                 } finally {
                   consumer.close();
                 }                  
               } finally {
                 messageProducer.close();
               }                
               // return (Document)
               // processRequestJmsTemplate.receiveAndConvert(replyTopic);
           }
       }, true); // start the connection for receiving messages
 
       // check if a file was specified for saving the received configuration
       if (fileToSaveConf.length() > 0 && xmlReplyDocument != null) {
           LOGGER.info("old_sendProcessConfigurationRequest - saving the process configuration XML in a file " + fileToSaveConf + " due to user request");
           FileWriter fwr = null;
           try {
               fwr = new FileWriter(fileToSaveConf);
            // TODO default output format - check this works ok
               XMLSerializer serializer = new XMLSerializer(fwr, new OutputFormat());
               serializer.serialize(xmlReplyDocument);
               fwr.close();
           } catch (java.io.IOException ex) {
               LOGGER.error("old_sendProcessConfigurationRequest - Could not save the configuration to the file " + fileToSaveConf, ex);
           }
       }
 
       /**
         * This method publishes the ProcessConnection message on the configured
         * topic. As a result it expects to receive the ProcessConfiguration
         * message. The XML content of the message is then automatically parsed, and
         * the DOM Document object is returned for further processing. If the server
         * does not reply within the specified timeout, null is returned.
         * 
         * @return the parsed XML document
         */
       return xmlReplyDocument; // should never be null on return
   }
   
   /**
    * This method is invoked by the Kernel just before the driver (clearly)
    * exits. The server application stops supervising the process in question
    * and invalidates all associated tags.The driver is also supposed to send a
    * ProcessDisconnection message in reply to a ProcessTerminationRequest
    * message (TODO is this implemented in the server?!). No response from the
    * server is expected.
    */
   @Override
   public void old_sendProcessDisconnection() {
     RunOptions runOptions = this.configurationController.getRunOptions();

     LOGGER.debug("old_sendProcessDisconnection - Sending Process Disconnection notification to server.");
     ProcessConfiguration processConfiguration =
         configurationController.getProcessConfiguration();
     ProcessDisconnectionBC processDisconnectionRequestBC;
     if (processConfiguration.getProcessID() != -1) {
       processDisconnectionRequestBC = new ProcessDisconnectionBC(processConfiguration.getProcessID(),
           processConfiguration.getProcessName(),
           configurationController.getRunOptions().getStartUp());
     }
     else {
       processDisconnectionRequestBC = new ProcessDisconnectionBC(
           processConfiguration.getProcessName(),
           configurationController.getRunOptions().getStartUp());
     }
     jmsTemplate.setMessageConverter(this.processRequestConverterBC);
     jmsTemplate.convertAndSend(processDisconnectionRequestBC);
   }

}
