package cern.c2mon.publisher.dip;

import static org.junit.Assert.*;

import org.junit.Test;

public class DipPublisherTest {
  
  @Test
  public void testIsASCII() {
    assertTrue(DipPublisher.isASCII(""));
    assertFalse(DipPublisher.isASCII(null));
    assertFalse(DipPublisher.isASCII("Réal"));
    assertFalse(DipPublisher.isASCII("°C"));
  }
}
