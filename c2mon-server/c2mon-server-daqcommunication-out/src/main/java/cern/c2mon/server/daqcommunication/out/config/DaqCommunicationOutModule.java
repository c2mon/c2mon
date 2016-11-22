package cern.c2mon.server.daqcommunication.out.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@Import({
    DaqCommunicationOutJmsConfig.class
})
@ComponentScan("cern.c2mon.server.daqcommunication.out")
public class DaqCommunicationOutModule {}
