package cern.c2mon.client.config.shell;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * Bootstraps the C2MON client config shell.
 **/
@SpringBootApplication
@EnableAutoConfiguration
@Slf4j
public class ConfigShellStartup {

    public static void main(final String[] args) throws IOException {
        SpringApplication.run(ConfigShellStartup.class, args);

        log.info("C2MON Client Config Shell is now initialised");
    }
}
