package cern.c2mon.driver.filter.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import cern.c2mon.driver.common.conf.core.ConfigurationController;
import cern.c2mon.driver.common.conf.core.RunOptions;
import cern.c2mon.driver.filter.FilterMessageSender;
import cern.c2mon.driver.filter.IFilterMessageSender;
import cern.tim.shared.daq.filter.FilteredDataTagValueUpdate;

/**
 * ActiveMQ implementation of the FilterMessageSender, sending filtered updates
 * to the statistics consumer application.
 * 
 * @author Mark Brightwell
 *
 */
public class ActiveFilterSender extends FilterMessageSender implements IFilterMessageSender {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ActiveFilterSender.class);
  
  /**
   * The Spring template for sending the updates (creates a session for each call!). 
   */
  private JmsTemplate filterTemplate;
  
  /**
   * Constructor called by Spring.
   * @param configurationController to access the configuration (run options)
   * @param filterTemplate the Spring template to send the message (defined in XML)
   */
  @Autowired
  public ActiveFilterSender(final ConfigurationController configurationController, 
                            @Qualifier("filterJmsTemplate") final JmsTemplate filterTemplate) {
    super(configurationController);
    this.filterTemplate = filterTemplate;
  }

  @Override
  public void connect() {
    //nothing to do, connects automatically (in separate DriverKernel thread)
  }

  @Override
  public void shutdown() {
    super.closeTagBuffer();
  }

  @Override
  protected void processValues(final FilteredDataTagValueUpdate filteredDataTagValueUpdate) throws JMSException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("entering FilterMessageSender processValues()...");
    }


    // skip this part if in test mode or filtering is not enabled,
    RunOptions runOptions = configurationController.getRunOptions();
    if (runOptions.isFilterMode()) {
        // prepare the message
        filterTemplate.send(new MessageCreator() {
          
          @Override
          public Message createMessage(final Session session) throws JMSException {
            TextMessage msg = session.createTextMessage();
            msg.setText(filteredDataTagValueUpdate.toXML());
            return msg;
          }
        });

    }

    // log the filtered values in the log file
    filteredDataTagValueUpdate.log();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("leaving FilterMessageSender processValues()");
    }
  }

}
