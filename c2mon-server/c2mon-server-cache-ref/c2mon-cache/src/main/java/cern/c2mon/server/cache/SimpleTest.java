package cern.c2mon.server.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.spi.C2monCacheProvider;

@Service
public class SimpleTest implements CommandLineRunner {

  @Autowired
  C2monCacheProvider c2monCacheProvider;

  @Override
  public void run(String... strings) {
    c2monCacheProvider.test();
  }
}
