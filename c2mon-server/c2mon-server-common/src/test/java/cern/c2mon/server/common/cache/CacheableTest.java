package cern.c2mon.server.common.cache;

import cern.c2mon.shared.common.Cacheable;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.Assert.*;

public abstract class CacheableTest<T extends Cacheable> {

  private final T sample;

  protected CacheableTest(T sample) {
    this.sample = sample;
  }

  @Test
  public void getId() {
    Long id = sample.getId();
    // Is this desired behaviour?
    assertNotNull("Id should not be initialized to null", id);
  }

  @Test
  public void cloneIsImplemented() {
    // This should be verified in compile time due to Generics limitation above, but let's test anyway
    assertThat(sample, CoreMatchers.instanceOf(Cloneable.class));
    assertEquals(sample, sample.clone());
  }

  @Test
  public void equalsIsImplemented() {
    try {
      assertNotEquals(Object.class,
        sample.getClass().getMethod("equals", Object.class).getDeclaringClass());
    } catch (NoSuchMethodException e) {
      fail("Equals should be implemented for all cache objects");
    }
    assertEquals(sample, sample);
  }

  @Test
  public void cloneCreatesDeepCopy() {
    T cloneObject = (T) sample.clone();
    // Mutate object
    mutateObject(cloneObject);
    assertNotEquals(sample, cloneObject);
  }

  @Test
  public void timestamp() {
    // Initial value should never be null
    assertNotNull(sample.getCacheTimestamp());
    Timestamp test = Timestamp.from(Instant.EPOCH);
    sample.setCacheTimestamp(test);
    assertEquals(test, sample.getCacheTimestamp());
  }

  @Test(expected = NullPointerException.class)
  public void timestampNonNull() {
    sample.setCacheTimestamp(null);
  }

  protected abstract void mutateObject(T cloneObject);
}
