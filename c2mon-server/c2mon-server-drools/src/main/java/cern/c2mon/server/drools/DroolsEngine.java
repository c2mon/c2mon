package cern.c2mon.server.drools;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

@Component
public class DroolsEngine {

  private static KieSession kieSession;

  public DroolsEngine() {

  }

  public static KieSession getKieSession() {
    if (kieSession == null) {
      KieServices kieServices = KieServices.Factory.get();
      KieContainer kContainer = kieServices.getKieClasspathContainer();
      kieSession = kContainer.newKieSession();
    }
    return kieSession;
  }
}
