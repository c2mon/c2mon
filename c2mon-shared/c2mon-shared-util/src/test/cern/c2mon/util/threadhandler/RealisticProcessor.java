package cern.c2mon.util.threadhandler;

public interface RealisticProcessor {

  void processUpdate(Object object);
  
  void processUpdate1(Long object);
  
  void processUpdate2(Long object, Integer integer);
  
  void processUpdate3(Object object, String string);
  
  void processUpdate4(Object object, Object object2);
  
  void processUpdate5(Object object, Long arg1, Integer arg2, Boolean arg3);
  
}
