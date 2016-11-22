package cern.c2mon.server.rule.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@Import({

})
@ComponentScan("cern.c2mon.server.rule")
public class RuleModule {}
