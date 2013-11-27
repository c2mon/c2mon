package cern.c2mon.daq.common.messaging.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.w3c.dom.Document;

import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.shared.daq.process.ProcessConfigurationResponse;
import cern.c2mon.shared.daq.process.ProcessConnectionResponse;

public class TestModeRequestSender implements ProcessRequestSender {

  private final ProcessRequestSender wrapperRequestSender;
  
  @Autowired
  public TestModeRequestSender(@Qualifier("activeRequestSender") final ProcessRequestSender wrapper) {
    this.wrapperRequestSender = wrapper;
  }
  
  @Override
  public ProcessConfigurationResponse sendProcessConfigurationRequest() {
    return wrapperRequestSender.sendProcessConfigurationRequest();
  }

  @Override
  public ProcessConnectionResponse sendProcessConnectionRequest() {
    return new ProcessConnectionResponse(ProcessConnectionResponse.NO_PROCESS, 123456L);
  }

  @Override
  public void sendProcessDisconnectionRequest() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Document old_sendProcessConfigurationRequest() {
    return wrapperRequestSender.old_sendProcessConfigurationRequest();
  }

  @Override
  public Document old_sendProcessConfigurationRequest(String fileToSaveConf) {
    return wrapperRequestSender.old_sendProcessConfigurationRequest(fileToSaveConf);
  }

  @Override
  public void old_sendProcessDisconnection() {
    // TODO Auto-generated method stub 
  }
}
