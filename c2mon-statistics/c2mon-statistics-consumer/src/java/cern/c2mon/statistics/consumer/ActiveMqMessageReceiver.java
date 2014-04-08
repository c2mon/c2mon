package cern.c2mon.statistics.consumer;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

import cern.c2mon.shared.daq.filter.FilteredDataTagValueUpdate;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

/**
 * ActiveMQ implementation of the MessageListener processing
 * incoming filtered values.
 * 
 * @author Mark Brightwell
 *
 */
public class ActiveMqMessageReceiver implements MessageListener {

  /**
   * The log4j logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ActiveMqMessageReceiver.class);
  
  /**
   * XML parser.
   */
  private SimpleXMLParser parser;
  
  /**
   * Buffer in which to put the updates
   */
  private DataTagValueBuffer dataTagValueBuffer;
  
  /**
   * Constructor.
   * @param dataTagValueBuffer the buffer
   */
  @Autowired
  public ActiveMqMessageReceiver(final DataTagValueBuffer dataTagValueBuffer) {
    super();
    this.dataTagValueBuffer = dataTagValueBuffer;
  }

  /**
   * Instantiates the parser.
   * @throws ParserConfigurationException if error in parser instantiation
   */
  @PostConstruct
  public void init() throws ParserConfigurationException {
    parser = new SimpleXMLParser();
  }
  
  @Override
  public void onMessage(final Message msg) {
    LOGGER.debug("entering onMessage()..");

    try {

        // check the message is a JMS XML message
        if (msg instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) msg;
            
            Document content = parser.parse(textMessage.getText());

            // get the top level element of the XML message, convert into a
            // DataTagValueUpdate object
            // (with several tag values inside), and add all these values to
            // the tag buffer
            dataTagValueBuffer.addValues(FilteredDataTagValueUpdate.fromXML(content.getDocumentElement()).getValues());

        } else {
            LOGGER.warn("received NON-text message");
        }
    } catch (JMSException ex) {
        LOGGER.error("Could not retrieve XML DOM document content from the received message");
        LOGGER.error(ex);
    }

    LOGGER.debug("leaving onMessage()");
  }

}
