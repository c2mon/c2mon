package cern.c2mon.server.eslog.logger;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests that the settings are well defined by default to 10 shards and 0 replica.
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