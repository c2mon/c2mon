package cern.c2mon.daq.common.messaging.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Required;

import cern.c2mon.daq.common.jmx.JmsSenderMXBean;
import cern.c2mon.daq.common.messaging.JmsSender;
import cern.c2mon.shared.daq.datatag.DataTagValueUpdate;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;

/**
 * this class is used for testing purposes and for the TestMode
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public class DummyJmsSender implements JmsSender, JmsSenderMXBean {

    
    private List<SourceDataTagValue> messages = new ArrayList<SourceDataTagValue>();
    
    /**
     * Enabling/disabling the action of sending information to the brokers
     */
    private boolean isEnabled = true;
    
    /**
     * The Spring name for the ActiveJmsSender
     */
    private String beanName;
    
    @Override
    public void connect() {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    @Override
    public void processValue(SourceDataTagValue sourceDataTagValue) throws JMSException {
        messages.add(sourceDataTagValue);

    }

    @Override
    public void processValues(DataTagValueUpdate dataTagValueUpdate) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }
    
    public List<SourceDataTagValue> getMessages() {
        return this.messages;
    }

    /**
     * Sets the isEnabled current value
     * 
     * @param value Enabling/disabling the action of sending information to the brokers
     */
    @Override
    public final void setEnabled(final boolean value) {
      this.isEnabled = value;
    }
    
    /**
     * Gets the isEnabled current value
     * 
     * @return isEnabled Current status of the action of sending information to the brokers
     */
    @Override
    public final boolean getEnabled() {
      return this.isEnabled;
    }
    
    /**
     * Sets the Spring name for the ActiveJmsSender
     */
    @Required
    public final void setBeanName(final String name) {
      this.beanName = name;
    }
    
    @Override
    public final String getBeanName() {
      return this.beanName;
    }

    @Override
    public final void jmsBrokerDataConnectionEnable(final boolean value) {
      this.setEnabled(value);
    }
    
}
