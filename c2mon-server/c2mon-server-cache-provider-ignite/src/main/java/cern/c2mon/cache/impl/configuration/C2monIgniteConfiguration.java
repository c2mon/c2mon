package cern.c2mon.cache.impl.configuration;

import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;


public class C2monIgniteConfiguration {

    @Autowired
    private IgniteProperties properties;

    @Bean
    public IgniteConfiguration igniteConfiguration() {
        IgniteConfiguration cfg = new IgniteConfiguration();

        if (properties.isEmbedded()) {
            cfg.setClientMode(false);
        } else {
            cfg.setClientMode(true);
            cfg.setPeerClassLoadingEnabled(true);

            TcpDiscoverySpi spi = new TcpDiscoverySpi();
            TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
            ipFinder.setAddresses(properties.getAddresses());
            spi.setIpFinder(ipFinder);

            cfg.setDiscoverySpi(spi);
        }
        return cfg;
    }
}
