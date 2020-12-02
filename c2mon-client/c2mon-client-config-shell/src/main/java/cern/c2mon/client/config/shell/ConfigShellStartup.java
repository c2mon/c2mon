package cern.c2mon.client.config.shell;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstraps the C2MON Client Configuration Shell.
 **/
@SpringBootApplication
@EnableAutoConfiguration
@Slf4j
public class ConfigShellStartup {

    /**
     * Entry point to the Client Shell.
     * @param args no specific treatment of any arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(ConfigShellStartup.class, args);
    }
}
