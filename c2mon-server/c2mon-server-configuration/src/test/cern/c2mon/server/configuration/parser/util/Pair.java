package cern.c2mon.server.configuration.parser.util;

/**
 * Describes a data-tuple
 *
 * @author Franz Ritter
 * @param <T1>
 *          Type of the first component
 * @param <T2>
 *          Type of the second component
 */
public class Pair<T1, T2> {

  /** The first component */
  public final T1 _1;

  /** The second component */
  public final T2 _2;

  /**
   * Constructs a new Pair instance
   *
   * @param _1
   *          The first component
   * @param _2
   *          The second component
   */
  public Pair(T1 _1, T2 _2) {
    this._1 = _1;
    this._2 = _2;
  }

  /**
   * @{inheritDoc}
   */
  @Override
  public String toString() {
    return "Pair [" + _1 + ", " + _2 + "]";
  }

  /**
   * @{inheritDoc}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
    result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
    return result;
  }

  /**
   * @{inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Pair<?, ?> other = (Pair<?, ?>) obj;
    if (_1 == null) {
      if (other._1 != null)
        return false;
    } else if (!_1.equals(other._1))
      return false;
    if (_2 == null) {
      if (other._2 != null)
        return false;
    } else if (!_2.equals(other._2))
      return false;
    return true;
  }

}
