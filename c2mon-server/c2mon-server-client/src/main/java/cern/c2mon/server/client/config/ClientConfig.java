package cern.c2mon.server.client.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@Import({
    ClientJmsConfig.class,
    AlarmJmsConfig.class,
    SupervisionJmsConfig.class,
    ConfigRequestJmsConfig.class,
    AdminJmsConfig.class
})
@ComponentScan("cern.c2mon.server.client")
public class ClientConfig {}
