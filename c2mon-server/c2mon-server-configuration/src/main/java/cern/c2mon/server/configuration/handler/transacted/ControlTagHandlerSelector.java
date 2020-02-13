package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.server.configuration.handler.BaseConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * A virtual config handler for Control Tags. Redirects
 * all requests received to the appropriate handler based
 * on their properties
 */
@Named
@Singleton
public class ControlTagHandlerSelector implements BaseConfigHandler {

  private final AliveTagConfigHandler aliveTagConfigHandler;
  private final CommFaultConfigHandler commFaultConfigHandler;
  private final StateTagConfigHandler stateTagConfigHandler;

  @Inject
  public ControlTagHandlerSelector(
    AliveTagConfigHandler aliveTagConfigHandler,
    CommFaultConfigHandler commFaultConfigHandler,
    StateTagConfigHandler stateTagConfigHandler) {

    this.aliveTagConfigHandler = aliveTagConfigHandler;
    this.commFaultConfigHandler = commFaultConfigHandler;
    this.stateTagConfigHandler = stateTagConfigHandler;
  }

  @Override
  public List<ProcessChange> create(ConfigurationElement element) {
    return chooseHandler(element.getElementProperties()).create(element);
  }

  @Override
  public List<ProcessChange> update(Long id, Properties properties) {
    return chooseHandler(properties).update(id, properties);
  }

  @Override
  public List<ProcessChange> remove(Long id, ConfigurationElementReport report) {
    if (aliveTagConfigHandler.getCache().containsKey(id)) {
      return aliveTagConfigHandler.remove(id, report);
    } else if (commFaultConfigHandler.getCache().containsKey(id)) {
      return commFaultConfigHandler.remove(id, report);
    } else if (stateTagConfigHandler.getCache().containsKey(id)){
      return stateTagConfigHandler.remove(id, report);
    } else {
      throw new IllegalArgumentException("Failed to find an appropriate handler for removing object with id: " + id);
    }
  }

  private BaseConfigHandler chooseHandler(Properties properties) {
    Collection<String> keysLowercase = properties.keySet().stream()
      // Values should always be non null, but let's be careful here
      .filter(Objects::nonNull).map(key -> key.toString().toLowerCase()).collect(Collectors.toSet());
    String nameLowercase = properties.getProperty("name","");

    if (keysLowercase.contains("address") || nameLowercase.contains("alive")) {
      return aliveTagConfigHandler;
      // Pretty permissive here, but we don't expect to receive any command tags
    } else if (nameLowercase.contains("comm")) {
      return commFaultConfigHandler;
    } else if (nameLowercase.contains("state") || nameLowercase.contains("status")) {
      return stateTagConfigHandler;
    }

    // A try catch higher up will handle this
    throw new IllegalArgumentException("Failed to find an appropriate handler for object");
  }
}
