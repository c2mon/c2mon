package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.spi.C2monCacheProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IgniteC2monCacheProvider implements C2monCacheProvider {

  @Override
  public void test() {
    IgniteConfiguration igniteConfiguration = new IgniteConfiguration();

    Ignite ignite = Ignition.start(igniteConfiguration);
  }
}
