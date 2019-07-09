package cern.c2mon.server.cache.alarm;

import java.util.Observable;
import java.util.Observer;

@FunctionalInterface
public interface TestObserver<T> extends Observer {

  @Override
  default void update(Observable observable, Object o){
    doThing((T) o);
  }

//  Class<T> getClassRef();

  void doThing(T thingie);
}
