package cern.c2mon.server.common.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import cern.c2mon.shared.common.Cacheable;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public abstract class CacheObjectTest<T extends Cacheable> {

  private final T sample;

  protected CacheObjectTest(T sample) {
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
    try {
      assertEquals(sample, sample.clone());
    } catch (CloneNotSupportedException e) {
      fail("Clone should be implemented for all cache objects");
    }
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
    try {
      T cloneObject = (T) sample.clone();
      // Mutate object
      mutateObject(cloneObject);
      assertNotEquals(sample, cloneObject);
    } catch (CloneNotSupportedException e) {
      fail();
    }
  }

  protected abstract void mutateObject(T cloneObject);
}
