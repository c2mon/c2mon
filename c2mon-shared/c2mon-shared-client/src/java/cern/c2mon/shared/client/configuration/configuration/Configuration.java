package cern.c2mon.shared.client.configuration.configuration;

import cern.c2mon.shared.client.configuration.configuration.process.Process;
import lombok.Builder;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Builder
public class Configuration {

  private String name;

  private String application;

  private String user;

  @Singular
  private List<Process> processes = new ArrayList<>();
}
