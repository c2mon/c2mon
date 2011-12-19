package cern.c2mon.web.configviewer.service;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cern.tim.shared.client.command.RbacAuthorizationDetails;

public class RbacTest {

  
  @Test
  public void testConfigLoaderService() {
    
    String configloader_details = "TIM_APPLICATIONS,TIM_WEBCONFIG,RUN ";
    
    String[] split_config_details = configloader_details.replace(" ", "").split( ",\\s*" ); // split on commas
    
    assertTrue (split_config_details[0].equals("TIM_APPLICATIONS"));
    assertTrue (split_config_details[1].equals("TIM_WEBCONFIG"));
    assertTrue (split_config_details[2].equals("RUN"));
  }
}
