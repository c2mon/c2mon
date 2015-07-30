package cern.c2mon.shared.client.configuration.configuration;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public interface ConfigurationListener {

  void onStart();

  void onUpdate();

  void onFinish();
}
