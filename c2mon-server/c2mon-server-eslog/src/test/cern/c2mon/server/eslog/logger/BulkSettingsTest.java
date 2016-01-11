package cern.c2mon.server.eslog.logger;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Alban Marguet.
 */
@Slf4j
public class BulkSettingsTest {
  @Test
  public void shouldHaveTheRightValues() {
    assertThat(BulkSettings.BULK_ACTIONS.getSetting(), is(5600));
    assertThat(BulkSettings.CONCURRENT.getSetting(), is(1));
    assertThat(BulkSettings.BULK_SIZE.getSetting(), is(5));
    assertThat(BulkSettings.FLUSH_INTERVAL.getSetting(), is(10));
  }
}