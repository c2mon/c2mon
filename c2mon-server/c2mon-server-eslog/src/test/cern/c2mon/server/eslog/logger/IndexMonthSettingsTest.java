package cern.c2mon.server.eslog.logger;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Alban Marguet.
 */
@Slf4j
public class IndexMonthSettingsTest {
    @Test
    public void should() {
        assertThat(IndexMonthSettings.REPLICA.getSetting(), is(0));
        assertThat(IndexMonthSettings.SHARDS.getSetting(), is(10));
    }
}
