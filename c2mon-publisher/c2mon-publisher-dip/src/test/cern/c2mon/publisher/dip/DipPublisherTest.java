package cern.c2mon.publisher.dip;

import static org.junit.Assert.*;

import org.junit.Test;

public class DipPublisherTest {
  
  @Test
  public void testIsASCII() {
    assertTrue(DipPublisher.isASCII(""));
    assertFalse(DipPublisher.isASCII(null));
    // french Real
    assertFalse(DipPublisher.isASCII("R\u00E9al"));
    // degree sign
    assertFalse(DipPublisher.isASCII("\u00B0C"));
  }
}
