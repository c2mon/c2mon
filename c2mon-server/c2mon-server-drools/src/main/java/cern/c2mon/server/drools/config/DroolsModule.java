package cern.c2mon.server.drools.config;

import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.TimedRuleExecutionOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
@EnableConfigurationProperties(DroolsProperties.class)
@ComponentScan("cern.c2mon.server.drools")
public class DroolsModule {

  @Bean
  public KieSession kieSession() {
    KieServices kieServices = KieServices.Factory.get();
    KieFileSystem kfs = kieServices.newKieFileSystem();

    KieBaseConfiguration config = KieServices.Factory.get().newKieBaseConfiguration();
    config.setOption(EventProcessingOption.STREAM);
    InputStream fis = DroolsModule.class.getResourceAsStream("/rules/test.drl");
    kfs.write("src/main/resources/rules/test.drl",
        kieServices.getResources().newInputStreamResource(fis));

    KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
    Results results = kieBuilder.getResults();
    if (results.hasMessages(Message.Level.ERROR)) {
      System.out.println(results.getMessages());
      throw new IllegalStateException("### errors ###");
    }

    KieContainer kieContainer =
        kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
    KieBase kieBase = kieContainer.newKieBase(config);

    KieSessionConfiguration ksconf = KieServices.Factory.get().newKieSessionConfiguration();

    ksconf.setOption(TimedRuleExecutionOption.YES);

    KieSession kSession = kieBase.newKieSession(ksconf, null);
    kSession.fireAllRules();
    //new Thread(() -> kSession.fireUntilHalt()).start();
    return kSession;
  }
}