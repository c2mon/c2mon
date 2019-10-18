package cern.c2mon.server.common.cache;

import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
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

  /**
   * @see <a href=https://docs.oracle.com/javase/tutorial/reflect/member/ctorInstance.html>Class.newInstance()
   * doc</a>
   */
  @Test
  public void hasDefaultEmptyCtor() {
    try {
      Object newInstance = sample.getClass().newInstance();
      assertNotNull(newInstance);
      // This also verifies we didn't go to a parent Ctor
      assertEquals(newInstance.getClass(), sample.getClass());
    } catch (InstantiationException | IllegalAccessException e) {
      fail("An empty default Ctor should be implemented for all cache objects");
    }
  }

  @Test
  public void cloneIsImplemented() {
    // This should be verified in compile time due to Generics limitation above, but let's test anyway
    Assert.assertThat(sample, CoreMatchers.instanceOf(Cloneable.class));
    assertEquals(sample, sample.clone());
  }

  @Test
  public void equalsIsImplemented() {
    try {
      assertEquals(sample.getClass(),
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

  @Test
  public void preInsertValidateAcceptsNull() {
    // Should not throw - null will be passed at least once!
    sample.preInsertValidate(null);
    // Should almost always return true, otherwise the first item can never be inserted to the cache!
    // There are cases where this could reasonably be false - override this test in the corresponding
    // child class test to check for the custom logic!
    assertTrue(sample.preInsertValidate(null));
  }

  @Test
  public void postInsertEventsAcceptsNull() {
    // Should not throw - null will be passed at least once!
    sample.postInsertEvents(null);
    // Should almost always be contained, unless an implementation for some reason doesn't want to
    // emit this event. Usually however this test failing means you didn't call super.postInsertEvents.
    // If you have a reasonable case, override this test in the corresponding child class test
    assertTrue(sample.postInsertEvents(null).contains(CacheEvent.INSERTED));
  }

  protected abstract void mutateObject(T cloneObject);
}
