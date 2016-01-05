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
public class IndexSettingsTest {
  @Test
  public void shouldHaveTheRightMonthlyValues() {
    assertThat(IndexSettings.INDEX_MONTH_SETTINGS.getReplica(), is(0));
    assertThat(IndexSettings.INDEX_MONTH_SETTINGS.getShards(), is(10));
  }

  @Test
  public void shouldHaveTheRightDailyValues() {
    assertThat(IndexSettings.INDEX_DAILY_SETTINGS.getReplica(), is(0));
    assertThat(IndexSettings.INDEX_DAILY_SETTINGS.getShards(), is(1));
  }
}